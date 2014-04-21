package f00f.net.irc.martyr.dcc;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A DccSendHandler manages a DCC "send" (to us) file transfer.  If
 * you wish to use this class to handle a DCC session, provide an
 * implementation for getStreamForReceive in a subclass.
 * 
 * @see f00f.net.irc.martyr.dcc.AutoDccSetup#getDccSendHandler
 * @see #getStreamForReceive
 */
public class DccSendHandler extends Thread
{
    private static final int DEFAULT_BUF_SIZE = 5000;
    private static int count = 0;

    private static synchronized int nextCount() {
        return count++;
    }
    

    private final Socket socket_;
	private final String filename_;
	private final int filesize_;

	public DccSendHandler(Socket socket, String filename, int filesize)
        {
            super("dcchandler-" + nextCount());
            this.socket_ = socket;
            this.filename_ = filename;
            this.filesize_ = filesize;
        }

	/**
     * A call to this method means that a connection has been established
     * and processing the file should commence.  Note that normally
     * one would not override this method, but rather, override
     * getStreamForReceive.
     *
     * @see #getStreamForReceive
     * @param socket Socket ove which send will be handled
     * @param filename Name of file to be sent
     * @param filesize Size of file to send
     * @throws IOException if connection could not be established
     */
    protected void handleDccSend( Socket socket, String filename, int filesize) throws IOException
    {
        OutputStream dest = getStreamForReceive( filename, filesize );
        
        
        
        // read/write as per protocol
        InputStream incoming = socket.getInputStream();
        OutputStream outgoing  = socket.getOutputStream();
        
        // The generally accepted packet size is 1024 bytes.  However,
        // this is arbitrary, therefore we will make a somewhat larger
        // buffer.
        byte[] buffer = new byte[DEFAULT_BUF_SIZE];
        byte[] intbuf = new byte[4];
        
        int totalin = 0;
        try
        {
            while( true )
            {
                int recvd = incoming.read( buffer );
                //System.out.println("        read: " + recvd);
                
                if( recvd < 0 )
                {
                    // We are done
                    break;
                }
                
                dest.write( buffer, 0, recvd );
                totalin += recvd;
                //System.out.println("         totalin: " + totalin);
                
                intbuf[0] = (byte)((totalin >>> 24) & 0xFF);
                intbuf[1] = (byte)((totalin >>> 16) & 0xFF);
                intbuf[2] = (byte)((totalin >>>  8) & 0xFF);
                intbuf[3] = (byte)((totalin) & 0xFF); // (byte)((totalin >>>  0) & 0xFF)
                /* System.out.println( "Sending: " + intbuf[0] + ","
                + intbuf[1] + "," + intbuf[2] + "," + intbuf[3] ); */
                outgoing.write(intbuf);
                outgoing.flush();
            }
        }
        finally
        {
            dest.close();
        }
        
        
        socket.close();
    }
	
	/**
     * This method should return an output stream that a file can be
     * placed into.  Default implementation throws a
     * FileNotFoundException.  This is the only method you must
     * override (in this class) to provide fully functional DCC file transfers.
     *
     * @param filename Name of file to be received
     * @param filesize Size of file to be received
     * @throws IOException of connection could not be established
     * @return OutputStream for incoming transfer
     */
	protected OutputStream getStreamForReceive( String filename, int filesize)
		throws IOException
        {
            throw new FileNotFoundException("Refusing to write '" + filename + "'" );
        }

    @Override
	public void run(  )
        {
            try
            {
                handleDccSend( socket_, filename_, filesize_ );
            }
            catch( IOException ioe )
            {
                ioe.printStackTrace();
            }
        }
	
}

