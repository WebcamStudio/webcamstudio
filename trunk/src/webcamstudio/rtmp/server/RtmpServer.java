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

package webcamstudio.rtmp.server;

import webcamstudio.rtmp.RtmpConfig;
import webcamstudio.util.StopMonitor;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

public class RtmpServer {


    static {
        RtmpConfig.configureServer();
        CHANNELS = new DefaultChannelGroup("server-channels");
        APPLICATIONS = new ConcurrentHashMap<String, ServerApplication>();
        TIMER = new HashedWheelTimer(RtmpConfig.TIMER_TICK_SIZE, TimeUnit.MILLISECONDS);
    }
    
    protected static final ChannelGroup CHANNELS;
    protected static final Map<String, ServerApplication> APPLICATIONS;
    public static final Timer TIMER;

    public static void main(String[] args) throws Exception {

        final ChannelFactory factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        final ServerBootstrap bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new ServerPipelineFactory());
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        final InetSocketAddress socketAddress = new InetSocketAddress(RtmpConfig.SERVER_PORT);
        bootstrap.bind(socketAddress);

        final Thread monitor = new StopMonitor(RtmpConfig.SERVER_STOP_PORT);
        monitor.start();        
        monitor.join();

        TIMER.stop();
        final ChannelGroupFuture future = CHANNELS.close();
        future.awaitUninterruptibly();
        factory.releaseExternalResources();

    }

}
