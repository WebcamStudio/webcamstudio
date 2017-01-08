/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.streams;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import webcamstudio.channels.MasterChannels;
import webcamstudio.channels.transitions.Transition;
import webcamstudio.mixers.Frame;
import webcamstudio.mixers.MasterMixer;
import webcamstudio.mixers.PreviewFrameBuilder;
import webcamstudio.sources.effects.Effect;
/**
 *
 * @author patrick (modified by karl)
 *
 * Stream is the top-level interface used for different kinds of video or audio sources.
 * A set of active streams is maintained by MasterFrameBuilder, which
 * uses the streams' Callable interface to fetch new video and/or
 * audio data.
 */
public abstract class Stream implements Callable<Frame> {

    /* FIXME: Stream.getInstance(file) is an abstract factory for
     * producing different kinds of streams from a filename.
     * But the name getInstance() is usually used for accessing a
     * singleton or instance pool, not for constructing a new
     * instance.
     * This should probably be renamed. ---GEC */
    public static Stream getInstance(File file) {
        Stream stream = null;
        String ext = file.getName().toLowerCase().trim();

        if (ext.endsWith(".avi")
                || ext.endsWith(".ogg")
                || ext.endsWith(".ogv")
                || ext.endsWith(".mp4")
                || ext.endsWith(".m4v")
                || ext.endsWith(".mpg")
                || ext.endsWith(".divx")
                || ext.endsWith(".wmv")
                || ext.endsWith(".flv")
                || ext.endsWith(".mov")
                || ext.endsWith(".mkv")
                || ext.endsWith(".vob")) {
            stream = new SourceMovie(file);
        } else if (file.getAbsolutePath().toLowerCase().startsWith("/dev/video")) {
            stream = new SourceWebcam(file);
//        } else if (ext.endsWith(".jpg")
//                || ext.endsWith(".bmp")
//                || ext.endsWith(".jpeg")) {
//            stream = new SourceImageU(file);
        } else if (ext.endsWith(".png")
                || ext.endsWith(".jpg")
                || ext.endsWith(".bmp")
                || ext.endsWith(".jpeg")) {
            stream = new SourceImage(file);
        } else if (ext.endsWith(".gif")) {
            stream = new SourceImageGif(file);
        } else if (ext.endsWith(".mp3")
                || ext.endsWith(".wav")
                || ext.endsWith(".wma")
                || ext.endsWith(".m4a")
                || ext.endsWith(".mp2")) {
            stream = new SourceMusic(file);
        } else if (ext.endsWith(".wss")) {
            stream = new SourceCustom(file);
        } else if (ext.startsWith("/dev/video")) {
            stream = new SourceWebcam(file);
        }
        return stream;
    }

    private final MasterMixer mixer = MasterMixer.getInstance();
    protected String uuid = java.util.UUID.randomUUID().toString();
    protected int captureWidth = mixer.getWidth();
    protected int captureHeight = mixer.getHeight();
    protected int width = mixer.getWidth();
    protected int height = mixer.getHeight();
    protected int x = 0;
    protected int y = 0;
    protected int panelX = 0;
    protected int panelY = 0;
    protected boolean more = false;
    protected boolean sliders = false;
    protected float volume = 0.5f;
    protected int opacity = 100;
    protected int rate = mixer.getRate();
    protected int seek = 0;
    protected int zorder = 0;
    protected File file = null;
    protected String name = "Default";
    protected String url = null;
    protected int audioLevelLeft = 0;
    protected int audioLevelRight = 0;
    protected ArrayList<Effect> effects = new ArrayList<>();
    protected ArrayList<SourceChannel> channels = new ArrayList<>();
    protected String gsEffect = "";
    protected SourceChannel channel = new SourceChannel();

