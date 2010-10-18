/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package webcamstudio.rtmp.client;

import webcamstudio.io.flv.FlvWriter;

import webcamstudio.rtmp.LoopedReader;
import webcamstudio.rtmp.message.Control;
import webcamstudio.rtmp.RtmpMessage;
import webcamstudio.rtmp.RtmpReader;
import webcamstudio.rtmp.RtmpPublisher;
import webcamstudio.rtmp.RtmpWriter;
import webcamstudio.rtmp.message.BytesRead;
import webcamstudio.rtmp.message.ChunkSize;
import webcamstudio.rtmp.message.WindowAckSize;
import webcamstudio.rtmp.message.Command;
import webcamstudio.rtmp.message.Metadata;
import webcamstudio.rtmp.message.SetPeerBw;
import webcamstudio.util.ChannelUtils;
import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;


@ChannelPipelineCoverage("one")
public class ClientHandler extends SimpleChannelUpstreamHandler {

    private int transactionId = 1;
    private Map<Integer, String> transactionToCommandMap;
    private ClientOptions options;
    private byte[] swfvBytes;

    private RtmpWriter writer;

    private int bytesReadWindow = 2500000;
    private long bytesRead;
    private long bytesReadLastSent;    
    private int bytesWrittenWindow = 2500000;
    
    private RtmpPublisher publisher;
    private int streamId;    

    public void setSwfvBytes(byte[] swfvBytes) {
        this.swfvBytes = swfvBytes;        
    }

    public ClientHandler(ClientOptions options) {
        this.options = options;
        transactionToCommandMap = new HashMap<Integer, String>();        
    }

