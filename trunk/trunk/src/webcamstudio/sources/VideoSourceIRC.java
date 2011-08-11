/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.util.Collection;
import f00f.net.irc.martyr.*;
import f00f.net.irc.martyr.clientstate.ClientState;
import f00f.net.irc.martyr.services.AutoRegister;
import f00f.net.irc.martyr.services.AutoReconnect;
import f00f.net.irc.martyr.services.AutoJoin;
import f00f.net.irc.martyr.GenericAutoService;
import f00f.net.irc.martyr.InCommand;
import f00f.net.irc.martyr.State;
import f00f.net.irc.martyr.commands.ActionCtcp;
import f00f.net.irc.martyr.commands.KickCommand;
import f00f.net.irc.martyr.commands.MessageCommand;
import f00f.net.irc.martyr.commands.QuitCommand;
import f00f.net.irc.martyr.commands.RawCommand;


import java.util.StringTokenizer;
import f00f.net.irc.martyr.clientstate.Channel;
import f00f.net.irc.martyr.commands.PingCommand;
import f00f.net.irc.martyr.commands.PongCommand;
import java.awt.Graphics2D;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import webcamstudio.components.PreciseTimer;

/**
 *
 * @author pballeux
 */
public class VideoSourceIRC extends VideoSource {

    public VideoSourceIRC(String address, int port, String channel, String nick) {
        location = address + ":" + port + ":" + channel;
        name = location;
        this.nick = nick;
        frameRate = 1;

    }

    public VideoSourceIRC() {
        location = "irc.freenode.net" + ":" + "6667" + ":" + "##WS4GL";
        name = location;
        this.nick = "webcamstudio";
        frameRate = 1;
        captureWidth = 320;
        captureHeight = 240;
    }

    public void setChatroom(String address, String port, String channel, String nick) {
        location = address + ":" + port + ":" + channel;
        name = location;
        this.nick = nick;
    }

    private void connect() {
        if (nick == null || nick.length() == 0) {
            nick = "webcamstudio";
        }
        String server = location.split(":")[0];
        int port = new Integer(location.split(":")[1]).intValue();
        String channel = location.split(":")[2];
        state = new ClientState();
        state.addChannel(channel);
        connection = new IRCConnection(state);
        new AutoRegister(connection, nick, nick + "_" + new java.util.Random().nextInt(), nick + "_" + new java.util.Random().nextInt(), password);
        autoReconnect = new AutoReconnect(connection);
        new AutoJoin(connection, channel, null);
        messageMonitor = new MessageMonitor(this);
        autoReconnect.go(server, port);
        mainChannel = state.getChannel(channel);
        if (connection.getState() == State.UNCONNECTED) {
            error("IRC: " + name + " (Could not connect)");
            disconnect();
        } else {
            info("Connected");
        }
    }

    private void disconnect() {
        if (connection != null) {
            messageMonitor.disable();
            messageMonitor.removeMe();
            messageMonitor = null;
            connection.stop();
            connection.shutdown(0);
            if (autoReconnect != null) {
                autoReconnect.disable();
                autoReconnect = null;
            }
            connection.disconnect();
            state = null;
            connection = null;
        }
        mainChannel = null;
        quit = false;
    }

    private String drawEmote(Graphics2D buffer, String line, int x, int y) {
        String retValue = "";
        String[] words = line.split(" ");
        for (String word : words) {
            if (emotes.isSmyley(word)) {
                ImageIcon img = emotes.getSmiley(word);
                int length = buffer.getFontMetrics().stringWidth(retValue);
                buffer.drawImage(img.getImage(), x + length - 3, y - fontSize + 2, x + length + charWidth + 3, y + 2, 0, 0, img.getIconWidth(), img.getIconHeight(), null);
                retValue += "  ";
            } else {
                retValue += word + " ";
            }
        }
        return retValue;
    }

