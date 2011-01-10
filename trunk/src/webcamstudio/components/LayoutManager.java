/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LayoutManager.java
 *
 * Created on 2010-09-21, 10:40:46
 */
package webcamstudio.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import webcamstudio.controls.ControlPosition;
import webcamstudio.controls.Controls;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;
import webcamstudio.layout.transitions.Start;
import webcamstudio.layout.transitions.Stop;
import webcamstudio.layout.transitions.Transition;
import webcamstudio.sound.AudioMixer;
import webcamstudio.sources.VideoSource;
import webcamstudio.sources.VideoSourceAnimation;
import webcamstudio.sources.VideoSourceDV;
import webcamstudio.sources.VideoSourceIRC;
import webcamstudio.sources.VideoSourceImage;
import webcamstudio.sources.VideoSourceMovie;
import webcamstudio.sources.VideoSourcePipeline;
import webcamstudio.sources.VideoSourceText;
import webcamstudio.sources.VideoSourceV4L;
import webcamstudio.sources.VideoSourceWidget;

/**
 *
 * @author lgs
 */
public class LayoutManager extends javax.swing.JPanel implements SourceListener {

    private Collection<VideoSource> sources = null;
    private javax.swing.tree.DefaultMutableTreeNode root = null;
    private java.util.Vector<Layout> layouts = new java.util.Vector<Layout>();
    javax.swing.tree.DefaultTreeModel model = null;
    private Layout currentLayout = null;
    private Layout oldLayout = null;
    private LayoutItem currentLayoutItem = null;
    private ImageIcon iconMovie = null;
    private ImageIcon iconImage = null;
    private ImageIcon iconDevice = null;
    private ImageIcon iconAnimation = null;
    private ImageIcon iconFolder = null;
    private ImageIcon iconText = null;
    private boolean stopMe = false;
    private AudioMixer audioMixer = null;
    private String micLabel = "Microphone";