    private void writeCommandExpectingResult(Channel channel, Command command) {
        final int id = transactionId++;
        command.setTransactionId(id);
        transactionToCommandMap.put(id, command.getName());
        System.out.println("WS4GL-" + getClass().getName() + ":  Sending command " + command.getName());
        channel.write(command);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("WS4GL-" + getClass().getName() + ":  Channel Openened " + e.toString());
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        System.out.println("WS4GL-" + getClass().getName() + ":  Handshake complete, sending 'connect ");
        writeCommandExpectingResult(e.getChannel(), Command.connect(options));
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("WS4GL-" + getClass().getName() + ":  Channel Closed " + e.toString());
        if(writer != null) {
            writer.close();
        }
        if(publisher != null) {
            publisher.close();
        }
        super.channelClosed(ctx, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent me) {
        if(publisher != null && publisher.handle(me)) {
            return;
        }
        final Channel channel = me.getChannel();
        final RtmpMessage message = (RtmpMessage) me.getMessage();
        System.out.println("WS4GL-" + getClass().getName() + ":  Message " + message.toString());
        switch(message.getHeader().getMessageType()) {
            case CHUNK_SIZE: // handled by decoder
                break;
            case CONTROL:
                Control control = (Control) message;
                switch(control.getType()) {
                    case PING_REQUEST:
                        System.out.println("WS4GL-" + getClass().getName() + ":  Ping Request " + me.toString());
                        final int time = control.getTime();
                        Control pong = Control.pingResponse(time);
                        channel.write(pong);
                        break;
                    case SWFV_REQUEST:

                        if(swfvBytes == null) {
                            System.out.println("WS4GL-" + getClass().getName() + ":  WARN : SWFV Request (swfBytes not initialized )" + me.toString());
                        } else {
                            System.out.println("WS4GL-" + getClass().getName() + ":  SWFV Request " + me.toString());
                            Control swfv = Control.swfvResponse(swfvBytes);
                            channel.write(swfv);
                        }
                        break;
                    case STREAM_BEGIN:
                        if(publisher != null && !publisher.isStarted()) {
                            publisher.start(channel, options.getStart(),
                                    options.getLength(), new ChunkSize(4096));
                            return;
                        }
                        if(streamId !=0) {
                            channel.write(Control.setBuffer(streamId, options.getBuffer()));
                        }
                        break;
                    default:
                        System.out.println("WS4GL-" + getClass().getName() + ":  Stream Begin " + me.toString());
                }
                break;
            case METADATA_AMF0:
            case METADATA_AMF3:
                Metadata metadata = (Metadata) message;
                if(metadata.getName().equals("onMetaData")) {
                    writer.write(message);
                } else {
                    System.out.println("WS4GL-" + getClass().getName() + ":  Ignoring Metadata " + me.toString());
                }
                break;
            case AUDIO:
            case VIDEO:
            case AGGREGATE:                
                writer.write(message);
                bytesRead += message.getHeader().getSize();
                if((bytesRead - bytesReadLastSent) > bytesReadWindow) {
                    bytesReadLastSent = bytesRead;
                    channel.write(new BytesRead(bytesRead));
                }
                break;
            case COMMAND_AMF0:
            case COMMAND_AMF3:
                Command command = (Command) message;                
                String name = command.getName();
                if(name.equals("_result")) {
                    String resultFor = transactionToCommandMap.get(command.getTransactionId());
                    System.out.println("WS4GL-" + getClass().getName() + ":  Command " + name  + ": "+ resultFor);
                    if(resultFor.equals("connect")) {
                        writeCommandExpectingResult(channel, Command.createStream());
                    } else if(resultFor.equals("createStream")) {
                        streamId = ((Double) command.getArg(0)).intValue();
                        if(options.getPublishType() != null) { // TODO append, record                            
                            RtmpReader reader;
                            if(options.getFileToPublish() != null) {
                                reader = RtmpPublisher.getReader(options.getFileToPublish());
                            } else {
                                reader = options.getReaderToPublish();
                            }
                            if(options.getLoop() > 1) {
                                reader = new LoopedReader(reader, options.getLoop());
                            }
                            publisher = new RtmpPublisher(reader, streamId, options.getBuffer(), false, false) {
                                @Override protected RtmpMessage[] getStopMessages(long timePosition) {
                                    return new RtmpMessage[]{Command.unpublish(streamId)};
                                }
                            };                            
                            channel.write(Command.publish(streamId, options));
                            return;
                        } else {
                            writer = options.getWriterToSave();
                            if(writer == null) {
                                writer = new FlvWriter(options.getStart(), options.getSaveAs());
                            }
                            channel.write(Command.play(streamId, options));
                            channel.write(Control.setBuffer(streamId, 0));
                        }
                    } else {
                        System.out.println("WS4GL-" + getClass().getName() + ":  WARN Un-handled server result for: " + resultFor);
                    }
                } else if(name.equals("onStatus")) {
                    final Map<String, Object> temp = (Map) command.getArg(0);
                    final String code = (String) temp.get("code");
                    System.out.println("WS4GL-" + getClass().getName() + ":  On Status : " + code);
                    if (code.equals("NetStream.Failed") // TODO cleanup
                            || code.equals("NetStream.Play.Failed")
                            || code.equals("NetStream.Play.Stop")
                            || code.equals("NetStream.Play.StreamNotFound")) {
                        channel.close();
                        return;
                    }
                    if(code.equals("NetStream.Publish.Start")
                            && publisher != null && !publisher.isStarted()) {
                            publisher.start(channel, options.getStart(),
                                    options.getLength(), new ChunkSize(4096));
                        return;
                    }
                    if (publisher != null && code.equals("NetStream.Unpublish.Success")) {
                        ChannelFuture future = channel.write(Command.closeStream(streamId));
                        future.addListener(ChannelFutureListener.CLOSE);
                        return;
                    }
                } else if(name.equals("close")) {
                    System.out.println("WS4GL-" + getClass().getName() + ":  Close ");
                    channel.close();
                    return;
                } else if(name.equals("_error")) {
                    System.out.println("WS4GL-" + getClass().getName() + ":  Close, Error found...");
                    channel.close();
                    return;
                } else {
                }
                break;
            case BYTES_READ:
                System.out.println("WS4GL-" + getClass().getName() + ":  ACK " + message.toString());
                break;
            case WINDOW_ACK_SIZE:
                WindowAckSize was = (WindowAckSize) message;                
                if(was.getValue() != bytesReadWindow) {
                    channel.write(SetPeerBw.dynamic(bytesReadWindow));
                }                
                break;
            case SET_PEER_BW:
                SetPeerBw spb = (SetPeerBw) message;                
                if(spb.getValue() != bytesWrittenWindow) {
                    channel.write(new WindowAckSize(bytesWrittenWindow));
                }
                break;
            default:
        }
        if(publisher != null && publisher.isStarted()) { // TODO better state machine
            publisher.fireNext(channel, 0);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        ChannelUtils.exceptionCaught(e);
    }    

}