    @Override
    public void startSource() {
        isPlaying = true;
        connect();

        Thread t = new Thread(new Runnable() {

            public void run() {
                long timestamp = 0;
                if (outputWidth == 0 || outputHeight == 0) {
                    outputWidth = 320;
                    outputHeight = 240;
                }

                tempimage = graphicConfiguration.createCompatibleImage(outputWidth, outputHeight, java.awt.image.BufferedImage.TRANSLUCENT);

                buffer = tempimage.createGraphics();
                int index = 0;
                int y = fontSize;
                int x = 0;
                int lineIndex = index;
                stopMe = false;
                String text = "";
                String emoteLine = "";
                int interLine = 0;
                java.awt.Color lastColor = foregroundColor;
                while (!stopMe) {
                    timestamp = System.currentTimeMillis();
                    try {
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
                        buffer.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                        buffer.setFont(new java.awt.Font(fontName, java.awt.Font.PLAIN, fontSize));

                        buffer.setFont(new java.awt.Font(fontName, java.awt.Font.PLAIN, fontSize));
                        charWidth = buffer.getFontMetrics().charWidth('M');
                        buffer.clearRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, backgroundOpacity));
                        buffer.setColor(backgroundColor);
                        buffer.fillRect(0, 0, tempimage.getWidth(), tempimage.getHeight());
                        buffer.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1F));
                        //draw Line
                        interLine = 0;
                        index = 0;
                        x = 0;
                        y = fontSize;
                        lineIndex = index;
                        lastColor = foregroundColor.brighter();
                        while (y < (tempimage.getHeight() + fontSize) && lines.size() > 0 && lineIndex < lines.size()) {

                            emoteLine = drawEmote(buffer, lines.get(lineIndex), x, y);
                            if (emoteLine.startsWith("> ") || emoteLine.startsWith("* ")) {
                                if (!lastColor.equals(foregroundColor.brighter())) {
                                    lastColor = foregroundColor.brighter();
                                } else {
                                    lastColor = foregroundColor.darker();
                                }
                            }
                            buffer.setColor(lastColor);
                            buffer.drawString(emoteLine, x, y);
                            lineIndex++;
                            y += fontSize;
                        }

                        applyShape(tempimage);
                        applyEffects(tempimage);
                        image = tempimage;
                        PreciseTimer.sleep(timestamp, 1000 / frameRate);

                    } catch (Exception e) {
                        error("IRC Error  :" + e.getMessage());
                    }
                }
                buffer.dispose();
                image = null;
                disconnect();
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private void say(String msg) {
        if (mainChannel != null) {
            connection.sendCommand(new MessageCommand(mainChannel.getName(), msg));
            updateLines(state.getNick().getNick(), msg, false, false);
        }
    }

    private void sayAction(String msg) {
        connection.sendCommand(new ActionCtcp(mainChannel.getName(), msg));
    }

    protected IRCConnection getConnection() {
        return connection;
    }

    public void addIRCListener(VideoSourceIRCListener l) {
        ircListeners.add(l);
    }

    public void removeIRCListener(VideoSourceIRCListener l) {
        ircListeners.remove(l);
    }

    public void updateLines(String from, String msg, boolean isAction, boolean isPrivate) {
        String line = "> " + from + " : " + msg;
        if (isAction) {
            line = "* " + from.toUpperCase() + " " + msg;
            newTextLine(line);
            addNewLine("* " + from, msg);
        } else {
            line = "> " + from + " : " + msg;
            addNewLine("> " + from, msg);
            newTextLine(line);
        }
        if (!isPrivate) {
            String temp = "";
            String[] words = line.split(" ");

            for (String word : words) {
                if (word.trim().length() > 0) {
                    if (buffer.getFontMetrics().stringWidth(temp + word) >= captureWidth) {
                        this.lines.add(temp.trim());
                        temp = "";
                    }
                    temp += word + " ";
                }
            }
            if (temp.trim().length() > 0) {
                this.lines.add(temp.trim());
            }
        }
        while (lines.size() > 0 && (lines.size() * fontSize) > captureHeight) {
            lines.removeElementAt(0);
        }
    }

