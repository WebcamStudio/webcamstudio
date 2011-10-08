/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LayoutManager2.java
 *
 * Created on 2011-06-04, 03:05:13
 */
package webcamstudio.components;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import webcamstudio.Main;
import webcamstudio.controls.ControlPosition;
import webcamstudio.controls.Controls;
import webcamstudio.layout.Layout;
import webcamstudio.layout.LayoutItem;
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
 * @author patrick
 */
public class LayoutManager2 extends javax.swing.JPanel implements SourceListener,AWTEventListener {

    private java.util.Vector<Layout> layouts = new java.util.Vector<Layout>();
    private Layout currentLayout = null;
    private LayoutItem currentLayoutItem = null;
    private ImageIcon iconMovie = null;
    private ImageIcon iconImage = null;
    private ImageIcon iconDevice = null;
    private ImageIcon iconAnimation = null;
    private ImageIcon iconFolder = null;
    private ImageIcon iconText = null;
    private boolean stopMe = false;
    private LayoutEventsManager eventsManager = null;
    //private DefaultListModel modelLayouts = new DefaultListModel();
    private DefaultComboBoxModel modelComboLayouts = new DefaultComboBoxModel();
    private DefaultListModel modelLayoutItems = new DefaultListModel();
    protected Viewer viewer = new Viewer();
    protected Mixer mixer;
    private Timer timer = null;

    /** Creates new form LayoutManager2 */
    public LayoutManager2(Mixer mix) {
        initComponents();
        this.mixer = mix;
        iconMovie = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/video-display.png")));
        iconImage = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/image-x-generic.png")));
        iconDevice = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/camera-video.png")));
        iconAnimation = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/user-info.png")));
        iconFolder = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/folder.png")));
        iconText = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/format-text-bold.png")));
        eventsManager = new LayoutEventsManager(layouts);
        cboLayouts.setModel(modelComboLayouts);
        addLayout(new Layout("Default Layout"));
        currentLayout.enterLayout();
        //lstLayouts.setModel(modelLayouts);
        lstLayoutItems.setModel(modelLayoutItems);
        viewer.setVisible(false);
        DefaultListCellRenderer rendererLayout = new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {
                Component comp = super.getListCellRendererComponent(list, value, index, selected, focus);
                JLabel label = (JLabel) comp;
                if (value instanceof Layout) {
                    Layout layout = (Layout) value;
                    label.setText("");
                    label.setIcon(new ImageIcon(layout.getPreview(mixer.getWidth(), mixer.getHeight()).getScaledInstance(180, 180*3/4, Image.SCALE_FAST)));
                    if (layout.isActive()) {
                        label.setForeground(Color.green);
                    } else {
                        label.setForeground(Color.black);
                    }
                }
                return comp;
            }
        };
        cboLayouts.setRenderer(rendererLayout);

