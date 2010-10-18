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

package webcamstudio.io.flv;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


import webcamstudio.rtmp.RtmpHeader;
import webcamstudio.rtmp.RtmpMessage;
import webcamstudio.rtmp.RtmpWriter;
import org.jboss.netty.buffer.ChannelBuffer;

public class FlvWriter implements RtmpWriter {


    private final FileChannel out;
    private final int[] channelTimes = new int[RtmpHeader.MAX_CHANNEL_ID];
    private int primaryChannel = -1;
    private int lastLoggedSeconds;
    private final int seekTime;
    private final long startTime;  

    public FlvWriter(final String fileName) {
        this(0, fileName);
    }

    public FlvWriter(final int seekTime, final String fileName) {
        this.seekTime = seekTime < 0 ? 0 : seekTime;
        this.startTime = System.currentTimeMillis();
        if(fileName == null) {
            out = null;
            return;
        }
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            out = fos.getChannel();
            out.write(FlvAtom.flvHeader().toByteBuffer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
    }

    @Override
    public void close() {
        if(out != null) {
            try {
                out.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if(primaryChannel == -1) {
            return;
        }
    }

    private void logWriteProgress() {
        final int seconds = (channelTimes[primaryChannel] - seekTime) / 1000;
        if (seconds >= lastLoggedSeconds + 10) {
            lastLoggedSeconds = seconds - (seconds % 10);
        }
    }

    @Override
    public void write(final RtmpMessage message) {
        final RtmpHeader header = message.getHeader();
        if(header.isAggregate()) {
            final ChannelBuffer in = message.encode();
            while (in.readable()) {
                final FlvAtom flvAtom = new FlvAtom(in);
                final int absoluteTime = flvAtom.getHeader().getTime();
                channelTimes[primaryChannel] = absoluteTime;
                write(flvAtom);
                // logger.debug("aggregate atom: {}", flvAtom);
                logWriteProgress();
            }
        } else { // METADATA / AUDIO / VIDEO
            final int channelId = header.getChannelId();
            channelTimes[channelId] = seekTime + header.getTime();
            if(primaryChannel == -1 && (header.isAudio() || header.isVideo())) {
                primaryChannel = channelId;
            }
            if(header.getSize() <= 2) {
                return;
            }
            write(new FlvAtom(header.getMessageType(), channelTimes[channelId], message.encode()));
            if (channelId == primaryChannel) {
                logWriteProgress();
            }
        }
    }

    private void write(final FlvAtom flvAtom) {
        if(out == null) {
            return;
        }
        try {
            out.write(flvAtom.write().toByteBuffer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