    protected boolean hasVideo=true;
    protected boolean hasFakeVideo=false;
    protected boolean hasFakeAudio=false;
    protected boolean needSeekCTRL=true;
    protected boolean hasAudio=true;
    protected boolean isIPCam=false;
    protected boolean isStillPicture=false;
    protected boolean isRTSP=false;
    protected boolean isRTMP = false;
    protected boolean loaded=false;
    protected int ADelay = 0;
    protected int VDelay = 0;
    protected String abitrate = "128";
    protected String vbitrate = "1200";
    protected int frequencyDVB = 0;
    protected int bandwidthDVB = 0;
    protected int chDVB = 0;
    protected int color = 0;
    protected boolean isATimer = false;
    protected boolean isACDown = false;
    protected boolean isPlayList = false;
    protected boolean isQRCode = false;
    protected String comm = "AV";
    protected boolean backFF = false;
    protected String webURL = null;
    protected String content = "";
    protected String fontName = "";
    protected String ptzBrand = "foscam";
    protected boolean protectedCam = false;
    protected boolean isOAudio = false;
    protected boolean isOVideo = false;
    protected boolean loop = false;
    protected String ipcUser = null;
    protected String ipcPWD = null;
    protected String chNameDVB = null;
    protected Frame nextFrame = null;
    public ArrayList<Transition> startTransitions = new ArrayList<>();
    public ArrayList<Transition> endTransitions = new ArrayList<>();
    Listener listener = null;
    protected String panelType = "Panel";
    protected String streamTime = "N/A";
    protected int duration = 0;
    protected String audioSource = "";
    protected String guid = "";
    protected boolean preView = false;
    protected boolean isPaused = false;

    protected Stream() {
        MasterChannels.getInstance().register(this);
    }

    public void addStartTransition(Transition t) {
        startTransitions.add(t);
    }

    public void addEndTransition(Transition t) {
        endTransitions.add(t);
    }

    public void removeStartTransition(Transition t) {
        startTransitions.remove(t);
    }

    public void removeEndTransition(Transition t) {
        endTransitions.remove(t);
    }

    public ArrayList<Transition> getStartTransitions() {
        return startTransitions;
    }

    public ArrayList<Transition> getEndTransitions() {
        return endTransitions;
    }

    public String getComm() {
        return comm;
    }

    public void setComm(String sComm) {
        this.comm = sComm;
    }

    public boolean getisPaused() {
        return isPaused;
    }

    public void setisPaused(boolean isP) {
        this.isPaused = isP;
    }

    public boolean getIsATimer() {
        return isATimer;
    }

    public void setIsATimer(boolean tTimer) {
        this.isATimer = tTimer;
    }

    public boolean getIsACDown() {
        return isACDown;
    }

    public void setIsACDown(boolean cDown) {
        this.isACDown = cDown;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int dur) {
        this.duration = dur;
    }

    public void setPlayList(boolean b) {
        isPlayList = b;
    }

    public boolean getPlayList() {
        return isPlayList;
    }

    public boolean getIsQRCode() {
        return isQRCode;
    }

    public void setIsQRCode(boolean tQRCode) {
        this.isQRCode = tQRCode;
    }

    public void setBackFF(boolean wasFF) {
        this.backFF = wasFF;
    }

    public boolean getBackFF() {
        return this.backFF;
    }

    public void setDVBChannelNumber(int chDVB) {
        this.chDVB = chDVB;
    }

    public void setDVBFrequency(int frequencyDVB) {
        this.frequencyDVB = frequencyDVB;
    }

    public void setDVBBandwidth(int bandwidthDVB) {
        this.bandwidthDVB = bandwidthDVB;
    }

    public int getDVBChannelNumber() {
        return chDVB;
    }

    public int getDVBFrequency() {
        return frequencyDVB;
    }

    public String getWebURL() {
        return webURL;
    }

    public void setWebURL(String webURL) {
        this.webURL = webURL;
    }

    public void setOnlyVideo(boolean setOVideo) {
        isOVideo = setOVideo;
    }

    public boolean isOnlyVideo() {
        return isOVideo;
    }

    public void setOnlyAudio(boolean setOAudio) {
        isOAudio = setOAudio;
    }

    public boolean isOnlyAudio() {
        return isOAudio;
    }

    public void setPreView(boolean setPre) {
        preView = setPre;
    }

    public boolean getPreView() {
        return preView;
    }

    public boolean getProtected() {
        return protectedCam;
    }

    public void setProtected(boolean prCam) {
        this.protectedCam = prCam;
    }

    public String getPtzBrand() {
        return ptzBrand;
    }