    /** Creates new form LayoutManager */
    public LayoutManager(AudioMixer mixer) {
        initComponents();
        audioMixer = mixer;
        iconMovie = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/video-display.png")));
        iconImage = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/image-x-generic.png")));
        iconDevice = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/camera-video.png")));
        iconAnimation = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/user-info.png")));
        iconFolder = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/folder.png")));
        iconText = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/format-text-bold.png")));
        sources = LayerManager.getSources();
        root = new javax.swing.tree.DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("LAYOUTS"));
        micLabel = java.util.ResourceBundle.getBundle("webcamstudio/Languages").getString("MICROPHONE");
        model = new javax.swing.tree.DefaultTreeModel(root);
        treeLayouts.setModel(model);
        txtLayoutName.setEnabled(false);
        javax.swing.tree.DefaultTreeCellRenderer treeRenderer = new javax.swing.tree.DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean sel, boolean expanded, boolean isleaf, int index, boolean hasFocus) {
                Component retValue = super.getTreeCellRendererComponent(tree, value, sel, expanded, isleaf, index, hasFocus);
                if (retValue instanceof JLabel) {
                    JLabel label = (JLabel) retValue;
                    label.setForeground(Color.BLACK);
                    if (((javax.swing.tree.DefaultMutableTreeNode) value).getUserObject() instanceof LayoutItem) {
                        VideoSource v = ((LayoutItem) ((javax.swing.tree.DefaultMutableTreeNode) value).getUserObject()).getSource();
                        label.setText(v.getName());
                        label.setToolTipText(v.getLocation());
                        if (!v.isPlaying()) {
                            label.setForeground(Color.GRAY);
                        }
                        if (v instanceof VideoSourceImage) {
                            VideoSourceImage source = (VideoSourceImage) v;
                            if (source.getThumnail() == null) {
                                label.setIcon(iconImage);
                            } else {
                                label.setIcon(new ImageIcon(source.getThumnail()));
                            }

                        } else if (v instanceof VideoSourceMovie) {
                            label.setIcon(iconMovie);
                        } else if (v instanceof VideoSourceV4L || v instanceof VideoSourceDV || v instanceof VideoSourcePipeline) {
                            label.setIcon(iconDevice);
                        } else if (v instanceof VideoSourceAnimation || v instanceof VideoSourceWidget) {
                            label.setIcon(iconAnimation);
                        } else if (v instanceof VideoSourceText || v instanceof VideoSourceIRC) {
                            label.setIcon(iconText);
                        } else {
                            label.setIcon(iconImage);
                        }
                    } else if (((javax.swing.tree.DefaultMutableTreeNode) value).getUserObject() instanceof Layout) {
                        Layout l = ((Layout) ((javax.swing.tree.DefaultMutableTreeNode) value).getUserObject());
                        label.setText(l.toString());
                        label.setIcon(new ImageIcon(l.getPreview().getScaledInstance(32, 32, BufferedImage.SCALE_FAST)));

                    } else {
                        label.setIcon(null);
                    }
                    label.setDisabledIcon(label.getIcon());
                }
                return retValue;
            }
        };

        treeLayouts.setCellRenderer(treeRenderer);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                while (!stopMe) {
                    repaint();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LayoutManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public void quitting() {
        stopMe = true;
    }

    public void applyLayoutHotKey(String key) {
        //Find Layout in tree...
        Enumeration en = root.children();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            Layout l = (Layout) node.getUserObject();
            if (l.getHotKey().equals(key)) {
                TreePath t = new TreePath(node.getPath());
                treeLayouts.setSelectionPath(t);
                treeLayouts.revalidate();
                treeLayouts.repaint();
                btnApply.doClick();
            }
        }
    }

    public Collection<Layout> getLayouts() {
        return layouts;
    }

    public void addSource(VideoSource s) {
        if (layouts.isEmpty()) {
            btnAdd.doClick();
        }
        for (Layout layout : layouts) {
            layout.addSource(s, new Start(), new Stop());
        }
        updateTree();
    }

    @Override
    public void sourceRemoved(VideoSource s) {
        s.stopSource();
        LayerManager.remove(s);
        for (Layout layout : layouts) {
            layout.removeSource(s);
        }
        currentLayoutItem = null;
        updateTree();
        treeLayouts.setSelectionPath(new TreePath(root.getPath()));
        tabControls.removeAll();
    }

    public void updateTree() {
        updateLayouts(root);
        model.reload();
        treeLayouts.revalidate();
        for (int i = 0; i < treeLayouts.getRowCount(); i++) {
            treeLayouts.expandRow(i);
        }
        treeLayouts.revalidate();
        treeLayouts.repaint();

    }

    public DefaultMutableTreeNode addLayout(Layout l) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(l);
        root.add(node);
        updateItems(l, node);
        model.reload();
        return node;
    }

    private void updateLayouts(DefaultMutableTreeNode node) {
        node.removeAllChildren();
        for (Layout l : layouts) {
            DefaultMutableTreeNode lNode = new DefaultMutableTreeNode(l);
            node.add(lNode);
            updateItems(l, lNode);
        }
    }

    private void updateItems(Layout l, DefaultMutableTreeNode node) {
        node.removeAllChildren();
        for (LayoutItem li : l.getItems()) {
            DefaultMutableTreeNode liNode = new DefaultMutableTreeNode(li);
            node.add(liNode);
            updateItemDetails(li,liNode);
        }
    }

    private void updateItemDetails (LayoutItem li,DefaultMutableTreeNode node){
        node.removeAllChildren();
        node.add(new DefaultMutableTreeNode(li.getX() + "," + li.getY()));
        node.add(new DefaultMutableTreeNode(li.getWidth() + "x" + li.getHeight()));
        node.add(new DefaultMutableTreeNode(li.getTransitionIn().toString() + " - " + li.getTransitionOut().toString()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panControls = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnApply = new javax.swing.JButton();
        scroller = new javax.swing.JScrollPane();
        treeLayouts = new javax.swing.JTree();
        panSources = new javax.swing.JPanel();
        txtLayoutName = new javax.swing.JTextField();
        cboHotkeys = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        tabControls = new javax.swing.JTabbedPane();

        setLayout(new java.awt.BorderLayout());

        panControls.setName("panControls"); // NOI18N
        panControls.setLayout(new javax.swing.BoxLayout(panControls, javax.swing.BoxLayout.LINE_AXIS));

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/list-add.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        btnAdd.setToolTipText(bundle.getString("ADD")); // NOI18N
        btnAdd.setName("btnAdd"); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        panControls.add(btnAdd);

        btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/list-remove.png"))); // NOI18N
        btnRemove.setToolTipText(bundle.getString("REMOVE")); // NOI18N
        btnRemove.setEnabled(false);
        btnRemove.setName("btnRemove"); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });
        panControls.add(btnRemove);

        btnApply.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        btnApply.setToolTipText(bundle.getString("APPLY")); // NOI18N
        btnApply.setEnabled(false);
        btnApply.setName("btnApply"); // NOI18N
        btnApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyActionPerformed(evt);
            }
        });
        panControls.add(btnApply);

        add(panControls, java.awt.BorderLayout.NORTH);

        scroller.setMinimumSize(new java.awt.Dimension(300, 25));
        scroller.setName("scroller"); // NOI18N

        treeLayouts.setMinimumSize(new java.awt.Dimension(300, 0));
        treeLayouts.setName("treeLayouts"); // NOI18N
        treeLayouts.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeLayoutsValueChanged(evt);
            }
        });
        scroller.setViewportView(treeLayouts);

        add(scroller, java.awt.BorderLayout.CENTER);

        panSources.setName("panSources"); // NOI18N

        txtLayoutName.setText("Layout Name...");
        txtLayoutName.setName("txtLayoutName"); // NOI18N
        txtLayoutName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLayoutNameActionPerformed(evt);
            }
        });

        cboHotkeys.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12" }));
        cboHotkeys.setName("cboHotkeys"); // NOI18N
        cboHotkeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboHotkeysActionPerformed(evt);
            }
        });

        jLabel7.setText(bundle.getString("HOTKEY")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel1.setText(bundle.getString("LAYOUTNAME")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        tabControls.setName("tabControls"); // NOI18N

        javax.swing.GroupLayout panSourcesLayout = new javax.swing.GroupLayout(panSources);
        panSources.setLayout(panSourcesLayout);
        panSourcesLayout.setHorizontalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSourcesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panSourcesLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLayoutName, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panSourcesLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboHotkeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(382, Short.MAX_VALUE))
            .addComponent(tabControls, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 673, Short.MAX_VALUE)
        );
        panSourcesLayout.setVerticalGroup(
            panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSourcesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtLayoutName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panSourcesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cboHotkeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabControls, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
        );

        add(panSources, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void treeLayoutsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeLayoutsValueChanged
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
        txtLayoutName.setText("");
        txtLayoutName.setEnabled(false);
        btnApply.setEnabled(false);
        btnRemove.setEnabled(false);
        cboHotkeys.setEnabled(false);
        tabControls.removeAll();
        tabControls.revalidate();

        Object obj = node.getUserObject();
        if (obj instanceof Layout) {
            currentLayout = (Layout) obj;
            btnApply.setEnabled(true);
            btnRemove.setEnabled(true);
            cboHotkeys.setEnabled(true);
            cboHotkeys.setSelectedItem(currentLayout.getHotKey());
            if (audioMixer.isActive()) {
                AudioMixerPanel mixerPanel = new AudioMixerPanel((Layout) obj, audioMixer);
                tabControls.add(micLabel, mixerPanel);
            }
            txtLayoutName.setText(currentLayout.toString());
            txtLayoutName.setEnabled(true);
        } else if (obj instanceof LayoutItem) {
            currentLayoutItem = (LayoutItem) obj;
            if (evt.getPath().getParentPath() != null) {
                currentLayout = (Layout) ((DefaultMutableTreeNode) evt.getPath().getParentPath().getLastPathComponent()).getUserObject();
                txtLayoutName.setText(currentLayout.toString());
                cboHotkeys.setEnabled(true);
                cboHotkeys.setSelectedItem(currentLayout.getHotKey());
                txtLayoutName.setEnabled(true);
                
                ControlPosition ctrl = new ControlPosition(currentLayoutItem);
                ctrl.setListener(this);
                tabControls.add(ctrl.getLabel(), ctrl);
                for (JPanel panel : currentLayoutItem.getSource().getControls()) {
                    Controls c = (Controls) panel;
                    tabControls.add(c.getLabel(), panel);
                    c.setListener(this);
                }
            }
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    }//GEN-LAST:event_treeLayoutsValueChanged

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Layout l = new Layout("New Layout");
        for (VideoSource s : sources) {
            l.addSource(s, new Start(), new Stop());
        }
        layouts.add(l);
        DefaultMutableTreeNode node = addLayout(l);
        model.reload();
        TreePath t = new TreePath(node.getPath());
        treeLayouts.setSelectionPath(t);
        treeLayouts.expandPath(t);
        treeLayouts.revalidate();
        treeLayouts.repaint();



    }//GEN-LAST:event_btnAddActionPerformed

    private void btnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyActionPerformed
        new Thread(new Runnable() {

            @Override
            public void run() {
                btnApply.setEnabled(false);
                treeLayouts.setEnabled(false);
                if (currentLayout != null) {
                    if (oldLayout != null) {
                        oldLayout.exitLayout();
                    }
                    audioMixer.setVolume(currentLayout.getMicVolume());
                    audioMixer.setLowFilter(currentLayout.getMicLow());
                    audioMixer.setMiddleFilter(currentLayout.getMicMiddle());
                    audioMixer.setHighFilter(currentLayout.getMicHigh());
                    currentLayout.enterLayout();
                    oldLayout = currentLayout;
                }
                treeLayouts.setEnabled(true);
                btnApply.setEnabled(true);
            }
        }).start();

    }//GEN-LAST:event_btnApplyActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        if (currentLayout != null) {
            layouts.remove(currentLayout);
            currentLayout = null;
            currentLayoutItem = null;
            updateTree();
            txtLayoutName.setText("");
            txtLayoutName.setEnabled(false);
            btnApply.setEnabled(false);
            btnRemove.setEnabled(false);

        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void txtLayoutNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLayoutNameActionPerformed
        currentLayout.setName(txtLayoutName.getText());
        treeLayouts.repaint();
    }//GEN-LAST:event_txtLayoutNameActionPerformed

    private void cboHotkeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboHotkeysActionPerformed
        currentLayout.setHotKey(cboHotkeys.getSelectedItem().toString());

    }//GEN-LAST:event_cboHotkeysActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnRemove;
    private javax.swing.JComboBox cboHotkeys;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel panControls;
    private javax.swing.JPanel panSources;
    private javax.swing.JScrollPane scroller;
    private javax.swing.JTabbedPane tabControls;
    private javax.swing.JTree treeLayouts;
    private javax.swing.JTextField txtLayoutName;
    // End of variables declaration//GEN-END:variables

    @Override
    public void sourceUpdate(VideoSource source) {
        Enumeration l = root.children();
        while (l.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) l.nextElement();
            if (node.getUserObject().equals(currentLayout)) {
                Enumeration li = node.children();
                while (li.hasMoreElements()) {
                    DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) li.nextElement();
                    if (subnode.getUserObject().equals(currentLayoutItem)) {
                        updateItemDetails(currentLayoutItem, subnode);
                        model.reload(subnode);
                        treeLayouts.setSelectionPath(new TreePath(subnode.getPath()));
                        break;
                    }
                }
            }
        }
        treeLayouts.revalidate();
    }

    @Override
    public void sourceSetX(VideoSource source, int x) {
        currentLayoutItem.setX(x);
        treeLayouts.revalidate();
    }

    @Override
    public void sourceSetY(VideoSource source, int y) {
        currentLayoutItem.setY(y);
        treeLayouts.revalidate();
    }

    @Override
    public void sourceSetWidth(VideoSource source, int w) {
        currentLayoutItem.setWidth(w);
        treeLayouts.revalidate();
    }

    @Override
    public void sourceSetHeight(VideoSource source, int h) {
        currentLayoutItem.setHeight(h);
        treeLayouts.revalidate();
    }

    @Override
    public void sourceMoveUp(VideoSource source) {
        currentLayout.moveUpItem(currentLayoutItem);
        updateTree();
        Enumeration l = root.children();
        while (l.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) l.nextElement();
            if (node.getUserObject().equals(currentLayout)) {
                Enumeration li = node.children();
                while (li.hasMoreElements()) {
                    DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) li.nextElement();
                    if (subnode.getUserObject().equals(currentLayoutItem)) {
                        treeLayouts.setSelectionPath(new TreePath(subnode.getPath()));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void sourceMoveDown(VideoSource source) {
        currentLayout.moveDownItem(currentLayoutItem);
        updateTree();
        Enumeration l = root.children();
        while (l.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) l.nextElement();
            if (node.getUserObject().equals(currentLayout)) {
                Enumeration li = node.children();
                while (li.hasMoreElements()) {
                    DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) li.nextElement();
                    if (subnode.getUserObject().equals(currentLayoutItem)) {
                        treeLayouts.setSelectionPath(new TreePath(subnode.getPath()));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void sourceSetTransIn(VideoSource source, Transition in) {
        currentLayoutItem.setTransitionIn(in);
        treeLayouts.revalidate();
    }

    @Override
    public void sourceSetTransOut(VideoSource source, Transition out) {
        currentLayoutItem.setTransitionOut(out);
        treeLayouts.revalidate();
    }
}
