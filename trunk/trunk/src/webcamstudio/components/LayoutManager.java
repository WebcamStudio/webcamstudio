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

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;
import webcamstudio.layout.transitions.None;
import webcamstudio.layout.transitions.Transition;
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
public class LayoutManager extends javax.swing.JPanel {

    private Collection<VideoSource> sources = null;
    private javax.swing.tree.DefaultMutableTreeNode root = null;
    private java.util.Vector<Layout> layouts = new java.util.Vector<Layout>();
    javax.swing.tree.DefaultTreeModel model = null;
    javax.swing.DefaultComboBoxModel transModelIn = new javax.swing.DefaultComboBoxModel();
    javax.swing.DefaultComboBoxModel transModelOut = new javax.swing.DefaultComboBoxModel();
    private Layout currentLayout = null;
    private Layout oldLayout = null;
    private LayoutItem currentLayoutItem = null;
    private ImageIcon iconMovie = null;
    private ImageIcon iconImage = null;
    private ImageIcon iconDevice = null;
    private ImageIcon iconAnimation = null;
    private ImageIcon iconFolder = null;
    private ImageIcon iconText = null;

    /** Creates new form LayoutManager */
    public LayoutManager() {
        initComponents();
        iconMovie = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/video-display.png")));
        iconImage = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/image-x-generic.png")));
        iconDevice = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/camera-video.png")));
        iconAnimation = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/user-info.png")));
        iconFolder = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/folder.png")));
        iconText = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/format-text-bold.png")));
        sources = LayerManager.getSources();
        root = new javax.swing.tree.DefaultMutableTreeNode("Layouts");
        model = new javax.swing.tree.DefaultTreeModel(root);
        treeLayouts.setModel(model);
        transModelIn = new javax.swing.DefaultComboBoxModel(Transition.getTransitions().values().toArray());
        transModelOut = new javax.swing.DefaultComboBoxModel(Transition.getTransitions().values().toArray());
        cboTransIn.setModel(transModelIn);
        cboTransOut.setModel(transModelOut);
        txtLayoutName.setEnabled(false);
        cboTransIn.setEnabled(false);
        cboTransOut.setEnabled(false);
        spinX.setEnabled(false);
        spinY.setEnabled(false);
        spinWidth.setEnabled(false);
        spinHeight.setEnabled(false);
        btnUpdateLayoutItem.setEnabled(false);
        btnMoveDown.setEnabled(false);
        btnMoveUp.setEnabled(false);
        javax.swing.tree.DefaultTreeCellRenderer treeRenderer = new javax.swing.tree.DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean sel, boolean expanded, boolean isleaf, int index, boolean hasFocus) {
                Component retValue = super.getTreeCellRendererComponent(tree, value, sel, expanded, isleaf, index, hasFocus);
                if (retValue instanceof JLabel) {
                    JLabel label = (JLabel) retValue;
                    if (((javax.swing.tree.DefaultMutableTreeNode) value).getUserObject() instanceof LayoutItem) {
                        VideoSource v = ((LayoutItem) ((javax.swing.tree.DefaultMutableTreeNode) value).getUserObject()).getSource();
                        label.setText(v.getName());
                        label.setToolTipText(v.getLocation());
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
                        label.setIcon(iconFolder);
                    }
                }
                return retValue;
            }
        };

        treeLayouts.setCellRenderer(treeRenderer);
    }

    public void applyLayoutHotKey(String key){
        //Find Layout in tree...
        Enumeration en = root.children();
        while (en.hasMoreElements()){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
            Layout l = (Layout)node.getUserObject();
            if (l.getHotKey().equals(key)){
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

    public void sourceAdded(VideoSource s) {
        for (Layout layout : layouts) {
            layout.addSource(s, new None(), new None());
        }
        updateTree();
    }

    public void sourceRemoved(VideoSource s) {
        for (Layout layout : layouts) {
            layout.removeSource(s);
        }
        updateTree();
    }

    public void updateTree() {
        root.removeAllChildren();
        updateLayouts(root);
        model.reload();
        treeLayouts.revalidate();
        for (int i = 0;i<treeLayouts.getRowCount();i++){
            treeLayouts.expandRow(i);
        }
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
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scroller = new javax.swing.JScrollPane();
        treeLayouts = new javax.swing.JTree();
        panControls = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnApply = new javax.swing.JButton();
        txtLayoutName = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        cboTransIn = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        cboTransOut = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        spinX = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        spinY = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        spinWidth = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        spinHeight = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        btnUpdateLayoutItem = new javax.swing.JButton();
        btnMoveDown = new javax.swing.JButton();
        btnMoveUp = new javax.swing.JButton();
        cboHotkeys = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();

        scroller.setName("scroller"); // NOI18N

        treeLayouts.setName("treeLayouts"); // NOI18N
        treeLayouts.setRootVisible(false);
        treeLayouts.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeLayoutsValueChanged(evt);
            }
        });
        scroller.setViewportView(treeLayouts);

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

        txtLayoutName.setText("Layout Name...");
        txtLayoutName.setName("txtLayoutName"); // NOI18N

        lblName.setText(bundle.getString("NAME")); // NOI18N
        lblName.setName("lblName"); // NOI18N

        cboTransIn.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboTransIn.setName("cboTransIn"); // NOI18N

        jLabel1.setText(bundle.getString("TRANSITION_IN")); // NOI18N

        cboTransOut.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboTransOut.setName("cboTransOut"); // NOI18N

        jLabel2.setText(bundle.getString("TRANSITION_OUT")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        spinX.setName("spinX"); // NOI18N

        jLabel3.setText(bundle.getString("POSITION_X")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        spinY.setName("spinY"); // NOI18N

        jLabel4.setText(bundle.getString("POSITION_Y")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        spinWidth.setName("spinWidth"); // NOI18N

        jLabel5.setText(bundle.getString("WIDTH")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        spinHeight.setName("spinHeight"); // NOI18N

        jLabel6.setText(bundle.getString("HEIGHT")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        btnUpdateLayoutItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh.png"))); // NOI18N
        btnUpdateLayoutItem.setToolTipText(bundle.getString("UPDATE")); // NOI18N
        btnUpdateLayoutItem.setName("btnUpdateLayoutItem"); // NOI18N
        btnUpdateLayoutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateLayoutItemActionPerformed(evt);
            }
        });

        btnMoveDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-down.png"))); // NOI18N
        btnMoveDown.setToolTipText(bundle.getString("MOVE_DOWN")); // NOI18N
        btnMoveDown.setName("btnMoveDown"); // NOI18N
        btnMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDownActionPerformed(evt);
            }
        });

        btnMoveUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-up.png"))); // NOI18N
        btnMoveUp.setToolTipText(bundle.getString("MOVE_UP")); // NOI18N
        btnMoveUp.setName("btnMoveUp"); // NOI18N
        btnMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveUpActionPerformed(evt);
            }
        });

        cboHotkeys.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12" }));
        cboHotkeys.setName("cboHotkeys"); // NOI18N

        jLabel7.setText(bundle.getString("HOTKEY")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(scroller, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btnMoveUp)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnMoveDown)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnUpdateLayoutItem))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cboTransOut, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(spinX)
                                .addComponent(spinY)
                                .addComponent(spinWidth)
                                .addComponent(spinHeight)
                                .addComponent(cboTransIn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboHotkeys, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtLayoutName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))))
                .addContainerGap())
            .addComponent(panControls, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtLayoutName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboHotkeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboTransIn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboTransOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUpdateLayoutItem)
                            .addComponent(btnMoveDown)
                            .addComponent(btnMoveUp))
                        .addGap(80, 80, 80))
                    .addComponent(scroller, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void treeLayoutsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeLayoutsValueChanged
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
        txtLayoutName.setText("");
        txtLayoutName.setEnabled(false);
        cboTransIn.setEnabled(false);
        cboTransOut.setEnabled(false);
        spinX.setEnabled(false);
        spinY.setEnabled(false);
        spinWidth.setEnabled(false);
        spinHeight.setEnabled(false);
        btnUpdateLayoutItem.setEnabled(false);
        btnMoveDown.setEnabled(false);
        btnMoveUp.setEnabled(false);
        btnApply.setEnabled(false);
        btnRemove.setEnabled(false);
        cboHotkeys.setEnabled(false);

        Object obj = node.getUserObject();
        if (obj instanceof Layout) {
            currentLayout = (Layout) obj;
            btnApply.setEnabled(true);
            btnRemove.setEnabled(true);
            cboHotkeys.setEnabled(true);
            cboHotkeys.setSelectedItem(currentLayout.getHotKey());
            
            txtLayoutName.setText(currentLayout.toString());
            txtLayoutName.setEnabled(true);
            btnUpdateLayoutItem.setEnabled(true);
        } else if (obj instanceof LayoutItem) {
            currentLayoutItem = (LayoutItem) obj;
            if (evt.getPath().getParentPath() != null) {
                currentLayout = (Layout) ((DefaultMutableTreeNode) evt.getPath().getParentPath().getLastPathComponent()).getUserObject();
                txtLayoutName.setText(currentLayout.toString());
                cboHotkeys.setEnabled(true);
                cboHotkeys.setSelectedItem(currentLayout.getHotKey());
                txtLayoutName.setEnabled(true);
                cboTransIn.setEnabled(true);
                cboTransOut.setEnabled(true);
                spinX.setEnabled(true);
                spinY.setEnabled(true);
                spinWidth.setEnabled(true);
                spinHeight.setEnabled(true);
                btnUpdateLayoutItem.setEnabled(true);
                btnMoveDown.setEnabled(true);
                btnMoveUp.setEnabled(true);
                spinX.setValue(new Integer(currentLayoutItem.getX()));
                spinY.setValue(new Integer(currentLayoutItem.getY()));
                spinWidth.setValue(new Integer(currentLayoutItem.getWidth()));
                spinHeight.setValue(new Integer(currentLayoutItem.getHeight()));
                for (int i = 0; i < cboTransIn.getItemCount(); i++) {
                    if (currentLayoutItem.getTransitionIn().getClass().getName().equals(cboTransIn.getItemAt(i).getClass().getName())) {
                        cboTransIn.setSelectedIndex(i);
                        break;
                    }
                }
                for (int i = 0; i < cboTransOut.getItemCount(); i++) {
                    if (currentLayoutItem.getTransitionOut().getClass().getName().equals(cboTransIn.getItemAt(i).getClass().getName())) {
                        cboTransOut.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

    }//GEN-LAST:event_treeLayoutsValueChanged

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Layout l = new Layout("New Layout");
        for (VideoSource s : sources) {
            l.addSource(s, new None(), new None());
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

    private void btnUpdateLayoutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateLayoutItemActionPerformed
        if (currentLayout != null) {
            currentLayout.setName(txtLayoutName.getText());
            currentLayout.setHotKey(cboHotkeys.getSelectedItem().toString());
        }
        if (currentLayoutItem != null) {
            currentLayoutItem.setTransitionIn((Transition) cboTransIn.getSelectedItem());
            currentLayoutItem.setTransitionOut((Transition) cboTransOut.getSelectedItem());
            currentLayoutItem.setX((Integer) spinX.getValue());
            currentLayoutItem.setY((Integer) spinY.getValue());
            currentLayoutItem.setWidth((Integer) spinWidth.getValue());
            currentLayoutItem.setHeight((Integer) spinHeight.getValue());
        }
        treeLayouts.revalidate();
        treeLayouts.repaint();
    }//GEN-LAST:event_btnUpdateLayoutItemActionPerformed

    private void btnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyActionPerformed
        new Thread(new Runnable() {

            @Override
            public void run() {
                btnApply.setEnabled(false);
                if (currentLayout != null) {
                    if (oldLayout != null) {
                        oldLayout.exitLayout();
                    }
                    currentLayout.enterLayout();
                    oldLayout = currentLayout;
                }
                btnApply.setEnabled(true);
            }
        }).start();

    }//GEN-LAST:event_btnApplyActionPerformed

    private void btnMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveDownActionPerformed
        if (currentLayoutItem != null) {
            currentLayout.moveDownItem(currentLayoutItem);
            Enumeration en = root.children();

            while (en.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
                if (node.getUserObject().equals(currentLayout)) {
                    updateItems(currentLayout, node);
                    model.reload(node);
                    DefaultMutableTreeNode selectedNode = null;
                    Enumeration selEnum = node.children();
                    while (selEnum.hasMoreElements()) {
                        selectedNode = (DefaultMutableTreeNode) selEnum.nextElement();
                        if (selectedNode.getUserObject().equals(currentLayoutItem)) {
                            TreePath t = new TreePath(selectedNode.getPath());
                            treeLayouts.setSelectionPath(t);
                            break;
                        }
                    }
                }
            }
            treeLayouts.revalidate();
            treeLayouts.repaint();
        }
    }//GEN-LAST:event_btnMoveDownActionPerformed

    private void btnMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveUpActionPerformed
        if (currentLayoutItem != null) {
            currentLayout.moveUpItem(currentLayoutItem);
            Enumeration en = root.children();
            while (en.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
                if (node.getUserObject().equals(currentLayout)) {
                    updateItems(currentLayout, node);
                    model.reload(node);
                    DefaultMutableTreeNode selectedNode = null;
                    Enumeration selEnum = node.children();
                    while (selEnum.hasMoreElements()) {
                        selectedNode = (DefaultMutableTreeNode) selEnum.nextElement();
                        if (selectedNode.getUserObject().equals(currentLayoutItem)) {
                            TreePath t = new TreePath(selectedNode.getPath());
                            treeLayouts.setSelectionPath(t);
                            break;
                        }
                    }
                }
            }
            treeLayouts.revalidate();
            treeLayouts.repaint();
        }
    }//GEN-LAST:event_btnMoveUpActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        if (currentLayout != null) {
            layouts.remove(currentLayout);
            currentLayout = null;
            currentLayoutItem = null;
            updateTree();
            txtLayoutName.setText("");
            txtLayoutName.setEnabled(false);
            cboTransIn.setEnabled(false);
            cboTransOut.setEnabled(false);
            spinX.setEnabled(false);
            spinY.setEnabled(false);
            spinWidth.setEnabled(false);
            spinHeight.setEnabled(false);
            btnUpdateLayoutItem.setEnabled(false);
            btnMoveDown.setEnabled(false);
            btnMoveUp.setEnabled(false);
            btnApply.setEnabled(false);
            btnRemove.setEnabled(false);

        }
    }//GEN-LAST:event_btnRemoveActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnMoveDown;
    private javax.swing.JButton btnMoveUp;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnUpdateLayoutItem;
    private javax.swing.JComboBox cboHotkeys;
    private javax.swing.JComboBox cboTransIn;
    private javax.swing.JComboBox cboTransOut;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel lblName;
    private javax.swing.JPanel panControls;
    private javax.swing.JScrollPane scroller;
    private javax.swing.JSpinner spinHeight;
    private javax.swing.JSpinner spinWidth;
    private javax.swing.JSpinner spinX;
    private javax.swing.JSpinner spinY;
    private javax.swing.JTree treeLayouts;
    private javax.swing.JTextField txtLayoutName;
    // End of variables declaration//GEN-END:variables
}