    public void setPtzBrand(String ptzB) {
        this.ptzBrand = ptzB;
    }

    public String getIPUser() {
        return ipcUser;
    }

    public void setIPUser(String ipUser) {
        this.ipcUser = ipUser;
    }

    public String getIPPwd() {
        return ipcPWD;
    }

    public void setIPPwd(String ipPWD) {
        this.ipcPWD = ipPWD;
    }

    public boolean getLoaded() {
        return loaded;
    }

    public void setLoaded(boolean sLoaded) {
        this.loaded = sLoaded;
    }

    public String getChName() {
        return chNameDVB;
    }

    public void setChName(String chName) {
        this.chNameDVB = chName;
    }

    public int getDVBBandwidth() {
        return bandwidthDVB;
    }

    /* SourceDesktop-specific operations
       The following data accessors are used only for
       SourceDesktop. The corresponding data members were moved to
       that class and the accessors ultimately should be as well.

       Probably the main obstacle to that move is in ProcessRender's
       command parameter substitution. That could be resolved by
       providing a more general mechanism for Stream classes to
       publish their supported command parameters.

       (Many of the accessors in Stream are specific to some child
       class; those should be moved out as well for the sake
       of clarity.)   ---GEC */
    public String getDesktopN() {
        return "";
    }

    public void setDesktopN(String desktopN) {
        throw new UnsupportedOperationException();
    }

    public int getDeskN() {
        return 0;
    }

    public void setDeskN(int desktopN) {
        throw new UnsupportedOperationException();
    }

    public int getDesktopX() {
        return 0;
    }

    public String getDesktopXid() {
        return "";
    }

    public void setDesktopXid(String dXid) {
        throw new UnsupportedOperationException();
    }

    public String getElementXid() {
        return "";
    }

    public void setElementXid(String eXid) {
        throw new UnsupportedOperationException();
    }

    public boolean getSingleWindow() {
        return false;
    }

    public void setSingleWindow(boolean sWin) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param desktopX the desktopX to set
     */
    public void setDesktopX(int desktopX) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the desktopY
     */
    public int getDesktopY() {
        return 0;
    }

    /**
     * @param desktopY the desktopY to set
     */
    public void setDesktopY(int desktopY) {
        throw new UnsupportedOperationException();
    }

    public int getDesktopEndX() {
        return 0;
    }

    public int getDesktopEndY() {
        return 0;
    }

    /**
     * @return the desktopW
     */
    public int getDesktopW() {
        return 0;
    }

    /**
     * @param desktopW the desktopW to set
     */
    public void setDesktopW(int desktopW) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the desktopH
     */
    public int getDesktopH() {
        return 0;
    }

    /**
     * @param desktopH the desktopH to set
     */
    public void setDesktopH(int desktopH) {
        throw new UnsupportedOperationException();
    }

    public void setWindowX(int windowX) {
        throw new UnsupportedOperationException();
    }

    public int getWindowX() {
        return 0;
    }

    /**
     * @param windowY
     */
    public void setWindowY(int windowY) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the desktopY
     */
    public int getWindowY() {
        return 0;
    }

    public int getWindowEndX() {
        return 0;
    }

    public int getWindowEndY() {
        return 0;
    }

    /**
     * @return the desktopW
     */
    public int getWindowW() {
        return 0;
    }

    /**
     * @param windowW
     */
    public void setWindowW(int windowW) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the desktopH
     */
    public int getWindowH() {
        return 0;
    }

    /**
     * @param windowH
     */
    public void setWindowH(int windowH) {
        throw new UnsupportedOperationException();
    }
    /* SourceDesktop-specific operations END */


    public void setLoop (boolean sLoop) {
        loop = sLoop;
    }

