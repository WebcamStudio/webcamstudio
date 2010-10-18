/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 * @author pballeux
 */
public class StreamChannelReader implements webcamstudio.io.BufferReader {

    private long pos = 0;
    private DataInputStream din = null;
    private boolean streamClosed = false;

    public StreamChannelReader(java.io.InputStream in) {
        din = new DataInputStream(in);
    }
    public StreamChannelReader(int port) {
        try {
            Socket socket = new Socket("127.0.0.1", port);
            din = new DataInputStream(socket.getInputStream());
        } catch (UnknownHostException ex) {
            Logger.getLogger(StreamChannelReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StreamChannelReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isCLosed() {
        return streamClosed;
    }

    @Override
    public long size() {
        if (streamClosed) {
            return 0;
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public long position() {
        if (streamClosed) {
            return 0;
        } else {
            return pos;
        }
    }

    @Override
    public void position(long position) {
        try {
            din.skip(position - pos);
            pos = position;
        } catch (IOException ex) {
            streamClosed = true;
        }
    }

    @Override
    public ChannelBuffer read(int size) {
        return ChannelBuffers.wrappedBuffer(readBytes(size));
    }

    @Override
    public byte[] readBytes(int size) {
        byte[] bs = new byte[size];

        try {
            din.readFully(bs);
        } catch (IOException ex) {
            streamClosed = true;
        }
        return bs;
    }

    @Override
    public int readInt() {
        try {
            return din.read();
        } catch (IOException ex) {
            streamClosed = true;
            return -1;

        }
    }

    @Override
    public long readUnsignedInt() {
        return read(4).readUnsignedInt();
    }

    @Override
    public void close() {
        try {
            din.close();
        } catch (IOException ex) {
        }
    }
}
