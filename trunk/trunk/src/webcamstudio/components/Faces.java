/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.components;



/**
 *
 * @author pballeux
 */
public class Faces {

    public Faces() {
        
        java.util.ResourceBundle resourceMap = java.util.ResourceBundle.getBundle("webcamstudio/resources/faces/Faces",java.util.Locale.getDefault());

        java.util.Iterator<String> keyList = resourceMap.keySet().iterator();
        String imageName = "";
        String imageKey = "";
        int index = 0;
        java.awt.Image img = null;
        while (keyList.hasNext()) {
            imageKey = keyList.next();
            imageName = resourceMap.getString(imageKey);
            if (imageName != null && imageName.toLowerCase().endsWith("png")) {
                img = java.awt.Toolkit.getDefaultToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/faces/" + imageName));
                list.put(imageKey, img);
            }
        }
        keys = new String[list.size()];
        index = 0;
        keyList = list.keySet().iterator();
        while (keyList.hasNext()){
           keys[index++] = keyList.next();
        }
        setCustom(null);
    }

    public void setCustom(java.awt.Image img){
        list.put("custom", img);
    }
    public java.awt.Image getImage(String key) {
        return list.get(key);
    }

    public String[] getKeys() {
        return keys;
    }
    private java.util.TreeMap<String, java.awt.Image> list = new java.util.TreeMap<String, java.awt.Image>();
    private String[] keys = null;
}
