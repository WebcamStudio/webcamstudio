/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.externals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import static webcamstudio.WebcamStudio.audioFreq;
import static webcamstudio.WebcamStudio.outFMEbe;
import static webcamstudio.WebcamStudio.wsDistroWatch;
import static webcamstudio.externals.ProcessRenderer.ACTION.OUTPUT;
import webcamstudio.media.renderer.Capturer;
import webcamstudio.media.renderer.Exporter;
import webcamstudio.media.renderer.ProcessExecutor;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.streams.SinkFile;
import webcamstudio.streams.SinkUDP;
import webcamstudio.streams.SourceDVB;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;
import webcamstudio.util.Tools.OS;

/**
 *
 * @author patrick (modified by karl)
 *
 * ProcessRenderer is used to execute external commands which produce
 * audio or video streams, and to manage the process of reading those
 * streams.
 *
 * ProcessRenderer first tries to load a .properties file defining the
 * command to be run. This will either be a file in the application
 * JAR selected by the caller's choice of plugin, or a user
 * .properties file in ~/.webcamstudio
 *
 * Next ProcessRenderer replaces parameter markers in the command
 * string with parameter values obtained from the associated Stream
 *
 * Finally, ProcessRenderer launches the command process, and (in the
 * case of sources) launches a Capturer to read the data from the
 * process output.
 */
public class ProcessRenderer {

    final static String RES_CAP = "capture_OS.properties";
    final static String RES_OUT = "output_OS.properties";
    private final static String userHomeDir = Tools.getUserHome();
    public static String pidOutput;

