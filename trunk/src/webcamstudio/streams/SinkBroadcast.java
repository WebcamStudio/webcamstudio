/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import static webcamstudio.WebcamStudio.outFMEbe;
import webcamstudio.externals.FME;
import webcamstudio.externals.ProcessRenderer;
import webcamstudio.mixers.MasterMixer;

/**
 *
 * @author patrick (modified by karl)
 */
public class SinkBroadcast extends Stream {

    private ProcessRenderer capture = null;
    private FME fme = null;
    private boolean isPlaying = false;
    private String standard = "STD";
    public SinkBroadcast(FME fme) {
        this.fme=fme;
        name=fme.getName();
        url = fme.getUrl()+"/"+fme.getStream();
        if (outFMEbe == 0){
            this.setComm("FF");
        } else if (outFMEbe == 1) {
            this.setComm("AV");
        } else if (outFMEbe == 2) {
            this.setComm("GS");
        }
    }
    @Override
    public String getName(){
        return name;
    }
    @Override
    public void read() {
        isPlaying=true;
        rate = MasterMixer.getInstance().getRate();
        captureWidth = MasterMixer.getInstance().getWidth();
        captureHeight = MasterMixer.getInstance().getHeight();
        switch (name.toLowerCase()) {
            case "red5":
                if (standard.equals("STD")) {
                    capture = new ProcessRenderer(this,fme,"broadcastR5");
                } else {
                    capture = new ProcessRenderer(this,fme,"broadcastR5HQ");
                }
                break;
            case "icecast":
                if (standard.equals("STD")) {
                    capture = new ProcessRenderer(this,fme,"iceCast");
                } else {
                    capture = new ProcessRenderer(this,fme,"iceCastHQ");
                }
                break;
            default:
                if (standard.equals("STD")) {
                    capture = new ProcessRenderer(this,fme,"broadcast");
                } else {
                    capture = new ProcessRenderer(this,fme,"broadcastHQ");
                }
                break;
        }
        capture.writeCom();
    }

    @Override
    public void pause() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void stop() {
        isPlaying=false;
        if  (capture!=null){
            capture.stop();
            capture=null;
        }
        if (this.getBackFF()){
            this.setComm("FF");
        }
    }
    @Override
    public boolean needSeek() {
            return needSeekCTRL=false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
        }

    @Override
    public BufferedImage getPreview() {
        return null;
    }
    public void setStandard(String gStandard) {
        standard = gStandard;
    }
    
    public String getStandard() {
        return standard;
    }
    
    @Override
    public boolean hasAudio() {
        return true;
    }

    @Override
    public boolean hasVideo() {
        return true;
    }

    @Override
    public void readNext() {
        
    }

    @Override
    public void play() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
