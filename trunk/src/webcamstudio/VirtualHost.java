/**
 *  WebcamStudio for GNU/Linux
 *  Copyright (C) 2008  Patrick Balleux
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 */
package webcamstudio;

import webcamstudio.sources.VideoSource;

public class VirtualHost {

    public VirtualHost() {
    }

    public void put(String keywords, VideoSource source) {
        if (source.hasNewKeywords()) {
            source.updateOldKeywords();
            if (sources.containsValue(source)) {
                Object[] list = sources.keySet().toArray();
                for (int i = 0; i < list.length; i++) {
                    String key = list[i].toString();
                    if (sources.get(key).equals(source)) {
                        sources.remove(key);
                    }
                }
            }
            if (source.getKeywords().length() > 0) {
                String[] keys = keywords.toUpperCase().split(",");
                for (int i = 0; i < keys.length; i++) {
                    sources.put(keys[i], source);
                }
            }
        }

    }

    public void clear() {
        if (currentSource != null) {
            currentSource.stopSource();
            currentSource.setLooping(true);
            currentSource = null;
        }
        sources.clear();
    }

    public void remove(String keyword) {
        String[] keywords = keyword.split(",");
        for (int i = 0; i < keywords.length; i++) {
            sources.remove(keywords[i].toUpperCase());
        }
    }

    public void setLightMode(boolean mode){
        lightMode=mode;
    }
    public void setNewLine(String line) {
        String keyword = "";
        java.util.Iterator list = sources.keySet().iterator();
        while (list.hasNext()) {
            keyword = list.next().toString().toUpperCase();
            System.out.println(keyword + "," + line);
            currentSource = sources.get(keyword);
            if (keyword.startsWith("!")) {
                if (line.toUpperCase().indexOf(keyword.replaceFirst("!", "")) != -1 && currentSource != null) {
                    if (currentSource.isPlaying()) {
                        currentSource.setLooping(true);
                        currentSource.stopSource();
                    }
                }
            } else if (line.toUpperCase().indexOf(keyword) != -1 && currentSource != null) {
                if (!currentSource.isPlaying()) {
                    currentSource.setLooping(false);
                    currentSource.setLightMode(lightMode);
                    currentSource.startSource();
                }
            }


        }
    }
    private java.util.TreeMap<String, VideoSource> sources = new java.util.TreeMap<String, VideoSource>();
    private VideoSource currentSource = null;
    private boolean lightMode = false;
}
