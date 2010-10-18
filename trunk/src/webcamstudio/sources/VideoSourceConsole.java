/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

/**
 *
 * @author lgs
 */
public class VideoSourceConsole extends VideoSourceText {

    public VideoSourceConsole(String command) {
        location = command;
        customText = command;
        name = command;
        captureWidth = 320;
        captureHeight = 240;

    }

    public VideoSourceConsole(java.io.File script) {
        //TODO
    }

    @Override
    public void updateText(String command) {
        customText = command;
        location = command;
        name = command;
        if (isPlaying && customText.endsWith("\n")) {
            loadText();
        }
    }

    @Override
    protected void loadText() {
        String data = "";
        if (customText.length() > 0) {
            lines.clear();
            try {
                java.lang.Process p = Runtime.getRuntime().exec(customText.trim().split(" "));
                java.io.BufferedInputStream in = new java.io.BufferedInputStream(p.getInputStream());
                java.io.DataInput din = new java.io.DataInputStream(in);
                p.waitFor();
                while (in.available() > 0) {
                    data = din.readLine();
                    if (data != null) {
                        lines.add(data);
                        newTextLine(data);
                    }
                }
                in.close();
                p.destroy();
                din = null;
                p = null;

            } catch (Exception e) {
                error("Console Error  :" + e.getMessage());
            }
        }
    }

    public static void main(String args[]) {
        VideoSourceConsole v = new VideoSourceConsole("cal");
        //v.startSource();
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(VideoSourceConsole.class.getName()).log(Level.SEVERE, null, ex);
//        }
        v.stopSource();
        v = null;
    }
}
