/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.channels;

import java.util.ArrayList;
import webcamstudio.streams.SourceChannel;
import webcamstudio.streams.Stream;
import webcamstudio.util.Tools;

/**
 *
 * @author patrick (modified by karl)
 */
public class MasterChannels {
    ArrayList<String> channelNames = new ArrayList<>();
    ArrayList<Stream> streams = new ArrayList<>();
    ArrayList<ArrayList<String>> CHOfSource = new ArrayList<>();
    static MasterChannels instance = null;
    
    private MasterChannels(){
    }
    public static MasterChannels getInstance(){
        if (instance==null){
            instance = new MasterChannels();
        }
        return instance;
    }
    public void register(Stream s){
        String streamName =s.getClass().getName().toString();
        streamName = streamName.replace("webcamstudio.streams.", "");
        System.out.println(streamName + " registered.");
        streams.add(s);
    }
    public void unregister(Stream s){
        System.out.println(s.getName() + " unregistered.");
        streams.remove(s);
    }
    public void addChannel(String name){
        channelNames.add(name);
        for (Stream s : streams){
            s.addChannel(SourceChannel.getChannel(name, s));
        }
    }
    public void updateChannel(String name){
        for (Stream s : streams){
            String streamName =s.getClass().getName().toString();
            SourceChannel sc = null;
            int x = 0;
            for (int i=0; i < s.getChannels().size(); i++){
                if (s.getChannels().get(i).getName().equals(name)){
                    sc=s.getChannels().get(i);
                    x = i;
                    break;
                }
            }
            if (streamName.contains("Sink")){
            } else {
                if (sc!=null){
                    s.removeChannelAt(x);
                }
                s.addChannelAt(SourceChannel.getChannel(name, s),x);
                x=0;
            }
        }
    }
    
    public void insertStudio(String name){
        for (Stream s : streams){
            int co = 0;
            for (SourceChannel ssc : s.getChannels()){
                if (ssc.getName().equals(name)){
                    co++;
                }                  
            }
            if (co == 0){
                boolean backState = false;
                if (s.isPlaying()){
                    s.setIsPlaying(false);
                    backState = true;
                }
                s.addChannel(SourceChannel.getChannel(name, s));
                if (backState){
                    s.setIsPlaying(true);
                }
            } else {
                ArrayList<String> allChan = new ArrayList<>();
                for (String scn : MasterChannels.getInstance().getChannels()){
                    allChan.add(scn); 
                } 
                for (SourceChannel scc3 : s.getChannels()){
                    String removech = scc3.getName();
                    allChan.remove(removech);
                }
                for (String ssc2 : allChan) {
                    s.addChannel(SourceChannel.getChannel(ssc2, s));
                }
            }
        }
    }
    public void removeChannel(String name){
        channelNames.remove(name);
        for (Stream s : streams){
            SourceChannel toRemove = null;
            for (SourceChannel sc : s.getChannels()){
                if (sc.getName().equals(name)){
                    toRemove=sc;
                }
            }
            if (toRemove!=null){
            s.removeChannel(toRemove);
            }
        }
    }
    public void selectChannel(String name){
        for (Stream stream : streams){
            for (SourceChannel sc : stream.getChannels()){
                if (sc.getName().equals(name)){
                    sc.apply(stream);
                    break;
                }
            }
        }
    }
    
    public ArrayList<String> getChannels(){
        return channelNames;
    }
    
    public void stopAllStream(){
        for (Stream s : streams){
            Tools.sleep(30);
            s.stop();
        }
    }
    public ArrayList<Stream> getStreams(){
        return streams;
    }
}
