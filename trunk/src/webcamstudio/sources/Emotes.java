/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamstudio.sources;

import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

/**
 *
 * @author patrick
 */
public class Emotes {

    private static java.util.TreeMap<String, ImageIcon> emotes = new java.util.TreeMap<String, ImageIcon>();

    public Emotes() {
        initialise();
    }

    private void initialise() {
        if (emotes.size() == 0) {
            ImageIcon img = null;
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-angel.png"));
            emotes.put("O:-)", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-angry.png"));
            emotes.put("(:-&", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-cool.png"));
            emotes.put("<-|", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-crying.png"));
            emotes.put(":/", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-devilish.png"));
            emotes.put(">:>", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-embarrassed.png"));
            emotes.put("^^;", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-kiss.png"));
            emotes.put(":*", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-laugh.png"));
            emotes.put(":))", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-monkey.png"));
            emotes.put(":-|", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-plain.png"));
            emotes.put(":|", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-sad.png"));
            emotes.put(":(", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-sick.png"));
            emotes.put(":-(*)", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-smile-big.png"));
            emotes.put(":D", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-smile.png"));
            emotes.put(":)", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-smirk.png"));
            emotes.put(";^)", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-surprise.png"));
            emotes.put("=-o", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-tired.png"));
            emotes.put("<g_g>", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-uncertain.png"));
            emotes.put(":-$", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-wink.png"));
            emotes.put(";)", img);
            img = new javax.swing.ImageIcon(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/emotes/face-worried.png"));
            emotes.put(":-s", img);
        }
    }

    public boolean isSmyley(String word) {
        return emotes.containsKey(word);
    }

    public ImageIcon getSmiley(String word) {
        return emotes.get(word);
    }

    public static void main(String[] args) {
        new Emotes().getSmiley(":)");

        System.out.println("OK");
    }
}