        DefaultListCellRenderer rendererLayoutItem = new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {
                Component comp = super.getListCellRendererComponent(list, value, index, selected, focus);
                JLabel label = (JLabel) comp;
                if (value instanceof LayoutItem) {
                    LayoutItem item = (LayoutItem) value;
                    VideoSource v = item.getSource();
                    label.setText(v.getName());
                    label.setToolTipText(v.getLocation());

                    label.setForeground(Color.GRAY);
                    if (v.isPlaying()) {
                        label.setForeground(Color.BLACK);
                    }
                    if (v instanceof VideoSourceImage) {
                        VideoSourceImage source = (VideoSourceImage) v;
                        if (source.getThumnail() == null) {
                            label.setIcon(iconImage);
                        } else {
                            label.setIcon(new ImageIcon(source.getThumnail().getScaledInstance(16, 16, Image.SCALE_FAST)));
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
                }
                return comp;
            }
        };
        lstLayoutItems.setCellRenderer(rendererLayoutItem);
        timer = new Timer(this.getClass().getSimpleName(), true);

        timer.scheduleAtFixedRate(new imageUpdater(this), 0, 200);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, ActionEvent.KEY_EVENT_MASK);

    }

    public void clearLayouts() {
        for (Layout l : layouts) {
            l.setDuration(0, "");
            for (LayoutItem li : l.getItems()) {
                li.getSource().stopSource();
            }
        }
        layouts.removeAllElements();
        modelComboLayouts.removeAllElements();
        layouts.removeAllElements();
    }

    public void quitting() {
        eventsManager.stop();
        timer.cancel();
        stopMe = true;
    }

    public Collection<Layout> getLayouts() {
        return layouts;
    }

    public void applyLayoutHotKey(String key) {
        //Find Layout in tree...
        for (Layout l : layouts) {
            if (l.getHotKey().equals(key)) {
                currentLayout = l;
                cboLayouts.setSelectedItem(l);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        currentLayout.enterLayout();
                    }
                }).start();

                break;
            }
        }
    }

    private void updateLayoutItemList() {
        modelLayoutItems.clear();
        if (currentLayout != null) {
            Object[] ls = currentLayout.getItems().toArray();
            for (int i = ls.length - 1; i >= 0; i--) {
                modelLayoutItems.addElement(ls[i]);
            }
        } else {
            currentLayoutItem = null;
        }
        lstLayoutItems.revalidate();
        if (currentLayoutItem != null) {
            lstLayoutItems.setSelectedValue(currentLayoutItem, true);
        }
    }

    private void setLayoutToolTip(Layout layout) {
        String tip = "<HTML><BODY><H3>" + layout.toString() + "</H3><BR>";
        Object[] ls = currentLayout.getItems().toArray();
        for (int i = ls.length - 1; i >= 0; i--) {
            LayoutItem li = (LayoutItem) ls[i];
            tip += "<B>" + li.getSource().getName() + "</B><BR> X=" + li.getX() + ",Y=" + li.getY() + ", " + li.getWidth() + "x" + li.getHeight() + ", " + li.getTransitionIn().getName() + "-" + li.getTransitionOut().getName() + "<HR>";
        }
        tip += "</BODY></HTML>";
        cboLayouts.setToolTipText(tip);
    }

    public void addSource(VideoSource s) {
        if (currentLayout != null) {
            currentLayout.addSource(s);
            updateLayoutItemList();
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

        popItems = new javax.swing.JPopupMenu();
        popItemsDuplicateIn = new javax.swing.JMenu();
        popItemsRemove = new javax.swing.JMenuItem();
        panLayoutItems = new javax.swing.JPanel();
        scrollItems = new javax.swing.JScrollPane();
        lstLayoutItems = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        cboLayouts = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        btnAddLayout = new javax.swing.JButton();
        btnRemoveLayout = new javax.swing.JButton();
        btnActivateLayout = new javax.swing.JButton();
        btnLayoutDetails = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        tabControls = new javax.swing.JTabbedPane();

        popItems.setName("popItems"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        popItemsDuplicateIn.setText(bundle.getString("DUPLICATE_IN_LAYOUT")); // NOI18N
        popItemsDuplicateIn.setName("popItemsDuplicateIn"); // NOI18N
        popItems.add(popItemsDuplicateIn);

        popItemsRemove.setText(bundle.getString("REMOVE")); // NOI18N
        popItemsRemove.setName("popItemsRemove"); // NOI18N
        popItemsRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popItemsRemoveActionPerformed(evt);
            }
        });
        popItems.add(popItemsRemove);

        panLayoutItems.setName("panLayoutItems"); // NOI18N
        panLayoutItems.setLayout(new java.awt.BorderLayout());

        scrollItems.setName("scrollItems"); // NOI18N

        lstLayoutItems.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SOURCES"))); // NOI18N
        lstLayoutItems.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstLayoutItems.setDoubleBuffered(true);
        lstLayoutItems.setName("lstLayoutItems"); // NOI18N
        lstLayoutItems.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstLayoutItemsMouseClicked(evt);
            }
        });
        scrollItems.setViewportView(lstLayoutItems);

        panLayoutItems.add(scrollItems, java.awt.BorderLayout.CENTER);

        jPanel2.setName("jPanel2"); // NOI18N

        cboLayouts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboLayouts.setName("cboLayouts"); // NOI18N
        cboLayouts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLayoutsActionPerformed(evt);
            }
        });
        jPanel2.add(cboLayouts);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        btnAddLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-new.png"))); // NOI18N
        btnAddLayout.setToolTipText(bundle.getString("ADD")); // NOI18N
        btnAddLayout.setName("btnAddLayout"); // NOI18N
        btnAddLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddLayoutActionPerformed(evt);
            }
        });
        jPanel3.add(btnAddLayout);

        btnRemoveLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/edit-delete.png"))); // NOI18N
        btnRemoveLayout.setToolTipText(bundle.getString("REMOVE")); // NOI18N
        btnRemoveLayout.setName("btnRemoveLayout"); // NOI18N
        btnRemoveLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveLayoutActionPerformed(evt);
            }
        });
        jPanel3.add(btnRemoveLayout);

        btnActivateLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        btnActivateLayout.setToolTipText(bundle.getString("ACTIVATE")); // NOI18N
        btnActivateLayout.setName("btnActivateLayout"); // NOI18N
        btnActivateLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActivateLayoutActionPerformed(evt);
            }
        });
        jPanel3.add(btnActivateLayout);

        btnLayoutDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/document-properties.png"))); // NOI18N
        btnLayoutDetails.setToolTipText(bundle.getString("PROPERTIES")); // NOI18N
        btnLayoutDetails.setName("btnLayoutDetails"); // NOI18N
        btnLayoutDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLayoutDetailsActionPerformed(evt);
            }
        });
        jPanel3.add(btnLayoutDetails);

        jPanel2.add(jPanel3);

        panLayoutItems.add(jPanel2, java.awt.BorderLayout.WEST);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        tabControls.setName("tabControls"); // NOI18N
        jPanel1.add(tabControls, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panLayoutItems, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panLayoutItems, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void popItemsRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popItemsRemoveActionPerformed

        Object obj = lstLayoutItems.getSelectedValue();
        if (obj instanceof LayoutItem) {
            LayoutItem layoutItem = (LayoutItem) obj;
            VideoSource source = layoutItem.getSource();
            sourceRemoved(source);
        }
}//GEN-LAST:event_popItemsRemoveActionPerformed

    private void lstLayoutItemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstLayoutItemsMouseClicked
        if (lstLayoutItems.getSelectedIndex() != -1) {
            if (evt.getButton() == MouseEvent.BUTTON3) {
                updatePopItemMenu();
                popItems.setInvoker(lstLayoutItems);
                popItems.setLocation(evt.getLocationOnScreen());
                popItems.setVisible(true);
            } else {
                currentLayoutItem = (LayoutItem) lstLayoutItems.getSelectedValue();
                tabControls.removeAll();
                ControlPosition ctrl = new ControlPosition(currentLayoutItem);
                ctrl.setListener(this);
                tabControls.add(ctrl.getLabel(), ctrl);
                for (Controls control : currentLayoutItem.getSource().getControls()) {
                    tabControls.add(control.getLabel(), (JPanel) control);
                    control.setListener(this);
                }
            }
        }
    }//GEN-LAST:event_lstLayoutItemsMouseClicked

    private void btnAddLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddLayoutActionPerformed
        addLayout(new Layout("New Layout"));
    }//GEN-LAST:event_btnAddLayoutActionPerformed

    private void btnRemoveLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveLayoutActionPerformed
        layouts.remove(currentLayout);
        cboLayouts.removeItem(currentLayout);
        currentLayout = null;
        currentLayoutItem = null;

    }//GEN-LAST:event_btnRemoveLayoutActionPerformed

    private void btnActivateLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActivateLayoutActionPerformed
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (currentLayout != null) {
                    currentLayout.enterLayout();
                }
            }
        }).start();
    }//GEN-LAST:event_btnActivateLayoutActionPerformed

    private void cboLayoutsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboLayoutsActionPerformed
        if (cboLayouts.getSelectedItem() != null) {
            currentLayout = (Layout) cboLayouts.getSelectedItem();
            tabControls.removeAll();
            updateLayoutItemList();
        }

    }//GEN-LAST:event_cboLayoutsActionPerformed

    private void btnLayoutDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLayoutDetailsActionPerformed
        tabControls.removeAll();
        LayoutEventsControl control = new LayoutEventsControl(currentLayout, layouts);
        tabControls.add(currentLayout.toString(), control);
        if (!Main.XMODE) {
            PulseAudioInputSelecter pulse = new PulseAudioInputSelecter(currentLayout);
            tabControls.add("Pulseaudio", pulse);
        }
    }//GEN-LAST:event_btnLayoutDetailsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivateLayout;
    private javax.swing.JButton btnAddLayout;
    private javax.swing.JButton btnLayoutDetails;
    private javax.swing.JButton btnRemoveLayout;
    private javax.swing.JComboBox cboLayouts;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JList lstLayoutItems;
    private javax.swing.JPanel panLayoutItems;
    private javax.swing.JPopupMenu popItems;
    private javax.swing.JMenu popItemsDuplicateIn;
    private javax.swing.JMenuItem popItemsRemove;
    private javax.swing.JScrollPane scrollItems;
    private javax.swing.JTabbedPane tabControls;
    // End of variables declaration//GEN-END:variables

    @Override
    public void sourceUpdate(VideoSource source) {
        updateLayoutItemList();
    }

    @Override
    public void sourceSetX(VideoSource source, int x) {
        currentLayoutItem.setX(x);
    }

    @Override
    public void sourceSetY(VideoSource source, int y) {
        currentLayoutItem.setY(y);
    }

    @Override
    public void sourceSetWidth(VideoSource source, int w) {
        currentLayoutItem.setWidth(w);
    }

    @Override
    public void sourceSetHeight(VideoSource source, int h) {
        currentLayoutItem.setHeight(h);
    }

    @Override
    public void sourceMoveUp(VideoSource source) {
        currentLayout.moveUpItem(currentLayoutItem);
        updateLayoutItemList();
    }

    @Override
    public void sourceMoveDown(VideoSource source) {
        currentLayout.moveDownItem(currentLayoutItem);
        updateLayoutItemList();
    }

    @Override
    public void sourceSetTransIn(VideoSource source, Transition in) {
        currentLayoutItem.setTransitionIn(in);
    }

    @Override
    public void sourceSetTransOut(VideoSource source, Transition out) {
        currentLayoutItem.setTransitionOut(out);
    }

    @Override
    public void sourceRemoved(VideoSource source) {
        source.stopSource();
        currentLayout.removeSource(source);
        currentLayoutItem = null;
        tabControls.removeAll();
        updateLayoutItemList();
    }

    public void addLayout(Layout l) {
        layouts.add(l);
        currentLayout = l;
        modelComboLayouts.addElement(l);
        cboLayouts.setSelectedItem(l);
    }

    private void updatePopItemMenu() {

        final Object obj = lstLayoutItems.getSelectedValue();
        popItemsDuplicateIn.removeAll();
        if (obj instanceof LayoutItem) {
            popItemsDuplicateIn.setEnabled(true);
            popItemsRemove.setEnabled(true);
            for (int i = 0; i < layouts.size(); i++) {
                JMenuItem menu = new JMenuItem(layouts.get(i).toString(), i);
                popItemsDuplicateIn.add(menu);
                ActionListener a = new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int index = ((JMenuItem) e.getSource()).getMnemonic();

                        LayoutItem layoutItem = (LayoutItem) obj;
                        VideoSource source = layoutItem.getSource();
                        Layout layout = layouts.get(index);
                        layout.addSource(source);
                    }
                };
                menu.addActionListener(a);
            }
        } else {
            popItemsDuplicateIn.setEnabled(false);
            popItemsRemove.setEnabled(false);
        }


    }

    protected void update() {
        cboLayouts.repaint();
        lstLayoutItems.repaint();
    }

    @Override
    public void eventDispatched(AWTEvent event) {
       KeyEvent key = (KeyEvent)event;
       if (key.isAltDown()){
           String c = key.getKeyChar()+"";
           for (Layout l : layouts){
               if (l.getHotKey().equals(c)){
                   l.enterLayout();
               }
           }
       }
    }
}
class imageUpdater extends TimerTask {

    private LayoutManager2 layoutManager = null;

    public imageUpdater(LayoutManager2 l) {
        layoutManager = l;
    }

    @Override
    public void run() {
        layoutManager.update();
    }
}
