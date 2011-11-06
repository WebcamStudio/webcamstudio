/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.ffmpeg;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author patrick
 */
public class TCPAudioListener {

    private int frequency = 0;
    private int channels = 0;
    private int bits = 0;
    private byte[] data = null;
    private ServerSocket server = null;
    private boolean stopMe = false;
    private int port = 0;
    private byte[] audioBuffer = null;

    public TCPAudioListener(int freq, int channels, int bits) {
        this.frequency = freq;
        this.channels = channels;
        this.bits = bits;
        try {
            server = new ServerSocket(0);
            server.setSoTimeout(1000);
            port = server.getLocalPort();
            System.out.println("Port used is " + port);
        } catch (IOException ex) {
            Logger.getLogger(TCPAudioListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                read();
            }
        }).start();

    }

    public void shutdown() {
        stopMe = true;
    }

    private void read() {
        while (!stopMe) {
            try {
                Socket conn = server.accept();
                java.io.DataInput din = new java.io.DataInputStream(conn.getInputStream());
                while (!stopMe) {
                    data = new byte[bits / 8 * channels * frequency];
                    din.readFully(data);
                    audioBuffer = data;
                }
            } catch (SocketTimeoutException timeout) {
                System.out.println("Waiting...");
                continue;
            } catch (IOException ex) {
                //We lost the connection...
                stopMe = true;
            }
        }
        audioBuffer = null;
        try {
            System.out.println("Quitting...");
            server.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getPort() {
        return port;
    }

    public byte[] getAudio() {
        return audioBuffer;
    }

    public static void main(String[] args) {
        TCPAudioListener audio = new TCPAudioListener(44100, 2, 16);
        // Open an audio input stream.
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        while (audio.getAudio() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TCPAudioListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open();
            speaker.start();
            
            byte[] data = audio.getAudio();
            while (data != null) {
                speaker.write(data, 0, data.length);
                data = audio.getAudio();
            }
            speaker.close();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(TCPAudioListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
