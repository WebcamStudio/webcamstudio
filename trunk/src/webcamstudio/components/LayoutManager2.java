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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class LayoutManager2 extends javax.swing.JPanel implements SourceListener {

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
    private DefaultListModel modelLayouts = new DefaultListModel();
    private DefaultListModel modelLayoutItems = new DefaultListModel();
    private Viewer viewer = new Viewer();
    private Mixer mixer;

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
        lstLayouts.setModel(modelLayouts);
        lstLayoutItems.setModel(modelLayoutItems);
        panPreview.add(viewer, BorderLayout.CENTER);
        viewer.setVisible(false);
        panPreview.setVisible(false);
        DefaultListCellRenderer rendererLayout = new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {
                Component comp = super.getListCellRendererComponent(list, value, index, selected, focus);
                JLabel label = (JLabel) comp;
                if (value instanceof Layout) {
                    Layout layout = (Layout) value;
                    label.setIcon(new ImageIcon(layout.getPreview().getScaledInstance(32, 32, BufferedImage.SCALE_FAST)));
                    label.setText(layout.toString());
                }
                return comp;
            }
        };
        lstLayouts.setCellRenderer(rendererLayout);
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
                    if (!v.isPlaying() || v.getImage() == null) {
                        label.setForeground(Color.GRAY);
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

                    } else {
                        label.setIcon(new ImageIcon(v.getImage().getScaledInstance(16, 16, BufferedImage.SCALE_FAST)));
                    }
                }
                return comp;
            }
        };
        lstLayoutItems.setCellRenderer(rendererLayoutItem);

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopMe) {
                    repaint();
                    try {
                        if (viewer.isVisible()) {
                            viewer.img = mixer.getImage();
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LayoutManager2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();

    }

    public void showPreview(boolean visible) {
        viewer.setVisible(visible);
        panPreview.setVisible(visible);
        panPreview.setPreferredSize(new Dimension(320, 240));
        panPreview.revalidate();
    }

    private void updateLayoutsList() {
        modelLayouts.clear();
        for (Layout l : layouts) {
            modelLayouts.addElement(l);
        }
        lstLayouts.setSelectedValue(currentLayout, true);
        updateLayoutItemList();
        lstLayouts.revalidate();
    }

    public void quitting() {
        eventsManager.stop();
        stopMe = true;
    }

    public Collection<Layout> getLayouts() {
        return layouts;
    }

    public void applyLayoutHotKey(String key) {
        //Find Layout in tree...
        for (Layout l : layouts) {
            if (l.getHotKey().equals(key)) {
                lstLayoutItems.setSelectedValue(l, true);
            }
        }
    }

    private void updateLayoutItemList() {
        modelLayoutItems.clear();
        if (currentLayout != null) {
            for (LayoutItem i : currentLayout.getReversedItems()) {
                modelLayoutItems.addElement(i);
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
        String tip = "<HTML><BODY>";
        for (LayoutItem li : layout.getReversedItems()) {
            tip += "<B>" + li.getSource().getName() + "</B><BR> X=" + li.getX() + ",Y=" + li.getY() + ", " + li.getWidth() + "x" + li.getHeight() + ", " + li.getTransitionIn().getName() + "-" + li.getTransitionOut().getName() + "<HR>";
        }
        tip += "</BODY></HTML>";
        lstLayouts.setToolTipText(tip);
    }

    public void addSource(VideoSource s) {
        if (layouts.isEmpty()) {
            Layout l = new Layout("New Layout");
            layouts.add(l);
            currentLayout = l;
            l.enterLayout();
        }
        if (currentLayout != null) {
            currentLayout.addSource(s);
            updateLayoutsList();
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
        popLayouts = new javax.swing.JPopupMenu();
        mnuLayoutsAdd = new javax.swing.JMenuItem();
        mnuLayoutsRemove = new javax.swing.JMenuItem();
        mnuLayoutsActivate = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuLayoutsRename = new javax.swing.JMenuItem();
        mainSplit = new javax.swing.JSplitPane();
        panLayoutItems = new javax.swing.JPanel();
        tabControls = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstLayoutItems = new javax.swing.JList();
        panLayouts = new javax.swing.JPanel();
        scrollLayouts = new javax.swing.JScrollPane();
        lstLayouts = new javax.swing.JList();
        panPreview = new javax.swing.JPanel();

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

        popLayouts.setName("popLayouts"); // NOI18N

        mnuLayoutsAdd.setText(bundle.getString("ADD")); // NOI18N
        mnuLayoutsAdd.setName("mnuLayoutsAdd"); // NOI18N
        mnuLayoutsAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLayoutsAddActionPerformed(evt);
            }
        });
        popLayouts.add(mnuLayoutsAdd);

        mnuLayoutsRemove.setText(bundle.getString("REMOVE")); // NOI18N
        mnuLayoutsRemove.setName("mnuLayoutsRemove"); // NOI18N
        mnuLayoutsRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLayoutsRemoveActionPerformed(evt);
            }
        });
        popLayouts.add(mnuLayoutsRemove);

        mnuLayoutsActivate.setText(bundle.getString("ACTIVATE")); // NOI18N
        mnuLayoutsActivate.setName("mnuLayoutsActivate"); // NOI18N
        mnuLayoutsActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLayoutsActivateActionPerformed(evt);
            }
        });
        popLayouts.add(mnuLayoutsActivate);

        jSeparator1.setName("jSeparator1"); // NOI18N
        popLayouts.add(jSeparator1);

        mnuLayoutsRename.setText(bundle.getString("RENAME")); // NOI18N
        mnuLayoutsRename.setName("mnuLayoutsRename"); // NOI18N
        mnuLayoutsRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLayoutsRenameActionPerformed(evt);
            }
        });
        popLayouts.add(mnuLayoutsRename);

        setLayout(new java.awt.BorderLayout());

        mainSplit.setName("mainSplit"); // NOI18N

        panLayoutItems.setName("panLayoutItems"); // NOI18N
        panLayoutItems.setLayout(new java.awt.BorderLayout());

        tabControls.setName("tabControls"); // NOI18N
        panLayoutItems.add(tabControls, java.awt.BorderLayout.CENTER);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        lstLayoutItems.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SOURCES"))); // NOI18N
        lstLayoutItems.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstLayoutItems.setName("lstLayoutItems"); // NOI18N
        lstLayoutItems.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstLayoutItemsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstLayoutItems);

        panLayoutItems.add(jScrollPane1, java.awt.BorderLayout.NORTH);

        mainSplit.setRightComponent(panLayoutItems);

        panLayouts.setName("panLayouts"); // NOI18N
        panLayouts.setLayout(new java.awt.BorderLayout());

        scrollLayouts.setName("scrollLayouts"); // NOI18N

        lstLayouts.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LAYOUTS"))); // NOI18N
        lstLayouts.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstLayouts.setName("lstLayouts"); // NOI18N
        lstLayouts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstLayoutsMouseClicked(evt);
            }
        });
        lstLayouts.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                lstLayoutsMouseMoved(evt);
            }
        });
        scrollLayouts.setViewportView(lstLayouts);

        panLayouts.add(scrollLayouts, java.awt.BorderLayout.CENTER);

        panPreview.setName("panPreview"); // NOI18N
        panPreview.setLayout(new java.awt.BorderLayout());
        panLayouts.add(panPreview, java.awt.BorderLayout.SOUTH);

        mainSplit.setLeftComponent(panLayouts);

        add(mainSplit, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void popItemsRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popItemsRemoveActionPerformed

        Object obj = lstLayoutItems.getSelectedValue();
        if (obj instanceof LayoutItem) {
            LayoutItem layoutItem = (LayoutItem) obj;
            VideoSource source = layoutItem.getSource();
            sourceRemoved(source);
        }
}//GEN-LAST:event_popItemsRemoveActionPerformed

    private void lstLayoutsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstLayoutsMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            popLayouts.setInvoker(lstLayouts);
            mnuLayoutsRemove.setEnabled(currentLayout != null);
            mnuLayoutsActivate.setEnabled(currentLayout != null);
            mnuLayoutsRename.setEnabled(currentLayout != null);
            popLayouts.setLocation(evt.getLocationOnScreen());
            popLayouts.setVisible(true);
        } else if (lstLayouts.getSelectedValue() != null) {
            currentLayout = (Layout) lstLayouts.getSelectedValue();
            currentLayoutItem = null;
            tabControls.removeAll();
            updateLayoutItemList();
            if (!Main.XMODE) {
                PulseAudioInputSelecter p = new PulseAudioInputSelecter(currentLayout);
                tabControls.add("Audio", p);
            }
            LayoutEventsControl eventsControl = new LayoutEventsControl(currentLayout, layouts);
            tabControls.add("Events", eventsControl);
        }
    }//GEN-LAST:event_lstLayoutsMouseClicked

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
                for (JPanel panel : currentLayoutItem.getSource().getControls()) {
                    Controls c = (Controls) panel;
                    tabControls.add(c.getLabel(), panel);
                    c.setListener(this);
                }
            }
        }
    }//GEN-LAST:event_lstLayoutItemsMouseClicked

    private void lstLayoutsMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstLayoutsMouseMoved
        int index = lstLayouts.locationToIndex(evt.getPoint());
        if (index != -1) {
            Layout layout = layouts.get(index);
            setLayoutToolTip(layout);
        } else {
            lstLayouts.setToolTipText("");
        }

    }//GEN-LAST:event_lstLayoutsMouseMoved

    private void mnuLayoutsAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLayoutsAddActionPerformed
        addLayout(new Layout("New Layout"));
    }//GEN-LAST:event_mnuLayoutsAddActionPerformed

    private void mnuLayoutsRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLayoutsRemoveActionPerformed
        if (currentLayout != null) {
            layouts.remove(currentLayout);
            currentLayout = null;
            currentLayoutItem = null;
            updateLayoutsList();
        }
    }//GEN-LAST:event_mnuLayoutsRemoveActionPerformed

    private void mnuLayoutsActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLayoutsActivateActionPerformed
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (currentLayout != null) {
                    currentLayout.enterLayout();
                }
            }
        }).start();
    }//GEN-LAST:event_mnuLayoutsActivateActionPerformed

    private void mnuLayoutsRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLayoutsRenameActionPerformed
        EditLayoutName editor = new EditLayoutName(null, true, currentLayout);
        editor.setLocationRelativeTo(lstLayouts);
        editor.setVisible(true);
        lstLayouts.revalidate();
    }//GEN-LAST:event_mnuLayoutsRenameActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JList lstLayoutItems;
    private javax.swing.JList lstLayouts;
    private javax.swing.JSplitPane mainSplit;
    private javax.swing.JMenuItem mnuLayoutsActivate;
    private javax.swing.JMenuItem mnuLayoutsAdd;
    private javax.swing.JMenuItem mnuLayoutsRemove;
    private javax.swing.JMenuItem mnuLayoutsRename;
    private javax.swing.JPanel panLayoutItems;
    private javax.swing.JPanel panLayouts;
    private javax.swing.JPanel panPreview;
    private javax.swing.JPopupMenu popItems;
    private javax.swing.JMenu popItemsDuplicateIn;
    private javax.swing.JMenuItem popItemsRemove;
    private javax.swing.JPopupMenu popLayouts;
    private javax.swing.JScrollPane scrollLayouts;
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
        l.setHotKey("F" + (layouts.size()));
        updateLayoutsList();
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
}
