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

import webcamstudio.util.Utils;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class RtmpClient {


    public static void main(String[] args) {
        System.out.println();
        final ClientOptions options = new ClientOptions();
        if(!options.parseCli(args)) {
            return;
        }
        Utils.printlnCopyrightNotice();
        final int count = options.getLoad();
        if(count == 1) {
            connect(options);
            return;
        }
        options.setSaveAs(null);
        final Executor executor = Executors.newFixedThreadPool(options.getThreads());        
        final ClientBootstrap bootstrap = getBootstrap(executor, options);
        for(int i = 0; i < count; i++) {
            final int index = i + 1;
            executor.execute(new Runnable() {
                @Override public void run() {
                    bootstrap.connect(new InetSocketAddress(options.getHost(), options.getPort()));
                }
            });
        }
        // TODO graceful shutdown
    }

    public static void connect(final ClientOptions options) {  
        final ClientBootstrap bootstrap = getBootstrap(Executors.newCachedThreadPool(), options);
        future = bootstrap.connect(new InetSocketAddress(options.getHost(), options.getPort()));
        future.awaitUninterruptibly();

        if(!future.isSuccess()) {
             future.getCause().printStackTrace();
        }
        future.getChannel().getCloseFuture().awaitUninterruptibly(); 
        bootstrap.getFactory().releaseExternalResources();

    }

    public static void setSuccess(){
        if (future!=null){
            future.setSuccess();
        }
    }
    private static ClientBootstrap getBootstrap(final Executor executor, final ClientOptions options) {
        final ChannelFactory factory = new NioClientSocketChannelFactory(executor, executor);
        final ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new ClientPipelineFactory(options));
        bootstrap.setOption("tcpNoDelay" , true);
        bootstrap.setOption("keepAlive", true);
        return bootstrap;
    }
    private static ChannelFuture future = null;

}
