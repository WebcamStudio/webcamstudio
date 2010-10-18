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

package webcamstudio.io.f4v;

import webcamstudio.io.BufferReader;
import webcamstudio.io.f4v.box.FTYP;
import webcamstudio.io.f4v.box.MVHD;
import webcamstudio.io.f4v.box.STSD.VideoSD;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieInfo {

    
    private long moovPosition;
    private FTYP ftyp;
    private MVHD mvhd;    
    private List<TrackInfo> tracks = new ArrayList<TrackInfo>();
    private List<Sample> samples;

    public List<Sample> getSamples() {
        return samples;
    }

    public long getMoovPosition() {
        return moovPosition;
    }

    public double getDuration() {
        return mvhd.getDuration() / mvhd.getTimeScale();
    }
    @SuppressWarnings("unchecked")
    private void initSamples() {
        samples = new ArrayList<Sample>();
        for(TrackInfo track : tracks) {
            for(Chunk chunk : track.getChunks()) {
                samples.addAll(chunk.getSamples());
            }
        }
        Collections.sort(samples); // sort by time, implements comparable
    }

    public MovieInfo(final BufferReader in) {
        while(in.position() < in.size()) {            
            Box box = new Box(in, in.size());
            if(box.getType() == BoxType.FTYP) {
                ftyp = (FTYP) box.getPayload();
            }
            if(box.getType() == BoxType.MOOV) {
                moovPosition = box.getFileOffset();
                for(Box moov : box.getChildren()) {
                    if(moov.getType() == BoxType.MVHD) {
                        mvhd = (MVHD) moov.getPayload();
                    }
                    if(moov.getType() == BoxType.TRAK) {
                        TrackInfo track = new TrackInfo(moov);
                        track.setMovie(this);
                        tracks.add(track);
                    }
                }
            }
        }
        initSamples();
    }

    public List<TrackInfo> getTracks() {
        return tracks;
    }

    public TrackInfo getVideoTrack() {
        for(TrackInfo track : tracks) {
            if(track.getStsd().getSampleType(1).isVideo()) {
                return track;
            }
        }
        return null;
    }

    public byte[] getVideoDecoderConfig() {
        return getVideoSampleDescription().getConfigBytes();
    }

    public VideoSD getVideoSampleDescription() {
        TrackInfo track = getVideoTrack();
        if(track == null) {
            return null;
        }
        return (VideoSD) track.getStsd().getSampleDescription(1);
    }

    public TrackInfo getAudioTrack() {
        for(TrackInfo track : tracks) {
            if(!track.getStsd().getSampleType(1).isVideo()) {
                return track;
            }
        }
        return null;
    }

}
