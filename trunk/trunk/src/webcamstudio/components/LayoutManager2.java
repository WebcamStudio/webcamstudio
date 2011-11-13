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
import java.util.AbstractMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import webcamstudio.Main;
import webcamstudio.controls.ControlLayoutSources;
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
import webcamstudio.sources.VideoSourceText;
import webcamstudio.sources.VideoSourceV4L;
import webcamstudio.sources.VideoSourceWidget;

/**
 *
 * @author patrick
 */
public class LayoutManager2 extends javax.swing.JPanel implements SourceListener, AWTEventListener {

    private Layout currentLayout = null;
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
    protected Viewer viewer = new Viewer();
    private Timer timer = null;

    /** Creates new form LayoutManager2 */
    public LayoutManager2() {
        initComponents();
        iconMovie = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/video-display.png")));
        iconImage = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/image-x-generic.png")));
        iconDevice = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/camera-video.png")));
        iconAnimation = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/user-info.png")));
        iconFolder = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/folder.png")));
        iconText = new ImageIcon(getToolkit().getImage(java.net.URLClassLoader.getSystemResource("webcamstudio/resources/tango/format-text-bold.png")));
        eventsManager = new LayoutEventsManager(Layout.getLayouts());
        lstLayouts.setModel(modelLayouts);
        addLayout(new Layout("Default Layout"));
        currentLayout.enterLayout(false);
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
                    label.setIconTextGap(0);
                    label.setIcon(new ImageIcon(layout.getPreview(selected).getScaledInstance(180, 180 * 3 / 4, Image.SCALE_FAST)));
                    if (layout.isActive()) {
                        label.setForeground(Color.green);
                    } else {
                        label.setForeground(Color.black);
                    }
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
        for (Layout l : Layout.getLayouts().values()) {
            l.setDuration(0, "");
            for (LayoutItem li : l.getItems()) {
                li.getSource().stopSource();
            }
        }
        Layout.getLayouts().clear();
        modelLayouts.removeAllElements();
    }

    public void quitting() {
        eventsManager.stop();
        timer.cancel();
        stopMe = true;
    }


    public void applyLayoutHotKey(String key) {
        //Find Layout in tree...
        for (Layout l : Layout.getLayouts().values()) {
            if (l.getHotKey().equals(key)) {
                currentLayout = l;
                lstLayouts.setSelectedValue(l, true);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        currentLayout.enterLayout(false);
                    }
                }).start();

                break;
            }
        }
    }

    public void updateLayoutItemList() {
        modelLayoutItems.clear();
        if (currentLayout != null) {
            Object[] ls = currentLayout.getItems().toArray();
            for (int i = ls.length - 1; i >= 0; i--) {
                modelLayoutItems.addElement(ls[i]);
            }
        } 
        lstLayoutItems.revalidate();
        if (currentLayout != null) {
            lstLayoutItems.setSelectedValue(currentLayout.getItemSelected(), true);
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
        lstLayouts.setToolTipText(tip);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        popItems = new javax.swing.JPopupMenu();
        popItemsDuplicateIn = new javax.swing.JMenu();
        popItemsRemove = new javax.swing.JMenuItem();
        panLayoutItems = new javax.swing.JPanel();
        scrollItems = new javax.swing.JScrollPane();
        lstLayoutItems = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        btnAddLayout = new javax.swing.JButton();
        btnRemoveLayout = new javax.swing.JButton();
        btnActivateLayout = new javax.swing.JButton();
        btnLayoutDetails = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        tabControls = new javax.swing.JTabbedPane();
        lstLayoutsScroll = new javax.swing.JScrollPane();
        lstLayouts = new javax.swing.JList();

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
        lstLayoutItems.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstLayoutItemsValueChanged(evt);
            }
        });
        scrollItems.setViewportView(lstLayoutItems);

        panLayoutItems.add(scrollItems, java.awt.BorderLayout.CENTER);

        jPanel2.setName("jPanel2"); // NOI18N

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

        lstLayoutsScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        lstLayoutsScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        lstLayoutsScroll.setName("lstLayoutsScroll"); // NOI18N

        lstLayouts.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstLayouts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstLayouts.setName("lstLayouts"); // NOI18N
        lstLayouts.setSelectionBackground(java.awt.Color.white);
        lstLayouts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstLayoutsMouseClicked(evt);
            }
        });
        lstLayouts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lstLayoutsKeyPressed(evt);
            }
        });
        lstLayoutsScroll.setViewportView(lstLayouts);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(lstLayoutsScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addComponent(panLayoutItems, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panLayoutItems, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
            .addComponent(lstLayoutsScroll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
        );
    }// </editor-fold>                        

    private void popItemsRemoveActionPerformed(java.awt.event.ActionEvent evt) {                                               

        Object obj = lstLayoutItems.getSelectedValue();
        if (obj instanceof LayoutItem) {
            LayoutItem layoutItem = (LayoutItem) obj;
            VideoSource source = layoutItem.getSource();
            sourceRemoved(source);
        }
}                                              

    private void lstLayoutItemsMouseClicked(java.awt.event.MouseEvent evt) {                                            
        if (lstLayoutItems.getSelectedIndex() != -1) {
            if (evt.getButton() == MouseEvent.BUTTON3) {
                updatePopItemMenu();
                popItems.setInvoker(lstLayoutItems);
                popItems.setLocation(evt.getLocationOnScreen());
                popItems.setVisible(true);
            } else {
                LayoutItem currentLayoutItem = (LayoutItem) lstLayoutItems.getSelectedValue();
                currentLayout.setItemSelected(currentLayoutItem);
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
    }                                           

    private void btnAddLayoutActionPerformed(java.awt.event.ActionEvent evt) {                                             
        addLayout(new Layout("New Layout"));
    }                                            

    private void btnRemoveLayoutActionPerformed(java.awt.event.ActionEvent evt) {                                                
        Layout.removeLayout(currentLayout);
        modelLayouts.removeElement(currentLayout);
        lstLayouts.revalidate();
        currentLayout.setItemSelected(null);
        currentLayout = null;
    }                                               

    private void btnActivateLayoutActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (currentLayout != null) {
                    currentLayout.enterLayout(false);
                }
            }
        }).start();
    }                                                 

    private void btnLayoutDetailsActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        tabControls.removeAll();
        LayoutEventsControl control = new LayoutEventsControl(currentLayout, Layout.getLayouts().values());
        tabControls.add(currentLayout.toString(), control);
        if (!Main.XMODE) {
            PulseAudioInputSelecter pulse = new PulseAudioInputSelecter(currentLayout);
            tabControls.add("Pulseaudio", pulse);
        }
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        tabControls.add(bundle.getString("LAYOUTS"),new ControlLayoutSources(currentLayout,this));
    }                                                