    protected void incomingMessage(MessageCommand msg) {

        String sayCommand = "say ";
        String meCommand = "me ";
        String kickCommand = "kick ";
        String nickCommand = "nick ";
        String quitCommand = "quit ";
        String opCommand = "op";
        String setSecret = "setsecret";
        String showSecret = "showsecret";

        String message = msg.getMessage();

        StringTokenizer tokenizer = new StringTokenizer(message);
        // First, remove the command.
        String commandStr = tokenizer.nextToken().toLowerCase();

        if (commandStr.equals(opCommand)) {
            if (tokenizer.countTokens() != 2) {
                connection.sendCommand(
                        new MessageCommand(msg.getSource(),
                        "Incorrect number of parameters: " + message));
                return;
            }


            String personToOp = tokenizer.nextToken();
            String maybeSecret = tokenizer.nextToken();

            if (maybeSecret.equals(secret)) {
                System.out.println("Opping " + personToOp);
                connection.sendCommand(
                        new RawCommand("MODE " + channelName + " +o " + personToOp));
            } else {
                connection.sendCommand(
                        new MessageCommand(msg.getSource(),
                        "Bad secret." + message));
            }
        } else if (commandStr.equals(setSecret)) {
            if (tokenizer.countTokens() != 2) {
                connection.sendCommand(
                        new MessageCommand(msg.getSource(),
                        "Incorrect number of parameters: " + message));
                return;
            }
            if (tokenizer.nextToken().equals(secret)) {
                secret = tokenizer.nextToken();
                connection.sendCommand(
                        new MessageCommand(msg.getSource(),
                        "Secret has been set to " + secret));
            } else {
                connection.sendCommand(
                        new MessageCommand(msg.getSource(),
                        "Bad secret f00! Try using the '"
                        + showSecret + "' command."));
            }
        } else if (commandStr.equals(showSecret)) {
            System.out.println("Show secret requested.  Secret is: " + secret);
            connection.sendCommand(
                    new MessageCommand(msg.getSource(),
                    "Secret has been output on standard out."));
        } else if (message.toLowerCase().startsWith(quitCommand)) {
            // we have a 'quit' command
            String comment = getParameter(message, quitCommand);

            autoReconnect.disable();
            quit = true;

            connection.sendCommand(new QuitCommand(comment));
        } else if (message.toLowerCase().startsWith(sayCommand)) {
            // We have a 'say' command
            String sayString = getParameter(message, sayCommand);
            say(sayString);
        } else if (message.toLowerCase().startsWith(meCommand)) {
            // we have a 'me' command
            String sayString = getParameter(message, meCommand);
            sayAction(sayString);
        } else if (message.toLowerCase().startsWith(kickCommand)) {
            // we have a 'kick' command
            long now = System.currentTimeMillis();
            final long delay = 60000;

            if (now - kickTime < delay) {
                connection.sendCommand(
                        new MessageCommand(msg.getSource(), "Woa, gotta wait " + ((delay - now + kickTime) / 1000) + " seconds before I can kick again."));
                return;
            }

            kickTime = now;

            String comment = message.substring(sayCommand.length(), message.length()).trim();
            String nickToKick;
            int space = comment.indexOf(' ');
            if (space >= 0) {
                nickToKick = comment.substring(0, space);
                comment = comment.substring(space + 1, comment.length());
            } else {
                nickToKick = comment;
                comment = "";
            }
            error("Kicking: " + nickToKick + " Comment: " + comment);

            connection.sendCommand(new KickCommand(mainChannel.getName(), nickToKick, comment));
        } else {
            // Umm..
            connection.sendCommand(
                    new MessageCommand(msg.getSource(), "Bad syntax: " + message));
        }
    }

    private String getParameter(String raw, String command) {
        return raw.substring(command.length(), raw.length());
    }

    public boolean canUpdateSource() {
        return false;
    }

    @Override
    public void stopSource() {
        stopMe = true;
        image = null;
        isPlaying = false;
    }

    public boolean hasText() {
        return true;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void pause() {
        //
    }

    @Override
    public void play() {
        //
    }

    public void sendLine(String text) {
        say(text);
    }

    public void addNewLine(String from, String text) {
        for (VideoSourceIRCListener l : ircListeners) {
            l.newLine(this, from, text);
        }
    }

    public ClientState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "IRC: " + name;
    }
    protected java.util.Vector<String> lines = new java.util.Vector<String>();
    private IRCConnection connection = null;
    private AutoReconnect autoReconnect = null;
    private ClientState state = null;
    private String secret = "one";
    private long kickTime = 0; // The last time kick was used.
    private Channel mainChannel;
    private String channelName;
    private boolean quit = false;
    private MessageMonitor messageMonitor = null;
    private int charWidth = 12;
    private java.util.Vector<VideoSourceIRCListener> ircListeners = new java.util.Vector<VideoSourceIRCListener>();
    private java.awt.Graphics2D buffer = null;
    private Emotes emotes = new Emotes();

    @Override
    public Collection<JPanel> getControls() {
        java.util.Vector<JPanel> list = new java.util.Vector<JPanel>();
        list.add(new webcamstudio.controls.ControlIRC(this));
        list.add(new webcamstudio.controls.ControlShapes(this));
        list.add(new webcamstudio.controls.ControlEffects(this));
        list.add(new webcamstudio.controls.ControlActivity(this));
        list.add(new webcamstudio.controls.ControlSpeech(this));
        return list;
    }
}

class MessageMonitor extends GenericAutoService {

    private VideoSourceIRC source;

    public MessageMonitor(VideoSourceIRC source) {
        super(source.getConnection());
        this.source = source;
        enable();
    }

    public void removeMe() {
        source = null;
        super.connection = null;
    }

    public void updateCommand(InCommand command) {
        System.out.println(command.getSourceString());
        if (command instanceof MessageCommand) {
            MessageCommand msg = (MessageCommand) command;

            source.updateLines(msg.getSource().getNick(), msg.getMessage(), command.getSourceString().indexOf("ACTION ") != -1, msg.isPrivateToUs(source.getState()));


        } else if (command instanceof PingCommand) {
            source.getConnection().sendCommand(new PongCommand(command.getSourceString()));
        }

    }

    protected void updateState(State state) {
        if (state == State.UNCONNECTED) {
            source.error("IRC: " + "Disconnected");
        }
    }
}
