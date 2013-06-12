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
    ArrayList<String> channelNames = new ArrayList<String>();
    ArrayList<Stream> streams = new ArrayList<Stream>();
    ArrayList<ArrayList<String>> CHOfSource = new ArrayList<ArrayList<String>>();
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
        System.out.println(s.getClass().getName().toString() + " registered");
        streams.add(s);
    }
    public void unregister(Stream s){
        System.out.println(s.getName() + " unregistered");
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
            SourceChannel sc = null;
            for (SourceChannel ssc : s.getChannels()){
                if (ssc.getName().equals(name)){
                    sc=ssc;
                    break;
                }
            }
            if (sc!=null){
                s.removeChannel(sc);
            }
            s.addChannel(SourceChannel.getChannel(name, s));
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
                Tools.sleep(5); //To let WSPidsBuster work correctly.
                if (sc.getName().equals(name)){
                    Tools.sleep(5); //To let WSPidsBuster work correctly.
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
