/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import webcamstudio.InfoListener;

/**
 *
 * @author patrick
 */
public class HTTPServer implements InfoListener {

    private int remoteStreamPort = 4888;
    private int port = 8888;
    private ServerSocket socket = null;
    private boolean stopMe = false;
    private String password = "";
    private InfoListener listener = null;

    public HTTPServer(int serverPort) {
        port = serverPort;
        start();
    }

    public HTTPServer(int serverPort, int remoteStreamPort) {
        port = serverPort;
        this.remoteStreamPort = remoteStreamPort;
        start();

    }

    public void setListener(InfoListener l) {
        listener = l;
    }

    public void setPassword(String p) {
        password = p;
    }

    private void start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    socket = new ServerSocket(port);
                    socket.setSoTimeout(1000);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    stopMe = true;
                }
                info("Waiting for HTTP connections...");

                while (!stopMe) {
                    Socket connection = null;
                    try {
                        connection = socket.accept();
                        connection.setSoTimeout(10000);

                        sendStream(connection);
                    } catch (SocketTimeoutException ex) {
                        continue;
                    } catch (IOException ex) {
                        stopMe = true;
                    }
                }
            }
        }).start();
    }

    private void sendStream(final Socket connection) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedReader in = null;
                try {
                    boolean passwordIsGood = true;
                    passwordIsGood = !(password.length() > 0);
                    info("Connection from " + connection.getRemoteSocketAddress().toString());
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    PrintStream pout = new PrintStream(out);
                    String request = in.readLine();
                    while (true) {
                        String misc = in.readLine();
                        if (misc == null || misc.length() == 0) {
                            break;
                        }
                    }
                    if (!request.startsWith("GET") || request.length() < 14 || !(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
                        System.out.println("Bad request");
                    } else {
                        String req = request.substring(4, request.length() - 9).trim();
                        info("Request: " + req);
                        if (password.length() > 0 && req.indexOf("password=") != -1) {
                            info("Password protection activated...");
                            //Comparing passwords
                            int index = req.indexOf("password=");
                            System.out.println(req.indexOf(password, index + 9));
                            if (req.indexOf(password, index + 9) == (index + 9)) {
                                //Password is good
                                info("Password accepted");
                                passwordIsGood = true;
                            } else {
                                info("Password rejected");
                                passwordIsGood = false;
                            }
                        }
                        if (passwordIsGood) {
                            if (req.startsWith("/video.ogg")) {
                                pout.print("HTTP/1.0 200 OK\r\n" + "Content-Type: " + "'video/ogg" + "\r\n" + "Date: " + new Date() + "\r\n" + "Server: WS4GL\r\n\r\n");
                                out.flush();
                                //Send stream...
                                Socket tcpStream = null;
                                info("Sending stream to " + connection.getRemoteSocketAddress().toString());
                                try {
                                    tcpStream = new Socket("127.0.0.1", remoteStreamPort);
                                    byte[] buffer = new byte[10000];
                                    int count;
                                    while (!stopMe) {
                                        count = tcpStream.getInputStream().read(buffer);
                                        out.write(buffer, 0, count);
                                        out.flush();
                                    }
                                } catch (UnknownHostException ex) {
                                    ex.printStackTrace();
                                } catch (IOException ex) {
                                    info("Connection lost from " + connection.getRemoteSocketAddress().toString());
                                }
                                if (tcpStream != null) {
                                    try {
                                        tcpStream.close();
                                    } catch (IOException ex) {
                                    }
                                }
                                try {
                                    out.close();
                                } catch (IOException ex) {
                                }
                            } else {
                                pout.print("HTTP/1.0 404 WS4GL File Not Found \r\n" + "\r\n" + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" + "<TITLE>404 WS4GL File Not Found</TITLE>\r\n" + "</HEAD><BODY>\r\n" + "<H1>WS4GL File Not Found</H1>\r\nThis file does not exists<P>\r\n" + "<HR><ADDRESS>WS4GL at " + connection.getLocalAddress().getHostName() + " Port " + connection.getLocalPort() + "</ADDRESS>\r\n" + "</BODY></HTML>\r\n");
                                out.flush();
                                pout.close();
                                out.close();
                            }
                        } else {
                            pout.print("HTTP/1.0 403 WS4GL Permission Denied \r\n" + "\r\n" + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" + "<TITLE>403 WS4GL Permission Denied</TITLE>\r\n" + "</HEAD><BODY>\r\n" + "<H1>WS4GL Permission Denied</H1>\r\nYou do not have access to this stream...<P>\r\n" + "<HR><ADDRESS>WS4GL at " + connection.getLocalAddress().getHostName() + " Port " + connection.getLocalPort() + "</ADDRESS>\r\n" + "</BODY></HTML>\r\n");
                            out.flush();
                            pout.close();
                            out.close();
                        }
                        out = null;
                        pout = null;
                    }
                } catch (IOException ex) {
                } finally {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }).start();

    }

    public void stop() {
        stopMe = true;
        socket = null;
    }

    public static void main(String[] args) {
        new HTTPServer(8888).setPassword("patrick");
    }

    @Override
    public void info(String info) {
        System.out.println("HTTPServer Info: " + info);
        if (listener != null) {
            listener.info(info);
        }
    }

    @Override
    public void error(String message) {
        if (listener != null) {
            listener.error(message);
        }
    }

    @Override
    public void newTextLine(String line) {
    }
}