private void lstLayoutsMouseClicked(java.awt.event.MouseEvent evt) {                                        
    if (lstLayouts.getSelectedValue() != null) {
        currentLayout = (Layout) lstLayouts.getSelectedValue();
        currentLayout.setItemSelected(null);
        tabControls.removeAll();
        updateLayoutItemList();
    }
    if (evt.getClickCount() == 2) {
        currentLayout.enterLayout(false);
    }
}                                       

private void lstLayoutsKeyPressed(java.awt.event.KeyEvent evt) {                                      
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        if (lstLayouts.getSelectedValue() != null) {
            currentLayout = (Layout) lstLayouts.getSelectedValue();
            currentLayout.setItemSelected(null);
            tabControls.removeAll();
            updateLayoutItemList();
            currentLayout.enterLayout(false);
        }
    }
}                                     

    private void lstLayoutItemsValueChanged(javax.swing.event.ListSelectionEvent evt) {                                            
    }                                           
    // Variables declaration - do not modify                     
    private javax.swing.JButton btnActivateLayout;
    private javax.swing.JButton btnAddLayout;
    private javax.swing.JButton btnLayoutDetails;
    private javax.swing.JButton btnRemoveLayout;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JList lstLayoutItems;
    private javax.swing.JList lstLayouts;
    private javax.swing.JScrollPane lstLayoutsScroll;
    private javax.swing.JPanel panLayoutItems;
    private javax.swing.JPopupMenu popItems;
    private javax.swing.JMenu popItemsDuplicateIn;
    private javax.swing.JMenuItem popItemsRemove;
    private javax.swing.JScrollPane scrollItems;
    private javax.swing.JTabbedPane tabControls;
    // End of variables declaration                   

    @Override
    public void sourceUpdate(VideoSource source) {
        updateLayoutItemList();
    }

    @Override
    public void sourceSetX(VideoSource source, int x) {
    }

    @Override
    public void sourceSetY(VideoSource source, int y) {
    }

    @Override
    public void sourceSetWidth(VideoSource source, int w) {
    }

    @Override
    public void sourceSetHeight(VideoSource source, int h) {
    }

    @Override
    public void sourceMoveUp(VideoSource source) {
        currentLayout.moveUpItem(currentLayout.getItemSelected());
        updateLayoutItemList();
    }

    @Override
    public void sourceMoveDown(VideoSource source) {
        currentLayout.moveDownItem(currentLayout.getItemSelected());
        updateLayoutItemList();
    }

    @Override
    public void sourceSetTransIn(VideoSource source, Transition in) {
        
    }

    @Override
    public void sourceSetTransOut(VideoSource source, Transition out) {
        
    }

    @Override
    public void sourceRemoved(VideoSource source) {
        source.stopSource();
        currentLayout.removeSource(source);
        tabControls.removeAll();
        updateLayoutItemList();
    }

    public void addLayout(Layout l) {
        Layout.addLayout(l);
        currentLayout = l;
        modelLayouts.addElement(l);
        lstLayouts.setSelectedValue(l, true);
    }

    public void updateLayouts(){
        modelLayouts.clear();
        for (Layout layout : Layout.getLayouts().values()){
            modelLayouts.addElement(layout);
            System.out.println("Layout ID: " + layout.getUUID());
        }
        lstLayoutItems.revalidate();
    }
    private void updatePopItemMenu() {

        final Object obj = lstLayoutItems.getSelectedValue();
        popItemsDuplicateIn.removeAll();
        if (obj instanceof LayoutItem) {
            popItemsDuplicateIn.setEnabled(true);
            popItemsRemove.setEnabled(true);
            for (Layout layout : Layout.getLayouts().values()) {
                JMenuItem menu = new JMenuItem(layout.toString());
                menu.setActionCommand(layout.getUUID());
                popItemsDuplicateIn.add(menu);
                ActionListener a = new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String UUID = ((JMenuItem) e.getSource()).getActionCommand();
                        LayoutItem layoutItem = (LayoutItem) obj;
                        VideoSource source = layoutItem.getSource();
                        Layout layout = Layout.getLayouts().get(UUID);
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
        lstLayouts.repaint();
        lstLayoutItems.repaint();
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        KeyEvent key = (KeyEvent) event;
        if (key.isAltDown()) {
            String c = key.getKeyChar() + "";
            for (Layout l : Layout.getLayouts().values()) {
                if (l.getHotKey().equals(c)) {
                    l.enterLayout(false);
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