    //Author Martijn Courteaux Code
    public static int getUnixPID(Process process) throws Exception {
        System.out.println("Process_GetUnixPid: "+process.getClass().getName());
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            Class cl = process.getClass();
            Field field = cl.getDeclaredField("pid");
            field.setAccessible(true);
            Object pidObject = field.get(process);
            return (Integer) pidObject;
        } else {
            throw new IllegalArgumentException("Needs to be a UNIXProcess");
        }
    }

    java.io.DataInput input = null;
    boolean stopMe = false;
    boolean stopped = true;
    private Properties plugins = null;
    String plugin = "";    // Used to select the .properties file containing command strings
    String oPlug = "output";
    String audioPulseInput = "";
    int videoPort = 0;
    int audioPort = 0;
    int frequency = audioFreq;
    int channels = 2;
    int bitSize = 16;
    Stream stream;
    ProcessExecutor processVideo;
    ProcessExecutor processAudio;
    Capturer capture;
    Exporter exporter;
    FME fme = null;
    private final MasterMixer mixer = MasterMixer.getInstance();

    public ProcessRenderer(Stream s, ACTION action, String plugin, String bkEnd) {
        stream = s;
        // System.out.println("BackEnd:"+bkEnd);
        String distro = wsDistroWatch();
        if (bkEnd.equals("FF")) {
            if (action == OUTPUT) {
                //System.out.println("Action Output - BackEnd FF !!!");
                this.oPlug = "ffmpeg_output";
                this.plugin = plugin;
            } else {
                this.oPlug = "output";
                this.plugin = "ffmpeg_" + plugin;
                s.setComm("AV");
            }
        } else {
            if (action == OUTPUT) {
                if (bkEnd.equals("AV")) {
                    this.oPlug = "output";
                    this.plugin = plugin;
                } else {
                    this.oPlug = "gst_output";
                    this.plugin = plugin;
                }
            } else {
                if (distro.toLowerCase().equals("ubuntu")) {
                    this.plugin = plugin;
                } else if ("AV".equals(bkEnd) && plugin.equals("audiosource")) {
                    this.plugin = "av_" + plugin;
                } else { //if ("AV".equals(bkEnd))
                    this.plugin = plugin;
                }
            }
        }
//        System.out.println("OPlugin:"+oPlug);
//        System.out.println("Plugin: "+this.plugin);
        if (plugins == null) {
            plugins = new Properties();
            try {
                if (plugin.equals("custom")) {
                    plugins.load(stream.getFile().toURI().toURL().openStream());
                    System.out.println("Plugins Custom: "+plugins);
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

    public ProcessRenderer(Stream s, FME fme, String plugin) {
        stream = s;
        this.plugin = plugin;
        if (outFMEbe == 0) {
            this.oPlug = "ffmpeg_output";
        } else if (outFMEbe == 1) {
            this.oPlug = "output";
        } else if (outFMEbe == 2) {
            this.oPlug = "gst_output";
        }
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

    private String translateTag(String value) {
        String result = value.toUpperCase().replace('.', '_');
        if (plugins.containsKey("TAG_" + result)) {
            result = plugins.getProperty("TAG_" + result);
        }
        return result;
    }

    private URL getResource(ACTION a) throws MalformedURLException {
        File userSettings = null;

        switch (a) {
            case CAPTURE:
                userSettings = new File(new File(userHomeDir + "/.webcamstudio"), RES_CAP.replaceAll("OS", Tools.getOSName()));
                break;

            case OUTPUT:
                userSettings = new File(new File(userHomeDir + "/.webcamstudio"), RES_OUT.replaceAll("OS", Tools.getOSName()));
                break;
        }

        URL res = null;
        // FIXME: It seems what we've provided here is a way for the
        // user to override all the supplied source properties files
        // with ONE file:
        // So if I create
        // ~/.webcamstudio/capture_linux.properties and run WCS,
        // any source I try to launch will use the settings from that
        // file and there will be no difference between the different
        // types of sources...  right?   ---GEC
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
                    path = "/webcamstudio/externals/OS/outputs/"+ oPlug +".properties";
                    path = path.replaceAll("OS", Tools.getOSName());
                    break;
            }

            // Pull back-end properties file from JAR or installation directory
            res = ProcessRenderer.class.getResource(path);
        }
//        System.out.println("Resource Used: " + res.toString());
        return res;
    }

    private String setParameters(String cmd) {
        String command = cmd;
        String fmeName = null;
        String fmeURL = null;

        if (fme != null) {
            fmeName = fme.getName();
            fmeURL = fme.getUrl();
        }

        /* TODO: Replace this with a more elegant system. For instance:
           1: Add to Stream a call which provides all the tags and associated command string text for the stream
           2: Merge the data obtained in a call to (1) with a set of command parameters defined here (such as videoPort, etc.)
           3: Loop through the constructed list of parameters to perform the substitutions
           As a result ProcessRenderer will no longer need to switch through an exhaustive list of parameters supported by different Streams
           ---GEC
        */
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

                case XID:
                    command = command.replaceAll(Tags.XID.toString(), stream.getDesktopXid() + "");
                    break;

                case WINDOWX:
                    command = command.replaceAll(Tags.WINDOWX.toString(), stream.getWindowX() + "");
                    break;

                case WINDOWY:
                    command = command.replaceAll(Tags.WINDOWY.toString(), stream.getWindowY() + "");
                    break;

                case WINDOWENDX:
                    command = command.replaceAll(Tags.WINDOWENDX.toString(), stream.getWindowEndX() + "");
                    break;

                case WINDOWENDY:
                    command = command.replaceAll(Tags.WINDOWENDY.toString(), stream.getWindowEndY() + "");
                    break;

                case GUID:
                    command = command.replaceAll(Tags.GUID.toString(), stream.getGuid() + "");
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
                    if (stream instanceof SinkFile || stream instanceof SinkUDP) {
                        command = command.replaceAll(Tags.VBITRATE.toString(), stream.getVbitrate());
                    }
                    break;

                case ABITRATE:
                    if (fme != null) {
                        command = command.replaceAll(Tags.ABITRATE.toString(), fme.getAbitrate());
                    }
                    if (stream instanceof SinkFile || stream instanceof SinkUDP) {
                        command = command.replaceAll(Tags.ABITRATE.toString(), stream.getAbitrate());
                    }
                    break;

                case URL:
                    if (fme != null) {
                        if (!"".equals(fme.getMount())) {
                            command = command.replaceAll(Tags.URL.toString(), "" + fmeURL);
                        } else {
                            command = command.replaceAll(Tags.URL.toString(), "" + fmeURL + "/" + fme.getStream()); // "\""+fme.getUrl()+"/"+fme.getStream()+" live=1 flashver=FME/2.520(compatible;20FMSc201.0)"+"\""
                        }
                    } else if (stream.getURL() != null) {
                        command = command.replaceAll(Tags.URL.toString(), "" + stream.getURL());
                    }
                    break;

                case MOUNT:
                    if (fme != null && !"".equals(fme.getMount())) {
                        command = command.replaceAll(Tags.MOUNT.toString(), "" + fme.getMount());
                    }
                    break;

                case PASSWORD:
                    if (fme != null && !"".equals(fme.getPassword())) {
                        command = command.replaceAll(Tags.PASSWORD.toString(), "" + fme.getPassword());
                    }
                    break;

                case KEYINT:
                    if (fme != null) {
                        command = command.replaceAll(Tags.KEYINT.toString(), "" + fme.getKeyInt());
                    } else {
                        command = command.replaceAll(Tags.KEYINT.toString(), "" + Integer.toString(5*mixer.getRate()));
                    }
                    break;

                case PORT:
                    if (fme != null && !"".equals(fme.getPort())) {
                        command = command.replaceAll(Tags.PORT.toString(), "" + fme.getPort());
                    }
                    break;

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
                                command = command.replaceAll(Tags.FILE.toString(), "" + stream.getFile().getAbsolutePath().replace(userHomeDir+"/", "") + "");
                            } else {
                                String sFile = stream.getFile().getAbsolutePath().replaceAll(" ", "\\ ");
                                if (stream instanceof SinkFile) {
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

                case GSEFFECT:
                    command = command.replaceAll(Tags.GSEFFECT.toString(), "" + stream.getGSEffect());
                    break;

                case WEBURL:
                    if (stream.getProtected() && "wanscam".equals(stream.getPtzBrand())) {
                        String soloURL = stream.getWebURL().replace("http://", "");
                        command = command.replaceAll(Tags.WEBURL.toString(), "\""+"http://"+soloURL+"?user="+stream.getIPUser()+"&pwd="+stream.getIPPwd()+"\"");
                    }
                    if (stream.getProtected()) {
                        String soloURL = stream.getWebURL().replace("http://", "");
                        command = command.replaceAll(Tags.WEBURL.toString(), "\""+"http://"+stream.getIPUser()+":"+stream.getIPPwd()+"@"+soloURL+"\"");
                    } else {
                        command = command.replaceAll(Tags.WEBURL.toString(), "\""+stream.getWebURL()+"\"");
                    }
                    break;

                case BW:
                    command = command.replaceAll(Tags.BW.toString(), "" + stream.getDVBBandwidth());
                    break;

                case DVBFREQ:
                    command = command.replaceAll(Tags.DVBFREQ.toString(), "" + stream.getDVBFrequency());
                    break;

                case DVBCH:
                    command = command.replaceAll(Tags.DVBCH.toString(), "" + stream.getDVBChannelNumber());
                    break;

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
                    command = command.replaceAll(Tags.AUDIOSRC.toString(), "" + stream.getAudioSource());
                    break;

                case PAUDIOSRC:
                    command = command.replaceAll(Tags.PAUDIOSRC.toString(), "" + audioPulseInput);
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
                capture = new Capturer(stream);
                String cmdVideo = "";
                String cmdAudio = "";
                if (stream.hasVideo()) {
                    videoPort = capture.getVideoPort();
                }
                if (stream.hasAudio()) {
                    audioPort = capture.getAudioPort();
                }
                String commandVideo = null;
                String commandAudio = null;
                // System.out.println(plugins.keySet().toString());
                if (stream.hasVideo()) {
                    if ("AV".equals(stream.getComm())) {
                        commandVideo = plugins.getProperty("AVvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                        if (!"".equals(stream.getGSEffect())) {
                            if (!"".equals(stream.getDesktopXid())) {
                                commandVideo = plugins.getProperty("GSvideoFXSingle").replaceAll("  ", " "); //Making sure there is no double spaces
                            } else {
                                commandVideo = plugins.getProperty("GSvideoFX").replaceAll("  ", " "); //Making sure there is no double spaces
                            }
                        } else {
                            if (!"".equals(stream.getDesktopXid())) {
                                commandVideo = plugins.getProperty("GSvideoSingle").replaceAll("  ", " "); //Making sure there is no double spaces
                            } else {
                                commandVideo = plugins.getProperty("GSvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                            }
                        }
                    }
                }
                if (stream.hasAudio()) {
                    if ("AV".equals(stream.getComm())) {
                        commandAudio = plugins.getProperty("AVaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else {
                        commandAudio = plugins.getProperty("GSaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                    }
                }
                // fix gst-launch dvbsrc >= 1.4 "delsys" issue.
                if (stream instanceof SourceDVB) {
                    try {
                        Runtime.getRuntime().exec("gst-launch-1.0 -q dvbsrc delsys=DVB-T2");
                    } catch (IOException ex) {
                        Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Tools.sleep(100);
                }

                if (commandVideo != null) {
                    // FIXME: This "ABCDE" business is kind of crap,
                    // and using String.split() to split arguments means we have no way to put a space in a command argument. ---GEC
                    commandVideo = commandVideo.replaceAll(" ", "ABCDE");
                    commandVideo = setParameters(commandVideo);
                    String[] parmsVideo = commandVideo.split("ABCDE");

                    try {
                        for (String p : parmsVideo) {
                            cmdVideo = cmdVideo + p + " ";
                        }
                        System.out.print("CommandVideo: "+cmdVideo+"\n");
                        processVideo.execute(parmsVideo);
                    } catch (IOException | InterruptedException e) {
                        Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, e);
                    }
                } else {
                    processVideo = null;
                }

                if (commandAudio != null) {
                    commandAudio = commandAudio.replaceAll(" ", "ABCDE");
                    commandAudio = setParameters(commandAudio);
                    String[] parmsAudio = commandAudio.split("ABCDE");
                    try {
                        for (String p : parmsAudio) {
                            cmdAudio = cmdAudio + p + " ";
                        }
                        System.out.print("CommandAudio: "+cmdAudio+"\n");
                        processAudio.execute(parmsAudio);
                    } catch (IOException | InterruptedException e) {
                        Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, e);
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
        final String iD = stream.getID().substring(0, 4);
        new Thread(new Runnable() {

            @Override
            public void run() {

                if (stream.isIPCam()) {
                    stream.setVideo(true);
                }
                capture = new Capturer(stream);

                if (stream.isOnlyAudio()) {
                    processVideo = null;
                } else {
                    videoPort = capture.getVideoPort();
                }

                if (stream.hasAudio()) {
                    audioPort = capture.getAudioPort();
                } else {
                    processAudio = null;
                }

                String commandVideo = null;
                String commandAudio = null;
                if (stream.hasVideo() && stream.isIPCam()) {
                    commandVideo = plugins.getProperty("videoIP").replaceAll("  ", " "); //Making sure there is no double spaces
                } else if (stream.hasVideo() && stream.isStillPicture()) {
                    commandVideo = plugins.getProperty("videoPic").replaceAll("  ", " "); //Making sure there is no double spaces
                } else if (stream.hasVideo() && stream.isRTSP()) {
                    if (stream.getComm().equals("GS")) {
                        if (!"".equals(stream.getGSEffect())) {
                            commandVideo = plugins.getProperty("RTSPvideoFX").replaceAll("  ", " "); //Making sure there is no double spaces
                        } else {
                            commandVideo = plugins.getProperty("GSRTSPvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                        }
                    } else {
                        commandVideo = plugins.getProperty("AVRTSPvideo").replaceAll("  ", " ");
                    }
                } else if (stream.hasVideo() && stream.isRTMP()) {
                    if (stream.getComm().equals("GS")) {
                        if (!"".equals(stream.getGSEffect())) {
                            commandVideo = plugins.getProperty("RTMPvideoFX").replaceAll("  ", " "); //Making sure there is no double spaces
                        } else {
                            commandVideo = plugins.getProperty("GSRTMPvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                        }
                    } else {
                        commandVideo = plugins.getProperty("AVRTMPvideo").replaceAll("  ", " ");
                    }
                } else if (stream.hasVideo()) {
                    if (stream.getWebURL().toLowerCase().startsWith("udp")) {
                        if (!"".equals(stream.getGSEffect())) {
                            commandVideo = plugins.getProperty("GSvideoUDPFX").replaceAll("  ", " "); //Making sure there is no double spaces
                        } else {
                            commandVideo = plugins.getProperty("GSvideoUDP").replaceAll("  ", " "); //Making sure there is no double spaces
                        }
                    } else {
                        if ("AV".equals(stream.getComm())) {
                            if (!stream.isOnlyAudio()) {
                                if (stream.hasVideo()) {
                                    commandVideo = plugins.getProperty("AVvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                                } else {
                                    if (!"".equals(stream.getGSEffect())) {
                                        commandVideo = plugins.getProperty("SndvideoFX").replaceAll("  ", " "); //Making sure there is no double spaces
                                        stream.setHasVideo(true);
                                    } else {
                                        commandVideo = plugins.getProperty("Sndvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                                        stream.setHasVideo(true);
                                    }
                                }
                            } else {
                                commandVideo = plugins.getProperty("AVvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                            }
                        } else {
                            if (!stream.isOnlyAudio()) {
                                if (stream.hasVideo()) {
                                    if (!"".equals(stream.getGSEffect())) {
                                        commandVideo = plugins.getProperty("GSvideoFX").replaceAll("  ", " "); //Making sure there is no double spaces
                                    } else {
                                        commandVideo = plugins.getProperty("GSvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                                    }
                                } else {
                                    if (!"".equals(stream.getGSEffect())) {
                                        commandVideo = plugins.getProperty("SndvideoFX").replaceAll("  ", " "); //Making sure there is no double spaces
                                        stream.setHasVideo(true);
                                    } else {
                                        commandVideo = plugins.getProperty("Sndvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                                        stream.setHasVideo(true);
                                    }
                                }
                            } else {
                                if (!"".equals(stream.getGSEffect())) {
                                    commandVideo = plugins.getProperty("GSvideoFX").replaceAll("  ", " "); //Making sure there is no double spaces
                                } else {
                                    commandVideo = plugins.getProperty("GSvideo").replaceAll("  ", " "); //Making sure there is no double spaces
                                }
                            }
                        }
                    }
                }
                if (stream.hasAudio()) {
                    if (stream.getWebURL().toLowerCase().startsWith("udp")) {
                        commandAudio = plugins.getProperty("GSaudioUDP").replaceAll("  ", " "); //Making sure there is no double spaces
                    } else if (stream.isRTSP()) {
                        if (stream.getComm().equals("GS")) {
                            commandAudio = plugins.getProperty("GSRTSPaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                        } else {
                            commandAudio = plugins.getProperty("AVRTSPaudio").replaceAll("  ", " ");
                        }
                    } else if (stream.isRTMP()) {
                        if (stream.getComm().equals("GS")) {
                            commandAudio = plugins.getProperty("GSRTMPaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                        } else {
                            commandAudio = plugins.getProperty("AVRTMPaudio").replaceAll("  ", " ");
                        }
                    } else {
                        if ("AV".equals(stream.getComm())) {
                            commandAudio = plugins.getProperty("AVaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                        } else {
                            commandAudio = plugins.getProperty("GSaudio").replaceAll("  ", " "); //Making sure there is no double spaces
                        }
                    }
                }
                if (commandVideo != null) {
                    commandVideo = setParameters(commandVideo);
                }
                if (commandAudio != null) {
                    commandAudio = setParameters(commandAudio);
                }
                System.out.println("CommandVideo: "+commandVideo);
                System.out.println("CommandAudio: "+commandAudio);
                File fileV=new File(userHomeDir + "/.webcamstudio/WSUVid" + iD + ".sh");
                File fileA=new File(userHomeDir + "/.webcamstudio/WSUAud" + iD + ".sh");

                FileOutputStream fosV;
                Writer dosV = null;
                FileOutputStream fosA;
                Writer dosA = null;
                try {
                    fosV = new FileOutputStream(fileV);
                    dosV = new OutputStreamWriter(fosV);
                    fosA = new FileOutputStream(fileA);
                    dosA = new OutputStreamWriter(fosA);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    dosV.write("#!/bin/bash\n");
                    dosV.write(commandVideo+"\n");
                    dosV.close();
                    dosA.write("#!/bin/bash\n");
                    dosA.write(commandAudio+"\n");
                    dosA.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                fileV.setExecutable(true);
                fileA.setExecutable(true);
                String batchVideoCommand = userHomeDir+"/.webcamstudio/WSUVid" + iD + ".sh";
                String batchAudioCommand = userHomeDir+"/.webcamstudio/WSUAud" + iD + ".sh";
                try {
                    if (stream.hasVideo()) {
                        processVideo.executeString(batchVideoCommand);
                    }
                    if (stream.hasAudio()) {
                        processAudio.executeString(batchAudioCommand);
                    }
                } catch (IOException | InterruptedException e) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }).start();
    }

    public void readCustom() {
        stopped = false;
        stopMe = false;
        final String iD = stream.getID().substring(0, 4);
        new Thread(new Runnable() {

            @Override
            public void run() {
                stream.setVideo(plugins.containsKey("video"));
                stream.setAudio(plugins.containsKey("audio"));
                stream.setFakeVideo(stream.hasVideo());
                stream.setFakeAudio(stream.hasAudio());
//                System.out.println("HasVideo: "+stream.hasVideo());
//                System.out.println("HasFakeVideo: "+stream.hasFakeVideo());
                capture = new Capturer(stream);
                if (stream.hasVideo()) {
                    videoPort = capture.getVideoPort();
                }
                if (stream.hasAudio()) {
                    audioPort = capture.getAudioPort();
                }
                String commandVideo = null;
                String commandAudio = null;
                if (stream.hasVideo()) {
                    commandVideo = plugins.getProperty("video").replaceAll("  ", " "); //Making sure there is no double spaces
                }
                if (stream.hasAudio()) {
                    commandAudio = plugins.getProperty("audio").replaceAll("  ", " "); //Making sure there is no double spaces
                }
                if (commandVideo != null) {
                    commandVideo = setParameters(commandVideo);
                }
                if (commandAudio != null) {
                    commandAudio = setParameters(commandAudio);
                }
                System.out.println("CommandVideo: "+commandVideo);
                System.out.println("CommandAudio: "+commandAudio);
                File fileV = new File(userHomeDir + "/.webcamstudio/WSCVid" + iD + ".sh");
                File fileA = new File(userHomeDir + "/.webcamstudio/WSCAud" + iD + ".sh");

                FileOutputStream fosV;
                Writer dosV = null;
                FileOutputStream fosA;
                Writer dosA = null;
                try {
                    fosV = new FileOutputStream(fileV);
                    dosV= new OutputStreamWriter(fosV);
                    fosA = new FileOutputStream(fileA);
                    dosA= new OutputStreamWriter(fosA);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    dosV.write("#!/bin/bash\n");
                    dosV.write(commandVideo+"\n");
                    dosV.close();
                    dosA.write("#!/bin/bash\n");
                    dosA.write(commandAudio+"\n");
                    dosA.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                fileV.setExecutable(true);
                fileA.setExecutable(true);
                String batchVideoCommand = userHomeDir+"/.webcamstudio/WSCVid" + iD + ".sh";
                String batchAudioCommand = userHomeDir+"/.webcamstudio/WSCAud" + iD + ".sh";
                try {
                    if (stream.hasVideo()) {
                        processVideo.executeString(batchVideoCommand);
                    } else {
                        processVideo = null;
                    }
                    if (stream.hasAudio()) {
                        processAudio.executeString(batchAudioCommand);
                    } else {
                        processAudio = null;
                    }
                } catch (IOException | InterruptedException e) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, e);
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
                stopped = false;
                stopMe = false;
                videoPort = exporter.getVideoPort();
                audioPort = exporter.getAudioPort();
                String command = plugins.getProperty(plugin).replaceAll("  ", " "); //Making sure there is no double spaces
                command = setParameters(command);
                System.out.println("Command Out: "+command);
                File file=new File(userHomeDir+"/.webcamstudio/"+"WSBro.sh");
                FileOutputStream fos;
                Writer dos = null;
                try {
                    fos = new FileOutputStream(file);
                    dos= new OutputStreamWriter(fos);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    dos.write("#!/bin/bash\n");
                    dos.write(command+"\n");
                    dos.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                file.setExecutable(true);
                String batchCommand = userHomeDir+"/.webcamstudio/"+"WSBro.sh";
                try {
                    if (stream.hasVideo()) {
                        processAudio = null;
                        processVideo.executeString(batchCommand);
                    } else if (stream.hasAudio()) {
                        processVideo = null;
                        processAudio.executeString(batchCommand);
                    }
                    //We don't need processAudio on export.  Only 1 process is required...
                } catch (IOException | InterruptedException e) {
                    Logger.getLogger(Capturer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }).start();

    }

    public void pause() //Author Martijn Courteaux Code
    {
        if (processVideo != null) {
            capture.vPause();
        }
        if (processAudio != null) {
            capture.aPause();
        }
    }

    public void play() {
        if (processVideo != null) {
            capture.vPlay();
        }
        if (processAudio != null) {
            capture.aPlay();
        }
    }

    public void stop() {
        stopMe = true;
        stopped = true;
        if (capture != null) {
            capture.abort();
            capture = null;
//            System.out.println(stream.getName()+" Capture Cleared ...");
        }
        if (exporter != null) {
            exporter.abort();
            exporter = null;
//            System.out.println(stream.getName()+" Export Cleared ...");
        }
        if (processVideo != null) {
            processVideo.destroy();
            processVideo = null;
            System.out.println(stream.getName()+" Video Cleared ...");
        }
        if (processAudio != null) {
            processAudio.destroy();
            processAudio = null;
            System.out.println(stream.getName()+" Audio Cleared ...");
        }
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

    public enum ACTION {
        CAPTURE, OUTPUT
    }
}
