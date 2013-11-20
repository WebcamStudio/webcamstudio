/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.externals;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.media.renderer.Capturer;
import webcamstudio.media.renderer.Exporter;
import webcamstudio.media.renderer.ProcessExecutor;
import webcamstudio.mixers.Frame;
import webcamstudio.streams.SinkFile;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
 */
public class ProcessRenderer {

    final static String RES_CAP = "capture_OS.properties";
    final static String RES_OUT = "output_OS.properties";

    public enum ACTION {

        CAPTURE,
        OUTPUT
    }
    java.io.DataInput input = null;
    boolean stopMe = false;
    boolean stopped = true;
    private Properties plugins = null;
    String plugin = "";
    int videoPort = 0;
    int audioPort = 0;
    int frequency = webcamstudio.WebcamStudio.audioFreq;
    int channels = 2;
    int bitSize = 16;
    Stream stream;
    ProcessExecutor processVideo;
    ProcessExecutor processAudio;
    public static String pidOutput;
    Capturer capture;
    Exporter exporter;
    FME fme = null;

    public ProcessRenderer(Stream s, ACTION action, String plugin) {
        this.plugin = plugin;
        stream = s;
        if (plugins == null) {
            plugins = new Properties();
            try {
                if (plugin.equals("custom")) {
                    plugins.load(stream.getFile().toURI().toURL().openStream());
                } else {
                    plugins.load(getResource(action).openStream());
                }
            } catch (IOException ex) {
                Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        processVideo = new ProcessExecutor(s.getName());        
        processAudio = new ProcessExecutor(s.getName());

    }

    private String translateTag(String value) {
        String result = value.toUpperCase().replace('.', '_');
        if (plugins.containsKey("TAG_" + result)) {
            result = plugins.getProperty("TAG_" + result);
        }
        return result;
    }

    public ProcessRenderer(Stream s, FME fme, String plugin) {
        stream = s;
        this.plugin = plugin;
        this.fme = fme;
        if (plugins == null) {
            plugins = new Properties();
            try {
                plugins.load(getResource(ACTION.OUTPUT).openStream());
            } catch (IOException ex) {
                Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        processVideo = new ProcessExecutor(s.getName());
        processAudio = new ProcessExecutor(s.getName());

    }

    private URL getResource(ACTION a) throws MalformedURLException {

        File userSettings = null;
        switch (a) {
            case CAPTURE:
                userSettings = new File(new File(System.getProperty("user.home") + "/.webcamstudio"), RES_CAP.replaceAll("OS", Tools.getOSName()));
                break;
            case OUTPUT:
                userSettings = new File(new File(System.getProperty("user.home") + "/.webcamstudio"), RES_OUT.replaceAll("OS", Tools.getOSName()));
                break;
        }
        URL res = null;
        if (userSettings.exists()) {
            res = userSettings.toURI().toURL();
        } else {
            String path = null;
            switch (a) {
                case CAPTURE:
                    path = "/webcamstudio/externals/OS/sources/" + plugin + ".properties";
                    path = path.replaceAll("OS", Tools.getOSName());
                    break;
                case OUTPUT:
                    path = "/webcamstudio/externals/OS/outputs/output.properties";
                    path = path.replaceAll("OS", Tools.getOSName());
                    break;
            }
            res = ProcessRenderer.class.getResource(path);
        }
//        System.out.println("Resource Used: " + res.toString());
        return res;
    }

    private String setParameters(String cmd) {
        String command = cmd;
        for (Tags tag : Tags.values()) {
            switch (tag) {
                case DESKTOPN:
                    command = command.replaceAll(Tags.DESKTOPN.toString(), stream.getDesktopN() + "");
                    break;
                case DESKTOPX:
                    command = command.replaceAll(Tags.DESKTOPX.toString(), stream.getDesktopX() + "");
                    break;
                case DESKTOPY:
                    command = command.replaceAll(Tags.DESKTOPY.toString(), stream.getDesktopY() + "");
                    break;
                case DESKTOPENDX:
                    command = command.replaceAll(Tags.DESKTOPENDX.toString(), stream.getDesktopEndX() + "");
                    break;
                case DESKTOPENDY:
                    command = command.replaceAll(Tags.DESKTOPENDY.toString(), stream.getDesktopEndY() + "");
                    break;
                case DESKTOPW:
                    command = command.replaceAll(Tags.DESKTOPW.toString(), stream.getDesktopW() + "");
                    break;
                case DESKTOPH:
                    command = command.replaceAll(Tags.DESKTOPH.toString(), stream.getDesktopH() + "");
                    break;
                case VCODEC:
                    if (fme != null) {
                        command = command.replaceAll(Tags.VCODEC.toString(), translateTag(fme.getVcodec()));
                    }
                    break;
                case ACODEC:
                    if (fme != null) {
                        command = command.replaceAll(Tags.ACODEC.toString(), translateTag(fme.getAcodec()));
                    }
                    break;
                case VBITRATE:
                    if (fme != null) {
                        command = command.replaceAll(Tags.VBITRATE.toString(), fme.getVbitrate());
                    }
                    break;
                case ABITRATE:
                    if (fme != null) {
                        command = command.replaceAll(Tags.ABITRATE.toString(), fme.getAbitrate());
                    }
                    break;
                case URL:
                    if (fme != null) {
                switch (fme.getName().toLowerCase()) {
                    case "red5":
                        command = command.replaceAll(Tags.URL.toString(), "" + fme.getUrl() + "/" + fme.getStream());
                        break;
                    case "icecast":
                        command = command.replaceAll(Tags.URL.toString(), "" + fme.getUrl());
                        break;
                    default:
                        command = command.replaceAll(Tags.URL.toString(), "\""+fme.getUrl()+"/"+fme.getStream()+" live=1 flashver=FME/2.520(compatible;20FMSc201.0)"+"\"");
                        break;
                }
                    } else if (stream.getURL() != null) {
                        command = command.replaceAll(Tags.URL.toString(), "" + stream.getURL());
                    }
                    break;
                case MOUNT:
                    if (fme != null) {
                        if (fme.getName().toLowerCase().equals("icecast")){
                            command = command.replaceAll(Tags.MOUNT.toString(), "" + fme.getMount());
                        }
                    }    
                case PASSWORD:
                    if (fme != null) {
                        if (fme.getName().toLowerCase().equals("icecast")){
                            command = command.replaceAll(Tags.PASSWORD.toString(), "" + fme.getPassword());
                        }
                    }
                case PORT:
                    if (fme != null) {
                        if (fme.getName().toLowerCase().equals("icecast")){
                            command = command.replaceAll(Tags.PORT.toString(), "" + fme.getPort());
                        }
                    }    
                case APORT:
                    command = command.replaceAll(Tags.APORT.toString(), "" + audioPort);
                    break;
                case CHEIGHT:
                    command = command.replaceAll(Tags.CHEIGHT.toString(), "" + stream.getCaptureHeight());
                    break;
                case CWIDTH:
                    command = command.replaceAll(Tags.CWIDTH.toString(), "" + stream.getCaptureWidth());
                    break;
                case FILE:
                    if (stream.getFile() != null) {
                        if (Tools.getOS() == OS.WINDOWS) {
                            command = command.replaceAll(Tags.FILE.toString(), "\"" + stream.getFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\") + "\"");
                        } else {
                            if (stream.getFile().getAbsolutePath().contains("http")) {
                                command = command.replaceAll(Tags.FILE.toString(), "" + stream.getFile().getAbsolutePath().replace(System.getProperty("user.home")+"/", "") + "");
                            } else {
                                String sFile = stream.getFile().getAbsolutePath().replaceAll(" ", "\\ ");
                                if (stream instanceof SinkFile){
                                    sFile = sFile.replaceAll(" ", "_");
                                }
                                command = command.replaceAll(Tags.FILE.toString(), "" + sFile + "");
                            }
                        }
                    }
                    break;
                case OHEIGHT:
                    command = command.replaceAll(Tags.OHEIGHT.toString(), "" + stream.getHeight());
                    break;
                case OWIDTH:
                    command = command.replaceAll(Tags.OWIDTH.toString(), "" + stream.getWidth());
                    break;
                case RATE:
                    command = command.replaceAll(Tags.RATE.toString(), "" + stream.getRate());
                    break;
                case SEEK:
                    command = command.replaceAll(Tags.SEEK.toString(), "" + stream.getSeek());
                    break;
                case VPORT:
                    command = command.replaceAll(Tags.VPORT.toString(), "" + videoPort);
                    break;
                case WEBURL:
                    command = command.replaceAll(Tags.WEBURL.toString(), "\""+stream.getWebURL()+"\"");
                case BW:
                    command = command.replaceAll(Tags.BW.toString(), "" + stream.getDVBBandwidth());
                case DVBFREQ:
                    command = command.replaceAll(Tags.DVBFREQ.toString(), "" + stream.getDVBFrequency());
                case DVBCH:
                    command = command.replaceAll(Tags.DVBCH.toString(), "" + stream.getDVBChannelNumber());
                case FREQ:
                    command = command.replaceAll(Tags.FREQ.toString(), "" + frequency);
                    break;
                case BITSIZE:
                    command = command.replaceAll(Tags.BITSIZE.toString(), "" + bitSize);
                    break;
                case CHANNELS:
                    command = command.replaceAll(Tags.CHANNELS.toString(), "" + channels);
                    break;
                case AUDIOSRC:
                    command = command.replaceAll(Tags.AUDIOSRC.toString(), "" + stream.initAudioSource());
                    break;
            }
        }
        return command;
    }

    public Frame getFrame() {
        if (capture == null) {
            return null;
        } else {
            return capture.getFrame();
        }
    }

    public void read() {
        stopped = false;
        stopMe = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                stream.setVideo(plugins.containsKey("video"));
                stream.setFakeVideo(plugins.containsKey("fakeVideo"));
                stream.setFakeAudio(plugins.containsKey("fakeAudio"));
                stream.setAudio(plugins.containsKey("audio"));
                capture = new Capturer(stream);
                if (stream.hasVideo()) {
                    videoPort = capture.getVideoPort();
                }
                if (stream.hasAudio()) {
                    audioPort = capture.getAudioPort();
                }
                String commandVideo = null;
                String commandAudio = null;
                //System.out.println(plugins.keySet().toString());
                if (plugins.containsKey("video")) {
                    if ("AV".equals(stream.getComm())){
                        commandVideo = plugins.getProperty("AVvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                        commandVideo = plugins.getProperty("GSvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                    }
                }
                if (plugins.containsKey("audio")) {
                    if ("AV".equals(stream.getComm())){
                        commandAudio = plugins.getProperty("AVaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                        commandAudio = plugins.getProperty("GSaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                    }
                }
                if (commandVideo != null) {
                    commandVideo = commandVideo.replaceAll(" ", "ABCDE");
                    commandVideo = setParameters(commandVideo);
                    String[] parmsVideo = commandVideo.split("ABCDE");
                    try {
//                        for (String p : parmsVideo) {
//                            System.out.print(p + " ");
//                        }
//                        System.out.println();
                        processVideo.execute(parmsVideo);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    processVideo = null;
                }
                if (commandAudio != null) {
                    commandAudio = commandAudio.replaceAll(" ", "ABCDE");
                    commandAudio = setParameters(commandAudio);
                    String[] parmsAudio = commandAudio.split("ABCDE");
                    try {
//                        for (String p : parmsAudio) {
//                            System.out.print(p + " ");
//                        }
//                        System.out.println();
                        processAudio.execute(parmsAudio);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    processAudio = null;
                }

            }
        }).start();

    }
    public void readCom() {
        stopped = false;
        stopMe = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (stream.isIPCam()){
                    stream.setVideo(true);
                } else if (stream.hasVideo()) {
                    stream.setVideo(plugins.containsKey("video"));
                }
                stream.setFakeVideo(plugins.containsKey("fakeVideo"));
                stream.setFakeAudio(plugins.containsKey("fakeAudio"));
                capture = new Capturer(stream);
                if (stream.hasVideo()) {
                    videoPort = capture.getVideoPort();
                } else {
                    processVideo = null;
                }
                if (stream.hasAudio()) {
                    audioPort = capture.getAudioPort();
                } else {
                    processAudio = null;
                }
                String commandVideo = null;
                String commandAudio = null;
                if (plugins.containsKey("video") && stream.isIPCam()) {
                    commandVideo = plugins.getProperty("videoIP").replaceAll("  ", " "); //Making sure there is no double spaces
                } else if (plugins.containsKey("video") && stream.isStillPicture()) {
                    commandVideo = plugins.getProperty("videoPic").replaceAll("  ", " "); //Making sure there is no double spaces
                } else if (plugins.containsKey("video")) {
                    if (stream.getWebURL().toLowerCase().contains("udp")) {
                        commandVideo = plugins.getProperty("GSvideoUDP").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                    if ("AV".equals(stream.getComm())){
                        commandVideo = plugins.getProperty("AVvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                        commandVideo = plugins.getProperty("GSvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                    }
//                    commandVideo = plugins.getProperty("video").replaceAll("  ", " "); //Making sure there is no double spaces
                }
                }
                if (plugins.containsKey("audio")) {
                    if (stream.getWebURL().toLowerCase().contains("udp")) {
                        commandAudio = plugins.getProperty("GSaudioUDP").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                    if ("AV".equals(stream.getComm())){
                        commandAudio = plugins.getProperty("AVaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                        commandAudio = plugins.getProperty("GSaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                    }
//                    commandAudio = plugins.getProperty("audio").replaceAll("  ", " "); //Making sure there is no double spaces
                }
                }
                if (commandVideo != null) {
                    commandVideo = setParameters(commandVideo);
                }
                if (commandAudio != null) {
                    commandAudio = setParameters(commandAudio);
                }
                File fileV=new File(System.getProperty("user.home")+"/.webcamstudio/"+"WSFromUrlVideo"+stream.getID()+".sh");
                File fileA=new File(System.getProperty("user.home")+"/.webcamstudio/"+"WSFromUrlAudio"+stream.getID()+".sh");
                FileOutputStream fosV;
                DataOutputStream dosV = null;
                FileOutputStream fosA;
                DataOutputStream dosA = null;
                try {
                    fosV = new FileOutputStream(fileV);
                    dosV= new DataOutputStream(fosV);
                    fosA = new FileOutputStream(fileA);
                    dosA= new DataOutputStream(fosA);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    dosV.writeBytes("#!/bin/bash\n");
                    dosV.writeBytes(commandVideo+"\n");
                    dosA.writeBytes("#!/bin/bash\n");
                    dosA.writeBytes(commandAudio+"\n");
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                Runtime rt = Runtime.getRuntime();
                String batchVideoCommand = "sh "+System.getProperty("user.home")+"/.webcamstudio/"+"WSFromUrlVideo"+stream.getID()+".sh";
                String batchAudioCommand = "sh "+System.getProperty("user.home")+"/.webcamstudio/"+"WSFromUrlAudio"+stream.getID()+".sh";
                try {
                    if (stream.hasVideo()) {
                        processVideo.executeString(batchVideoCommand);
                    }
                    if (stream.hasAudio()) {
                        processAudio.executeString(batchAudioCommand);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();    
    }
    
    public void writeCom() {
        stopped = false;
        stopMe = false;
        new  Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    exporter = new Exporter(stream);
                } catch (SocketException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                videoPort = exporter.getVideoPort();
                audioPort = exporter.getAudioPort();
                stopped = false;
                stopMe = false;
                String command = plugins.getProperty(plugin).replaceAll("  ", " "); //Making sure there is no double spaces
                command = setParameters(command);
//                System.out.println("Command: "+command);
                File file=new File(System.getProperty("user.home")+"/.webcamstudio/"+"WSBroadcast.sh");
                FileOutputStream fos;
                DataOutputStream dos = null;
                try {
                    fos = new FileOutputStream(file);
                    dos= new DataOutputStream(fos);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    dos.writeBytes("#!/bin/bash\n");
                    dos.writeBytes(command+"\n");
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                Runtime rt = Runtime.getRuntime();
                try {
                    Process p = rt.exec("chmod a+x "+System.getProperty("user.home")+"/.webcamstudio/"+"WSBroadcast.sh");
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                String batchCommand = "sh "+System.getProperty("user.home")+"/.webcamstudio/"+"WSBroadcast.sh";
                try {
                    processVideo.executeString(batchCommand);
                    processAudio = null;
                    //We don't need processAudio on export.  Only 1 process is required...
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public void write() {
        stopped = false;
        stopMe = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    exporter = new Exporter(stream);
                } catch (SocketException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                videoPort = exporter.getVideoPort();
                audioPort = exporter.getAudioPort();
                stopped = false;
                String command = plugins.getProperty(plugin).replaceAll("  ", " "); //Making sure there is no double spaces
                command = setParameters(command);
//                System.out.println(command);

                final String[] parms = command.split(" ");
                try {
                    processVideo.execute(parms);
                    processAudio = null;
                    //We don't need processAudio on export.  Only 1 process is required...
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void stop() {
        stopMe = true;
        if (capture != null) {
            capture.abort();
            capture = null;
        }
        if (exporter != null) {
            exporter.abort();
            exporter = null;
        }
        if (processVideo != null) {
            if (stream instanceof SinkFile) {
                System.out.println("Delay for SinkFile ...");
                Tools.sleep(1700);
            }
            System.out.println(stream.getName()+" Video Cleared ...");
                processVideo.destroy();
                processVideo = null;
        }
        if (processAudio != null) {
            System.out.println(stream.getName()+" Audio Cleared ...");
            processAudio.destroy();
            processAudio = null;
        }
        stopMe = false;
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getBitSize() {
        return bitSize;
    }

    public int getChannels() {
        return channels;
    }
}
