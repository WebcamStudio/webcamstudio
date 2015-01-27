/*
 * Copyright (C) 2014 patrick
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package webcamstudio.remote;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcamstudio.components.ResourceMonitor;
import webcamstudio.components.ResourceMonitorLabel;

/**
 *
 * @author patrick
 */
public class WebRemote implements Runnable {

    private static int port = -1;
    private ServerSocket socket = null;
    private boolean stopServer = false;
    private Listener listener = null;
    protected boolean auth = false;

    public WebRemote(Listener l) {
        listener = l;
    }
    
    public static int getPort() {
        return port;
    }
    
    public void setPort(int sPort) {
        port = sPort;
    }

    public void start(){
        stopServer = false;
        new Thread(this).start();
    }
    public void stop() {
        stopServer = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(WebRemote.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private int findPort() {
        boolean found = false;
        while (!found) {
            try {
                socket = new ServerSocket(port);
                socket.close();
                found = true;
            } catch (IOException ex) {
                port += 1;
            }
        }
        return port;
    }

    private void Listen() {
        // open server socket
        stopServer = false;
        try {
            findPort();
            socket = new ServerSocket(port);
            // request handler loop
            while (!stopServer) {
                Socket connection = null;
                try {
                    // wait for request
//                    System.out.println("WebcamStudio Remote accepting connections on port " + port);
                    listener.listening("http://" + InetAddress.getLocalHost().getHostName() + ".local:" + port);
                    ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis() + 10000, "WebcamStudio Remote listening on port: " + WebRemote.getPort());
                    ResourceMonitor.getInstance().addMessage(label);
                    connection = socket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    PrintStream pout = new PrintStream(out);

                    // read first line of request (ignore the rest)
                    String request = in.readLine();
                    if (request == null) {
                        continue;
                    }
                    log(connection, request);
                    while (true) {
                        String misc = in.readLine();
                        if (misc == null || misc.length() == 0) {
                            break;
                        }
                    }

                    // parse the line
                    if (!request.startsWith("GET") || request.length() < 14
                            || !(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
                        // bad request
                        errorReport(pout, connection, "400", "Bad Request",
                                "Your browser sent a request that "
                                + "this server could not understand.");
                    } else {
                        String req = request.substring(4, request.length() - 9).trim();
                        if (req.contains("..")
                                || req.contains("/.ht") || req.endsWith("~")) {
                            // evil hacker trying to read non-wwwhome or secret file
                            errorReport(pout, connection, "403", "Forbidden",
                                    "You don't have permission to access the requested URL.");
                        } else {
                            if (req.contains("?")) {
                                req = req.split("\\?")[0];
                            }
//                            System.out.println("Requesting /" + req);
                            if (req.equals("/run") && auth){
                                System.out.println("Requesting Remote Start...");
                                listener.requestStart();
                            } else if (req.equals("/stop") && auth){
                                System.out.println("Requesting Remote Stop...");
                                listener.requestStop();
                            } else if (req.equals("/reset" ) && auth){
                                System.out.println("Requesting Remote Reset...");
                                listener.requestReset();
                            } else if (req.equals("/logout") && auth) {
                                auth = false;    
                            } else if (req.contains("/j_security_check")){
                                System.out.println("Requesting Remote Login...");
                                req = listener.requestlogin(request);
                                auth = req.equals("/run") || req.equals("/stop");
                            } else {
                                req = "/login";
                            }
                            if (req.equals("/")) {
                                req = "/login";
                            }
                            String path = ""; 
                            path = "/webcamstudio/remote/resources" + req + ".html";
//                            System.out.println("Path: "+path);
                            try {
                                // send file
                                InputStream file = this.getClass().getResourceAsStream(path);
                                pout.print("HTTP/1.0 200 OK\r\n"
                                        + "Content-Type: text/html\r\n"
                                        + "Date: " + new Date() + "\r\n"
                                        + "Server: WebcamStudio 0.66\r\n\r\n");
                                sendFile(file, out); // send raw file 
                                log(connection, "200 OK");
                            } catch (Exception e) {
                                // file not found
                                errorReport(pout, connection, "404", "Not Found",
                                        "The requested URL was not found on this server.");
                            }

                        }
                    }
                    out.flush();
                } catch (IOException e) {
                    //Server was stoppped...
                    if (!stopServer){
                        System.err.println(e.getMessage());
                    }
                    
                }
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        } catch (IOException e) {
            port = -1;
            System.err.println("Could not start server: " + e);
        }
        listener.listening("");
        

    }

    private void log(Socket connection, String msg) {
//        System.err.println(new Date() + " [" + connection.getInetAddress().getHostAddress()
//                + ":" + connection.getPort() + "] " + msg);
    }

    private void errorReport(PrintStream pout, Socket connection,
            String code, String title, String msg) {
        pout.print("HTTP/1.0 " + code + " " + title + "\r\n"
                + "\r\n"
                + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n"
                + "<TITLE>" + code + " " + title + "</TITLE>\r\n"
                + "</HEAD><BODY>\r\n"
                + "<H1>" + title + "</H1>\r\n" + msg + "<P>\r\n"
                + "<HR><ADDRESS>WebcamStudio 0.66 at "
                + connection.getLocalAddress().getHostName()
                + " Port " + connection.getLocalPort() + "</ADDRESS>\r\n"
                + "</BODY></HTML>\r\n");
        log(connection, code + " " + title);
    }

    private String guessContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) {
            return "text/html";
        } else if (path.endsWith(".txt") || path.endsWith(".java")) {
            return "text/plain";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".class")) {
            return "application/octet-stream";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "text/plain";
        }
    }

    private void sendFile(InputStream resource, OutputStream out) {
        try {
            byte[] buffer = new byte[1000];
            while (resource.available() > 0) {
                out.write(buffer, 0, resource.read(buffer));
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @Override
    public void run() {
        Listen();
    }
}