    public boolean getLoop () {
        return loop;
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public void updateStatus() {
        if (listener != null) {
            listener.sourceUpdated(this);
        }
    }

    public void updatePreview() {
        if (listener != null) {
            listener.updatePreview(this.getPreview());
        }
    }

    public void destroy() {
        stop();
        MasterChannels.getInstance().unregister(this);
    }

    public void updateContent(String content) {
    }

    public void updateLineContent(String content) {
    }

    public void updatePNG() {
    }

    public String getContent() {
        return content;
    }

    public void setFont(String f) {
//        fontName = f;
//        updateContent(content);
    }

    public String getFont() {
        return fontName;
    }

    public void setColor(int c) {
//        color = c;
//        updateContent(content);
    }

    public int getColor() {
        return color;
    }

    public abstract void read();

    public abstract void stop();

    public abstract void pause();

    public abstract void play();

    public abstract boolean needSeek();

    public abstract boolean isPlaying();

    public abstract BufferedImage getPreview();

    public abstract void readNext();

    public boolean hasAudio() {
        return hasAudio;
    }

    public boolean isStillPicture() {
        return isStillPicture;
    }

    public void setRTSP(boolean setRTSP) {
        isRTSP = setRTSP;
    }

    public boolean isRTSP() {
        return isRTSP;
    }

    public void setRTMP(boolean setRTMP) {
        isRTMP = setRTMP;
    }

    public boolean isRTMP() {
        return isRTMP;
    }

    public void unRegister() {
            PreviewFrameBuilder.unregister(this);
    }

    public void register() {
            PreviewFrameBuilder.register(this);
    }

    public void setIsStillPicture(boolean setIsStillPicture) {
        isStillPicture = setIsStillPicture;
    }

    public void setIsPlaying(boolean setIsPlaying) {
    }

    public boolean isIPCam() {
        return isIPCam;
    }

    public void setIsIPCam(boolean setIsIPCam) {
        isIPCam = setIsIPCam;
    }

    public void setHasAudio(boolean setHasAudio) {
        hasAudio = setHasAudio;
    }

    public void setHasVideo(boolean setHasVideo) {
        hasVideo = setHasVideo;
    }

    public boolean hasVideo() {
        return hasVideo;
    }

    public void setMore(boolean setMo) {
        more = setMo;
    }

    public boolean getMore() {
        return more;
    }

    public void setSliders(boolean setSl) {
        sliders = setSl;
    }

    public boolean getSliders() {
        return sliders;
    }

    public void setPanelType(String sPanelType) {
        panelType = sPanelType;
    }

    public String getPanelType() {
        return panelType;
    }

    public void setStreamTime(String sStreamTime) {
        streamTime = sStreamTime;
    }

    public String getStreamTime() {
        return streamTime;
    }

    public void setAudioSource(String sAS) {
        audioSource = sAS;
    }

    public String getAudioSource() {
        return audioSource;
    }

    public void setGuid(String sGid) {
        guid = sGid;
    }

    public String getGuid() {
        return guid;
    }

    public boolean needSeekCTRL() {
        needSeekCTRL = needSeek();
        return needSeekCTRL;
    }

    public boolean hasFakeVideo() {
        return hasFakeVideo;
    }

    public boolean hasFakeAudio() {
        return hasFakeAudio;
    }

    public void setVideo(boolean hasIt) {
        hasVideo=hasIt;
    }

    public void setFakeVideo(boolean hasIt) {
        hasFakeVideo=hasIt;
    }

    public void setFakeAudio(boolean hasIt) {
        hasFakeAudio=hasIt;
    }

    public void setAudio(boolean hasIt) {
        hasAudio = hasIt;
    }

    public int getAudioLevelLeft() {
        return audioLevelLeft;
    }

    public int getAudioLevelRight() {
        return audioLevelRight;
    }

    public void addChannel(SourceChannel sc) {
        channels.add(sc);
    }

    public void removeChannel(SourceChannel sc) {
        channels.remove(sc);
    }

    public void addChannelAt(SourceChannel sc, int y) {
        channels.set(y, sc);
    }

    public void removeChannelAt(int y) {
        channels.set(y,null);
    }

    public void selectChannel(String name) {
        for (SourceChannel sc : channels) {
            if (sc.getName().equals(name)) {
                sc.apply(this);
                break;
            }
        }
    }

    public ArrayList<SourceChannel> getChannels() {
        return channels;
    }

    public SourceChannel getChannel() {
        return channel;
    }

    public void setName(String n) {
        name = n;
    }

    public ArrayList<Effect> getEffects() {
        return effects;
    }

    public synchronized void setEffects(ArrayList<Effect> list) {
        effects = list;
    }

    public String getGSEffect() {
        return gsEffect;
    }

    public synchronized void setGSEffect(String gsFx) {
        gsEffect = gsFx;
    }

    public synchronized void addEffect(Effect e) {
        effects.add(e);
    }

    public synchronized void removeEffect(Effect e) {
        effects.remove(e);
        e.clearEffect(e);
    }

    public synchronized void applyEffects(BufferedImage img) {
        ArrayList<Effect> temp = new ArrayList<>();
        temp.addAll(effects);
        for (Effect e : temp) {
            e.applyEffect(img);
        }
    }

    protected void setAudioLevel(Frame f) {
        if (f != null) {
            byte[] data = f.getAudioData();
            if (data != null) {
                audioLevelLeft = 0;
                audioLevelRight = 0;
                int tempValue = 0;
                for (int i = 0; i < data.length; i += 4) {
                    tempValue = (data[i] << 8 & (data[i + 1])) / 256;
                    if (tempValue < 0) {
                        tempValue *= -1;
                    }
                    if (audioLevelLeft < tempValue) {
                        audioLevelLeft = tempValue;
                    }
                    tempValue = (data[i + 2] << 8 & (data[i + 3])) / 256;

                    if (tempValue < 0) {
                        tempValue *= -1;
                    }
                    if (audioLevelRight < tempValue) {
                        audioLevelRight = tempValue;
                    }
                }
                audioLevelLeft = (int) (audioLevelLeft * volume);
                audioLevelRight = (int) (audioLevelRight * volume);
            }
        }
    }

    public String getURL() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Frame getFrame() {
        return nextFrame;
    }

    public String getID() {
        return uuid;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File f) {
        file = f;
    }

    public void setZOrder(int z) {
        zorder = z;
    }

    public int getZOrder() {
        return zorder;
    }

    /**
     * @return the captureWidth
     */
    public int getCaptureWidth() {
        return captureWidth;
    }

    /**
     * @param captureWidth the captureWidth to set
     */
    public void setCaptureWidth(int captureWidth) {
        this.captureWidth = captureWidth;
    }

    /**
     * @return the captureHeight
     */
    public int getCaptureHeight() {
        return captureHeight;
    }

    /**
     * @param captureHeight the captureHeight to set
     */
    public void setCaptureHeight(int captureHeight) {
        this.captureHeight = captureHeight;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
//        System.out.println("Set " + this.getName() + " To = " + this.height);
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    public int getPanelY() {
        return panelY;
    }

    /**
     * @param y the y to set
     */
    public void setPanelY(int pY) {
        this.panelY = pY;
    }

    public int getPanelX() {
        return panelX;
    }

    /**
     * @param x the x to set
     */
    public void setPanelX(int pX) {
        this.panelX = pX;
    }

    /**
     * @return the volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * @return the opacity
     */
    public int getOpacity() {
        return opacity;
    }

    /**
     * @param opacity the opacity to set
     */
    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    /**
     * @return the rate
     */
    public int getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * @return the seek
     */
    public int getSeek() {
        return seek;
    }

    /**
     * @param seek the seek to set
     */
    public void setSeek(int seek) {
        this.seek = seek;
    }

    public void setVDelay(int VDealy) {
        this.VDelay = VDealy;
    }

    public void setADelay(int ADealy) {
        this.ADelay = ADealy;
    }

    public int getVDelay() {
        return VDelay;
    }

    public int getADelay() {
        return ADelay;
    }

    public String getAbitrate() {
        return abitrate;
    }

    public void setAbitrate(String sAbitRate) {
        abitrate = sAbitRate;
    }

    /**
     * @return the vbitrate
     */
    public String getVbitrate() {
        return vbitrate;
    }

    public void setVbitrate(String sVbitRate) {
        vbitrate = sVbitRate;
    }

    @Override
    public Frame call() throws Exception {
        readNext();
        updatePreview();
        return nextFrame;
    }

    public interface chListener {
        public void loadingPostOP();
    }

    public interface Listener {
        public void sourceUpdated(Stream stream);

        public void updatePreview(BufferedImage image);
    }
}
