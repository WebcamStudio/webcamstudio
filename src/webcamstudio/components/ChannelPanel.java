/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChannelPanel.java
 *
 * Created on 23-Apr-2012, 12:17:31 AM
 */
package webcamstudio.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import webcamstudio.WebcamStudio;
import webcamstudio.channels.MasterChannels;
import webcamstudio.mixers.PrePlayer;
import webcamstudio.mixers.SystemPlayer;
import webcamstudio.remote.Listener;
import webcamstudio.remote.WebRemote;
import webcamstudio.streams.SourceChannel;
import webcamstudio.streams.Stream;
import webcamstudio.studio.Studio;
import webcamstudio.util.Tools;




/**
 *
 * @author patrick (modified by karl)
 */
public class ChannelPanel extends javax.swing.JPanel implements WebcamStudio.Listener, Studio.Listener, Listener {
    
    MasterChannels master = MasterChannels.getInstance();
    private final DefaultListModel model = new DefaultListModel();
    private final DefaultComboBoxModel aModel = new DefaultComboBoxModel();
    private final ArrayList<String> CHCurrNext = new ArrayList<>();
    private final ArrayList<Integer> CHTimers = new ArrayList<>();
    private final ArrayList<String> ListChannels = new ArrayList<>();
    private final WebRemote remote;
    ArrayList<Stream> streamS = MasterChannels.getInstance().getStreams();
    String selectChannel=null;   
    int CHon =0;
    String CHNxName = null;
    int CHNextTime =0;
    int CHTimer = 0;
    private Timer CHt=new Timer();
    String CHptS= null;
    private Boolean StopCHpt=false;
    boolean inTimer=false;
    JPopupMenu remotePopup = new JPopupMenu();
    private static String remUser = "webcamstudio";
    private static String remPsw = "webcamstudio";
    private static int remPort = 8000;
    Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    
    @Override
    public void resetAutoPLBtnState(ActionEvent evt) {
        btnAutoPlayList.setEnabled(true);
    }

    @Override
    public void requestStart() {
        btnSelect.doClick();
        listenerCPOP.requestStart();
    }

    @Override
    public void requestStop() {
        btnStopOnlyStream.doClick();
        listenerCPOP.requestStop();
    }

    @Override
    public void listening(String localURL) {
        
    }

    @Override
    public void requestReset() {
        listenerCPMP.requestReset();
        listenerCPOP.requestReset();
    }

    @Override
    public void resetSinks(ActionEvent evt) { // used resetSinks to AutoPlay from command line.
        btnSelect.doClick();
    }

    @Override
    public String requestlogin(String login) {
        String res = "";
        String [] loginSplit = login.split("\\?");
        String userPsw = loginSplit[1].replace("j_username=", "");
        userPsw = userPsw.replace("j_password=", "");
        userPsw = userPsw.replace(" HTTP/1.1", "");
//        System.out.println("userPsw: "+userPsw);
        if (!userPsw.equals("&")) {
            String [] userPswSplit = userPsw.split("&");
            if (!userPsw.equals("&")) 
            if (userPswSplit[0].equals(remUser) && userPswSplit[1].equals(remPsw)) {
                boolean play = false;
                for (Stream stream : streamS) {
                    if (!stream.getClass().toString().contains("Sink")) {
                        if (stream.isPlaying()){
                            play = true;
                        }
                    }
                }
                if (play) {
                    res = "/run";
                } else {
                    res = "/stop";
                }
            } else {
                res = "/error";
            }
        } else {
            res = "/login";
        }
        return res;
    }

    @Override
    public void setRemoteOn() {
        tglRemote.doClick();
    }

    public interface Listener {
        public void resetButtonsStates(ActionEvent evt);
        public void requestReset();
        public void requestStop();
        public void requestStart();
    }
    static Listener listenerCPOP = null;
    public static void setListenerCPOPanel(Listener l) {
        listenerCPOP = l;
    }
    static Listener listenerCPMP = null;
    public static void setListenerCPMPanel(Listener l) {
        listenerCPMP = l;
    }
    
    /**
     * Creates new form ChannelPanel
     */
    @SuppressWarnings("unchecked") 
    public ChannelPanel() {
        initComponents();
        remoteInitPopUp();
        final ChannelPanel instanceChPnl = this;
        lstChannels.setModel(model);
        lstNextChannel.setModel(aModel);
        WebcamStudio.setListenerCP(instanceChPnl);
        Studio.setListener(this);
        remote = new WebRemote(this);
        loadPrefs();
        
    }
    
    private void loadPrefs() {
        remUser = preferences.get("remoteuser", "webcamstudio");
        remPsw = preferences.get("remotepsw", "webcamstudio");
        remPort = preferences.getInt("remoteport", 8000);
        remote.setPort(remPort);
    }
    
    public void savePrefs() {
        preferences.put("remoteuser", remUser);
        preferences.put("remotepsw", remPsw);
        preferences.putInt("remoteport", remPort);
    }
    public static void setRemPsw (String psw) {
        remPsw = psw;
    }
    
    public static String getRemPsw () {
        return remPsw;
    }  
    
    public static void setRemUsr (String usr) {
        remUser = usr;
    }
    
    public static String getRemUsr () {
        return remUser;
    }
    
    public static void setRemPort (int port) {
        remPort = port;
    }
    
    public static int getRemPort () {
        return remPort;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lstChannelsScroll = new javax.swing.JScrollPane();
        lstChannels = new javax.swing.JList();
        lblChName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnSelect = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        lstNextChannel = new javax.swing.JComboBox();
        ChDuration = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        StopCHTimer = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        CHProgressTime = new javax.swing.JProgressBar();
        btnStopAllStream = new javax.swing.JButton();
        btnRenameCh = new javax.swing.JButton();
        btnAutoPlayList = new javax.swing.JButton();
        btnUp = new javax.swing.JButton();
        btnDown = new javax.swing.JButton();
        btnClearAllCh = new javax.swing.JButton();
        tglRemote = new javax.swing.JToggleButton();
        btnStopOnlyStream = new javax.swing.JButton();

        lstChannelsScroll.setName("lstChannelsScroll"); // NOI18N

        lstChannels.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstChannels.setToolTipText("Double Click to play selected channel");
        lstChannels.setName("lstChannels"); // NOI18N
        lstChannels.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstChannelsMouseClicked(evt);
            }
        });
        lstChannels.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                lstChannelsComponentAdded(evt);
            }
        });
        lstChannels.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstChannelsValueChanged(evt);
            }
        });
        lstChannelsScroll.setViewportView(lstChannels);

        lblChName.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("webcamstudio/Languages"); // NOI18N
        lblChName.setText(bundle.getString("Name")); // NOI18N
        lblChName.setName("lblChName"); // NOI18N

        txtName.setName("txtName"); // NOI18N

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/list-add.png"))); // NOI18N
        btnAdd.setToolTipText(bundle.getString("ADD_CHANNEL")); // NOI18N
        btnAdd.setName("btnAdd"); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/process-stop.png"))); // NOI18N
        btnRemove.setToolTipText(bundle.getString("REMOVE_CHANNEL")); // NOI18N
        btnRemove.setEnabled(false);
        btnRemove.setName("btnRemove"); // NOI18N
        btnRemove.setPreferredSize(new java.awt.Dimension(32, 30));
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-start.png"))); // NOI18N
        btnSelect.setToolTipText(bundle.getString("APPLY_CHANNEL")); // NOI18N
        btnSelect.setEnabled(false);
        btnSelect.setMinimumSize(new java.awt.Dimension(32, 30));
        btnSelect.setName("btnSelect"); // NOI18N
        btnSelect.setPreferredSize(new java.awt.Dimension(32, 30));
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/view-refresh.png"))); // NOI18N
        btnUpdate.setToolTipText(bundle.getString("UPDATE_CHANNEL")); // NOI18N
        btnUpdate.setEnabled(false);
        btnUpdate.setMinimumSize(new java.awt.Dimension(32, 25));
        btnUpdate.setName("btnUpdate"); // NOI18N
        btnUpdate.setPreferredSize(new java.awt.Dimension(32, 30));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        lstNextChannel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        lstNextChannel.setName("lstNextChannel"); // NOI18N
        lstNextChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lstNextChannelActionPerformed(evt);
            }
        });

        ChDuration.setToolTipText("0 = Infinite");
        ChDuration.setName("ChDuration"); // NOI18N
        ChDuration.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ChDurationStateChanged(evt);
            }
        });

        jLabel2.setText(bundle.getString("NEXT_CHANNEL")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(bundle.getString("DURATION")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        StopCHTimer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop-bk.png"))); // NOI18N
        StopCHTimer.setText(bundle.getString("STOP_CHANNEL_TIMER")); // NOI18N
        StopCHTimer.setToolTipText("Stop Timer Only");
        StopCHTimer.setEnabled(false);
        StopCHTimer.setName("StopCHTimer"); // NOI18N
        StopCHTimer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StopCHTimerActionPerformed(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(bundle.getString("CURRENT_CHANNEL_TIMER")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        CHProgressTime.setName("CHProgressTime"); // NOI18N

        btnStopAllStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop-bk.png"))); // NOI18N
        btnStopAllStream.setText(bundle.getString("STOP_ALL")); // NOI18N
        btnStopAllStream.setToolTipText("Stop All");
        btnStopAllStream.setName("btnStopAllStream"); // NOI18N
        btnStopAllStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopAllStreamActionPerformed(evt);
            }
        });

        btnRenameCh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/edit.png"))); // NOI18N
        btnRenameCh.setToolTipText(bundle.getString("RENAME_CHANNEL")); // NOI18N
        btnRenameCh.setEnabled(false);
        btnRenameCh.setName("btnRenameCh"); // NOI18N
        btnRenameCh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenameChActionPerformed(evt);
            }
        });

        btnAutoPlayList.setText("Auto Play-List");
        btnAutoPlayList.setToolTipText("Do an Automatic PlayList. Works Only with \"Load Media Folder\".");
        btnAutoPlayList.setEnabled(false);
        btnAutoPlayList.setName("btnAutoPlayList"); // NOI18N
        btnAutoPlayList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutoPlayListActionPerformed(evt);
            }
        });

        btnUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-up.png"))); // NOI18N
        btnUp.setToolTipText("Move Channel UP");
        btnUp.setEnabled(false);
        btnUp.setName("btnUp"); // NOI18N
        btnUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpActionPerformed(evt);
            }
        });

        btnDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/go-down.png"))); // NOI18N
        btnDown.setToolTipText("Move Channel DOWN");
        btnDown.setEnabled(false);
        btnDown.setName("btnDown"); // NOI18N
        btnDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownActionPerformed(evt);
            }
        });

        btnClearAllCh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/button-small-clear.png"))); // NOI18N
        btnClearAllCh.setToolTipText("Remove All Channels");
        btnClearAllCh.setEnabled(false);
        btnClearAllCh.setName("btnClearAllCh"); // NOI18N
        btnClearAllCh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAllChActionPerformed(evt);
            }
        });

        tglRemote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/rss.png"))); // NOI18N
        tglRemote.setToolTipText("Remote Control (Beta) - Right Click for Settings");
        tglRemote.setEnabled(false);
        tglRemote.setFocusable(false);
        tglRemote.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tglRemote.setMaximumSize(new java.awt.Dimension(29, 28));
        tglRemote.setMinimumSize(new java.awt.Dimension(25, 25));
        tglRemote.setName("tglRemote"); // NOI18N
        tglRemote.setPreferredSize(new java.awt.Dimension(28, 29));
        tglRemote.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/rss.png"))); // NOI18N
        tglRemote.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/rss_selected.png"))); // NOI18N
        tglRemote.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tglRemote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglRemoteActionPerformed(evt);
            }
        });
        tglRemote.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                JToggleButton button = ((JToggleButton) evt.getSource());
                if (!button.isSelected()) {
                    remoteRightMousePressed(evt);
                }
            }
        });

        btnStopOnlyStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/webcamstudio/resources/tango/media-playback-stop-bk.png"))); // NOI18N
        btnStopOnlyStream.setText(bundle.getString("STREAMS")); // NOI18N
        btnStopOnlyStream.setToolTipText("Stop Streams Only");
        btnStopOnlyStream.setName("btnStopOnlyStream"); // NOI18N
        btnStopOnlyStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopOnlyStreamActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(StopCHTimer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblChName)
                        .addGap(1, 1, 1)
                        .addComponent(txtName)
                        .addGap(1, 1, 1)
                        .addComponent(btnUp, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(btnDown, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(btnRenameCh, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lstChannelsScroll)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(CHProgressTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ChDuration)
                            .addComponent(lstNextChannel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnClearAllCh, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAutoPlayList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglRemote, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnStopAllStream, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStopOnlyStream, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRenameCh, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblChName))
                    .addComponent(btnDown, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUp, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lstChannelsScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAutoPlayList, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnClearAllCh, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tglRemote, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lstNextChannel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(ChDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CHProgressTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StopCHTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStopAllStream, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStopOnlyStream, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5))
        );

        btnStopAllStream.getAccessibleContext().setAccessibleParent(StopCHTimer);
    }// </editor-fold>//GEN-END:initComponents

    private void lstChannelsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstChannelsValueChanged
        if (lstChannels.getSelectedIndex() != -1) {
            selectChannel = lstChannels.getSelectedValue().toString();
            int SelectCHIndex = lstChannels.getSelectedIndex();
            lstNextChannel.setSelectedItem(CHCurrNext.get(SelectCHIndex));
            ChDuration.setValue(CHTimers.get(SelectCHIndex)/1000);
            btnRemove.setEnabled(!inTimer);
            btnUp.setEnabled(!inTimer);
            btnDown.setEnabled(!inTimer);
            btnSelect.setEnabled(!inTimer);
            btnRenameCh.setEnabled(!inTimer);
            btnAdd.setEnabled(!inTimer);
            btnClearAllCh.setEnabled(!inTimer);
            StopCHTimer.setEnabled(inTimer);
            btnUpdate.setEnabled(true);
            tglRemote.setEnabled(true);
            } else {
                btnRemove.setEnabled(false);
                btnSelect.setEnabled(false);
                btnUpdate.setEnabled(false);
                btnClearAllCh.setEnabled(false);
                tglRemote.setEnabled(false);
        }
    }//GEN-LAST:event_lstChannelsValueChanged
    @SuppressWarnings("unchecked") 
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        String name = txtName.getText();
        boolean noDuplicateCh = true;
        for (String chName : ListChannels){
            if (name.equals(chName)){
                noDuplicateCh = false;
                break;
            }
        }
        
        if (name.length() > 0 && noDuplicateCh) {
            master.addChannel(name);
            master.addChTransitions(name);
//            master.addFontsText(name);
            model.addElement(name);
            aModel.addElement(name);
            CHCurrNext.add(name);
            CHTimers.add(CHTimer);
            ListChannels.add(name);
            lstChannels.revalidate();
            lstNextChannel.revalidate();
            lstChannels.setSelectedValue(name, true);
        } else {
            if (!noDuplicateCh){
                ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Channel "+name+" Duplicated !!!");
                ResourceMonitor.getInstance().addMessage(label);
            }
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        String name = lstChannels.getSelectedValue().toString();
        int SelectCHIndex = lstChannels.getSelectedIndex();
        master.removeChannel(name);
        model.removeElement(name);
        aModel.removeElement(name);
        CHCurrNext.remove(name);
        CHTimers.remove(SelectCHIndex);
        ChDuration.setValue(0);
        ListChannels.remove(name);
        lstChannels.revalidate();
        lstNextChannel.revalidate();
        btnRenameCh.setEnabled(false);
        btnUp.setEnabled(false);
        btnDown.setEnabled(false);
        btnRemove.setEnabled(false);
        StopCHTimer.setEnabled(inTimer);
    }//GEN-LAST:event_btnRemoveActionPerformed

    @Override
    public ArrayList<String> getCHCurrNext () {
        return CHCurrNext;
    }
    @Override
    public ArrayList<Integer> getCHTimers () {
        return CHTimers;
    }   
    
    private void remoteInitPopUp(){
        JMenuItem remoteSettings = new JMenuItem (new AbstractAction("Remote Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePrefs();
                RemoteSettings remoteSet = new RemoteSettings();
                remoteSet.setLocationRelativeTo(WebcamStudio.cboAnimations);
                remoteSet.setAlwaysOnTop(true);
                remoteSet.setVisible(true);
            }
        });
        remoteSettings.setIcon(new ImageIcon(getClass().getResource("/webcamstudio/resources/tango/working-4.png"))); // NOI18N
        remotePopup.add(remoteSettings);
    }
    
    private void remoteRightMousePressed(java.awt.event.MouseEvent evt) {                                      
        if (evt.isPopupTrigger()) {
            remotePopup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }     
    
    @Override
    public void removeChannels(String removeSc, int a) {
        model.removeElement(removeSc);
        aModel.removeElement(removeSc);
        CHCurrNext.remove(removeSc);
        CHTimers.remove(a);
        ListChannels.remove(removeSc);
    }   
    @SuppressWarnings("unchecked") 
    @Override
    public void addLoadingChannel(String name) {
        if (name.length() > 0) {
            model.addElement(name);
            aModel.addElement(name);
            ListChannels.add(name);
            lstChannels.revalidate();
       }
        lstChannels.setSelectedValue(ListChannels.get(0), true);
    }   
    @Override
    public void stopChTime(ActionEvent evt) {
        RemoteStopCHTimerActionPerformed();
    }
    
    @Override
    public void resetBtnStates(ActionEvent evt) {
        btnRenameCh.setEnabled(false);
        btnClearAllCh.setEnabled(false);
        btnUp.setEnabled(false);
        btnDown.setEnabled(false);
        btnRemove.setEnabled(false);
        btnSelect.setEnabled(false);
        txtName.setText("");
        CHCurrNext.clear();
        CHTimers.clear();
        StopCHTimer.setEnabled(false);
        ChDuration.setValue(0);
    }

    class UpdateCHtUITask extends TimerTask {
        @Override
        public void run() {
            CHptS=null;
            int CHpTemptime = CHNextTime/1000;
            CHProgressTime.setValue(0);
            CHProgressTime.setStringPainted(true);
            CHProgressTime.setMaximum(CHpTemptime);             
            while (CHpTemptime>0 && StopCHpt==false){
                CHptS = Integer.toString(CHpTemptime);
                CHProgressTime.setValue(CHpTemptime);
                CHProgressTime.setString(CHptS);
                Tools.sleep(1000);
                CHpTemptime -= 1;
            }
            UpdateCHtUITask.this.stop();
        }
        public void stop() {
            StopCHpt=true;
        }
   }
    
   class TSelectActionPerformed extends TimerTask {
        @Override
        public void run(){
            CHon = lstChannels.getSelectedIndex();
            CHNxName = CHCurrNext.get(CHon);
            int n =0;
            for (String h : ListChannels) {
                 if (h.equals(CHNxName)) {
                    CHNextTime = CHTimers.get(n);
                 }
                 n += 1;
            }
            lstChannels.setSelectedValue(CHNxName, true);
            String name = lstChannels.getSelectedValue().toString();
            System.out.println("Apply Select: "+name);
            Tools.sleep(50);
            master.selectChannel(CHNxName);
            if (CHNextTime != 0) {
                CHt=new Timer();
                CHt.schedule(new TSelectActionPerformed(),CHNextTime);
                CHNextTime = CHTimers.get(lstChannels.getSelectedIndex());
                StopCHpt=false;
                CHProgressTime.setValue(0);
                CHt.schedule(new UpdateCHtUITask(),0);
            } else {
                CHt.cancel();
                CHt.purge();
                StopCHpt=true;
                lstChannels.setEnabled(true);
                ChDuration.setEnabled(true);
                btnStopAllStream.setEnabled(true);
                btnStopOnlyStream.setEnabled(true);
                btnSelect.setEnabled(true);
                btnRenameCh.setEnabled(true);
                btnUp.setEnabled(true);
                btnDown.setEnabled(true);
                btnRemove.setEnabled(true);
                btnAdd.setEnabled(true);
                inTimer=false;
                CHProgressTime.setValue(0);
                CHProgressTime.setString("0");
                StopCHTimer.setEnabled(inTimer);                
            }
	}        
    }
   
    public static String getSelectedChannel() {
        return (lstChannels.getSelectedValue().toString());
    }
    
    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        savePrefs();
        String name = lstChannels.getSelectedValue().toString();
        System.out.println("Apply Select: "+name);
        tglRemote.setEnabled(true);
        master.selectChannel(name);
        if (CHTimers.get(lstChannels.getSelectedIndex()) != 0) {
            inTimer=true;
            btnRenameCh.setEnabled(false);
            btnClearAllCh.setEnabled(false);
            btnUp.setEnabled(false);
            btnDown.setEnabled(false);
            btnRemove.setEnabled(false);
            btnAdd.setEnabled(false);
            lstChannels.setEnabled(false);
            ChDuration.setEnabled(false);
            StopCHTimer.setEnabled(inTimer);
            btnStopAllStream.setEnabled(false);
            btnSelect.setEnabled(false);
            CHt=new Timer();
            CHt.schedule(new TSelectActionPerformed(),CHTimers.get(lstChannels.getSelectedIndex()));
            CHNextTime = CHTimers.get(lstChannels.getSelectedIndex());
            StopCHpt=false;
            CHt.schedule(new UpdateCHtUITask(),0);
        }
    }//GEN-LAST:event_btnSelectActionPerformed
    
    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        String name = lstChannels.getSelectedValue().toString();
        master.updateChannel(name);
        master.addChTransitions(name);
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Channel "+name+" Updated");
        ResourceMonitor.getInstance().addMessage(label);
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void lstNextChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lstNextChannelActionPerformed
        if (lstChannels.getSelectedIndex() != -1) {
           String nextChannel = lstNextChannel.getSelectedItem().toString();
           int ChIndex = lstChannels.getSelectedIndex();
           CHCurrNext.set(ChIndex, nextChannel);
           } 
    }//GEN-LAST:event_lstNextChannelActionPerformed

    private void ChDurationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ChDurationStateChanged
        CHTimer = ChDuration.getValue().hashCode()* 1000;
        if (lstChannels.getSelectedIndex() != -1) {
            int ChIndex = lstChannels.getSelectedIndex();
            CHTimers.set(ChIndex, CHTimer);
        }
    }//GEN-LAST:event_ChDurationStateChanged
    
    private void RemoteStopCHTimerActionPerformed() {                                            
        CHt.cancel();
        CHt.purge();
        StopCHpt=true;
        lstChannels.setEnabled(true);
        ChDuration.setEnabled(true);
        btnStopAllStream.setEnabled(true);
        btnStopOnlyStream.setEnabled(true);
        btnSelect.setEnabled(true);
        btnRenameCh.setEnabled(true);
        btnClearAllCh.setEnabled(true);
        btnUp.setEnabled(true);
        btnDown.setEnabled(true);
        btnRemove.setEnabled(true);
        btnAdd.setEnabled(true);
        inTimer=false;
        CHProgressTime.setValue(0);
        CHProgressTime.setString("0");
        StopCHTimer.setEnabled(inTimer);
    } 
    
    public void RemoteStopCHTimerOnlyActionPerformed() {                                            
        CHt.cancel();
        CHt.purge();
        StopCHpt=true;
        inTimer=false;
        CHProgressTime.setValue(0);
        CHProgressTime.setString("0");
        StopCHTimer.setEnabled(inTimer);
    }
    
    private void StopCHTimerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StopCHTimerActionPerformed
        RemoteStopCHTimerActionPerformed();
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Channel Timer Stopped.");
        ResourceMonitor.getInstance().addMessage(label);
    }//GEN-LAST:event_StopCHTimerActionPerformed

    private void btnStopAllStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopAllStreamActionPerformed
        SystemPlayer.getInstance(null).stop();
        Tools.sleep(30);
        PrePlayer.getPreInstance(null).stop();
        Tools.sleep(10);
        MasterChannels.getInstance().stopAllStream();
        for (Stream s : streamS){
            s.updateStatus();
        }
        Tools.sleep(30);
        listenerCPOP.resetButtonsStates(evt);
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "All Stopped.");
        ResourceMonitor.getInstance().addMessage(label);
        System.gc();
    }//GEN-LAST:event_btnStopAllStreamActionPerformed
    @SuppressWarnings("unchecked")
    private void btnRenameChActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenameChActionPerformed
        if (lstChannels != null && txtName.getText().length() > 0) {
            String rnName = txtName.getText();
            String chName = lstChannels.getSelectedValue().toString();
            int selectCHIndex = lstChannels.getSelectedIndex();
            for (Stream stream : streamS){
                for (SourceChannel sc : stream.getChannels()) {
                    if (sc.getName().equals(chName)){
                        sc.setName(rnName);
                    }
                }
            }
            int coun =  0;
            for (String chCurNx : CHCurrNext){
                if (chCurNx.equals(chName)){
                    CHCurrNext.set(coun, rnName);
                }
                coun++;
            }
            lstNextChannel.revalidate();
            master.addChannelAt(rnName, selectCHIndex);
            master.removeChannelAt(chName);
            model.removeElement(chName);
            aModel.removeElement(chName);
            CHTimers.remove(selectCHIndex);
            ListChannels.remove(chName);
            lstChannels.revalidate();
            model.insertElementAt(rnName, selectCHIndex);
            aModel.insertElementAt(rnName, selectCHIndex);
            CHTimers.add(selectCHIndex, CHTimer);
            ListChannels.add(selectCHIndex, rnName);
            lstChannels.revalidate();
            lstNextChannel.revalidate();
            btnRenameCh.setEnabled(false);
            btnUp.setEnabled(false);
            btnDown.setEnabled(false);
        }
    }//GEN-LAST:event_btnRenameChActionPerformed
    @SuppressWarnings("unchecked")
    private void btnAutoPlayListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAutoPlayListActionPerformed
        ArrayList<Stream> allStreams = MasterChannels.getInstance().getStreams();
        for (Stream s : allStreams) {
            if (!s.getClass().toString().contains("Sink")) {
                String sourceName = s.getName();
    //            System.out.println("Source: "+sourceName);
                String shortName = "";
                if (sourceName.length() > 30) {
                    shortName = s.getName().substring(0, 30)+" ...";
                } else {
                    shortName = sourceName;
                }
                if (shortName.length() > 0) {
                    s.setIsPlaying(true);
                    master.addChannel(shortName);
                    model.addElement(shortName);
                    aModel.addElement(shortName);
                    CHCurrNext.add(shortName);
                    CHTimers.add(CHTimer);
                    ListChannels.add(shortName);
                    lstChannels.revalidate();
                    lstNextChannel.revalidate();
                    s.setIsPlaying(false);
                }
            }
        }
        int index = 0;
        int lastItemIndex = ListChannels.size()-1;
//        System.out.println("LastItemIndex: " + lastItemIndex);
        for (Stream s : allStreams) {
            if (!s.getClass().toString().contains("Sink")) {
                if (!"N/A".equals(s.getStreamTime())) {
                    String sPrepTime = s.getStreamTime().replaceAll("s", "");
                    int sDuration = Integer.parseInt(sPrepTime);
                    CHTimer = sDuration * 1000;
                    CHTimers.set(index, CHTimer);
                } else {
                    CHTimers.set(index, 0);
                }
                if (index < lastItemIndex) {
                    String nextChannel = ListChannels.get(index+1);
                    CHCurrNext.set(index, nextChannel);
                } else {
                    String nextChannel = ListChannels.get(0);
                    CHCurrNext.set(index, nextChannel);
                }
    //            System.out.println("Name: "+s.getName()+" Duration: "+CHTimer);
                index++;
            }
        }
        btnAutoPlayList.setEnabled(false);
    }//GEN-LAST:event_btnAutoPlayListActionPerformed
    @SuppressWarnings("unchecked")
    private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownActionPerformed
        int selectedCHIndex = lstChannels.getSelectedIndex();
        String selectedChName = ListChannels.get(selectedCHIndex);
        int selectedCHTimer = CHTimers.get(selectedCHIndex);
        int nextCHIndex;
        String nextChName;
        int nextCHTimer;
//        System.out.println("List Next Channels: "+CHCurrNext);
//        System.out.println("List Channels Timers: "+CHTimers);
        if (lstChannels != null && selectedCHIndex < ListChannels.size() - 1) {
            if (selectedCHIndex == ListChannels.size() - 2) {
                nextCHIndex = selectedCHIndex + 1;
                nextChName = ListChannels.get(nextCHIndex);
                nextCHTimer = CHTimers.get(nextCHIndex);
//                System.out.println("Master Channels Before:"+master.getChannels());
                // Update Master Channels
                master.removeChannelAt(selectedChName);
                master.removeChannelAt(nextChName);
                master.addToChannels(nextChName);
                master.addToChannels(selectedChName);
                // Update Streams Channels
                for (Stream stream : streamS){
                    String streamName =stream.getClass().getName();
                    if (!streamName.contains("Sink")){
                        SourceChannel tempSelSC = null;
                        SourceChannel tempNextSC = null;
                        for (SourceChannel sc : stream.getChannels()) {
                            if (sc.getName().equals(selectedChName)){
                                tempSelSC = sc;
                            }
                            if (sc.getName().equals(nextChName)){
                                tempNextSC = sc;
                            }
                        }                   
                        stream.addChannelAt(tempSelSC, nextCHIndex);
                        stream.addChannelAt(tempNextSC, selectedCHIndex);
                    }
                }
//                System.out.println("Master Channels After:"+master.getChannels());
                // Update UI lists and WS lists Channels
                model.removeElement(selectedChName);
                model.removeElement(nextChName);                
                aModel.removeElement(selectedChName);
                aModel.removeElement(nextChName);
                CHCurrNext.remove(selectedChName);
                CHTimers.remove(selectedCHIndex);
                CHTimers.remove(selectedCHIndex);
                ListChannels.remove(selectedChName);
                ListChannels.remove(nextChName);
                lstChannels.revalidate();
                lstNextChannel.revalidate();
//                System.out.println("List Next Channels Remove: "+CHCurrNext);
                model.addElement(nextChName);
                model.addElement(selectedChName);
                aModel.addElement(nextChName);
                aModel.addElement(selectedChName);
                CHCurrNext.add(selectedCHIndex, selectedChName);
                CHTimers.add(nextCHTimer);
                CHTimers.add(selectedCHTimer);
                ListChannels.add(nextChName);
                ListChannels.add(selectedChName);
                lstChannels.revalidate();
                lstNextChannel.revalidate();      
                lstChannels.setSelectedIndex(nextCHIndex);
//                System.out.println("List Next Channels Insert: "+CHCurrNext);
            } else {
                nextCHIndex = selectedCHIndex + 1;
                nextChName = ListChannels.get(nextCHIndex);
                nextCHTimer = CHTimers.get(nextCHIndex);
//                System.out.println("Master Channels Before:"+master.getChannels());
                // Update Master Channels
                master.removeChannelAt(selectedChName);
                master.removeChannelAt(nextChName);
                master.addChannelAt(nextChName, selectedCHIndex);
                master.addChannelAt(selectedChName, nextCHIndex);
                // Update Streams Channels
                for (Stream stream : streamS){
                    String streamName =stream.getClass().getName();
                    if (!streamName.contains("Sink")){
                        SourceChannel tempSelSC = null;
                        SourceChannel tempNextSC = null;
                        for (SourceChannel sc : stream.getChannels()) {
                            if (sc.getName().equals(selectedChName)){
                                tempSelSC = sc;
                            }
                            if (sc.getName().equals(nextChName)){
                                tempNextSC = sc;
                            }
                        }                   
                        stream.addChannelAt(tempSelSC, nextCHIndex);
                        stream.addChannelAt(tempNextSC, selectedCHIndex);
                    }
                }
//                System.out.println("Master Channels After:"+master.getChannels());
                // Update UI Channels lists and WS lists
                model.removeElement(selectedChName);
                model.removeElement(nextChName);                
                aModel.removeElement(selectedChName);
                aModel.removeElement(nextChName);
                if (selectedCHIndex == 0) {
                  CHCurrNext.remove(selectedChName);
                  CHCurrNext.remove(nextChName);
                } else {
                    CHCurrNext.remove(selectedChName);
                }
                CHTimers.remove(selectedCHIndex);
                CHTimers.remove(selectedCHIndex);
//                System.out.println("List Channels Timers Removed: "+CHTimers);
                ListChannels.remove(selectedChName);
                ListChannels.remove(nextChName);
                lstChannels.revalidate();
                lstNextChannel.revalidate();
//                System.out.println("List Next Channels Remove: "+CHCurrNext);
                model.insertElementAt(nextChName, selectedCHIndex);
                model.insertElementAt(selectedChName, nextCHIndex);
                aModel.insertElementAt(nextChName, selectedCHIndex);
                aModel.insertElementAt(selectedChName, nextCHIndex);
                if (selectedCHIndex == 0) {
                    CHCurrNext.add(selectedCHIndex, selectedChName);
                    CHCurrNext.add(nextChName);
                } else {
                    CHCurrNext.add(selectedCHIndex, selectedChName);
                }
                CHTimers.add(selectedCHIndex, nextCHTimer);
                CHTimers.add(nextCHIndex, selectedCHTimer);
//                System.out.println("List Channels Timers After: "+CHTimers);
                ListChannels.add(selectedCHIndex, nextChName);
                ListChannels.add(nextCHIndex, selectedChName);
                lstChannels.revalidate();
                lstNextChannel.revalidate();      
                lstChannels.setSelectedIndex(nextCHIndex);
//                System.out.println("List Next Channels Insert: "+CHCurrNext);
            }
        }
    }//GEN-LAST:event_btnDownActionPerformed
    @SuppressWarnings("unchecked")
    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
        int selectedCHIndex = lstChannels.getSelectedIndex();
        String selectedChName = ListChannels.get(selectedCHIndex);
        int selectedCHTimer = CHTimers.get(selectedCHIndex);
        int previousCHIndex;
        String previousChName;
        int previousCHTimer;
        int previous2CHIndex;
//        System.out.println("List Next Channels: "+CHCurrNext);
//        System.out.println("List Channels Timers: "+CHTimers);
        if (lstChannels != null && selectedCHIndex > 0) {
            if (selectedCHIndex == 1) {
                previousCHIndex = selectedCHIndex - 1;
                previousChName = ListChannels.get(previousCHIndex);
                previousCHTimer = CHTimers.get(previousCHIndex);
//                System.out.println("Master Channels Before:"+master.getChannels());
                // Update Master Channels
                master.removeChannelAt(selectedChName);
                master.removeChannelAt(previousChName);
                master.addChannelAt(selectedChName, previousCHIndex);
                master.addChannelAt(previousChName, selectedCHIndex);
                // Update Streams Channels
                for (Stream stream : streamS){
                    String streamName =stream.getClass().getName();
                    if (!streamName.contains("Sink")){
                        SourceChannel tempSelSC = null;
                        SourceChannel tempPrevSC = null;
                        for (SourceChannel sc : stream.getChannels()) {
                            if (sc.getName().equals(selectedChName)){
                                tempSelSC = sc;
                            }
                            if (sc.getName().equals(previousChName)){
                                tempPrevSC = sc;
                            }
                        }                   
                        stream.addChannelAt(tempSelSC, previousCHIndex);
                        stream.addChannelAt(tempPrevSC, selectedCHIndex);
                    }
                }
//                System.out.println("Master Channels After:"+master.getChannels());
                // Update UI lists and WS lists Channels
                model.removeElement(selectedChName);
                model.removeElement(previousChName);                
                aModel.removeElement(selectedChName);
                aModel.removeElement(previousChName);
                CHCurrNext.remove(selectedChName);
                CHTimers.remove(selectedCHIndex);
                CHTimers.remove(previousCHIndex);
                ListChannels.remove(selectedChName);
                ListChannels.remove(previousChName);
                lstChannels.revalidate();
                lstNextChannel.revalidate();               
//                System.out.println("List Next Channels Remove: "+CHCurrNext);                
                model.insertElementAt(selectedChName, previousCHIndex);
                model.insertElementAt(previousChName, selectedCHIndex);
                aModel.insertElementAt(selectedChName, previousCHIndex);
                aModel.insertElementAt(previousChName, selectedCHIndex);
                CHCurrNext.add(previousCHIndex, previousChName);
                CHTimers.add(previousCHIndex, selectedCHTimer);
                CHTimers.add(selectedCHIndex, previousCHTimer);                
                ListChannels.add(previousCHIndex, selectedChName);
                ListChannels.add(selectedCHIndex, previousChName);                
                lstChannels.revalidate();
                lstNextChannel.revalidate();                     
                CHCurrNext.remove(ListChannels.size()-1);
                lstNextChannel.revalidate();
                CHCurrNext.add(ListChannels.size()-1, selectedChName);
                lstNextChannel.revalidate();               
                lstChannels.setSelectedIndex(previousCHIndex);               
//                System.out.println("List Next Channels Insert: "+CHCurrNext);
            } else {
                previous2CHIndex = selectedCHIndex - 2;
                previousCHIndex = selectedCHIndex - 1;
                previousChName = ListChannels.get(previousCHIndex);
                previousCHTimer = CHTimers.get(previousCHIndex);
//                System.out.println("Master Channels Before:"+master.getChannels());
                // Update Master Channels
                master.removeChannelAt(selectedChName);
                master.removeChannelAt(previousChName);
                master.addChannelAt(selectedChName, previousCHIndex);
                master.addChannelAt(previousChName, selectedCHIndex);
                // Update Streams Channels
                for (Stream stream : streamS){
                    String streamName =stream.getClass().getName();
                    if (!streamName.contains("Sink")){
                        SourceChannel tempSelSC = null;
                        SourceChannel tempPrevSC = null;
                        for (SourceChannel sc : stream.getChannels()) {
                            if (sc.getName().equals(selectedChName)){
                                tempSelSC = sc;
                            }
                            if (sc.getName().equals(previousChName)){
                                tempPrevSC = sc;
                            }
                        }                   
                        stream.addChannelAt(tempSelSC, previousCHIndex);
                        stream.addChannelAt(tempPrevSC, selectedCHIndex);
                    }
                }
//                System.out.println("Master Channels After:"+master.getChannels());
                // Update UI Channels lists and WS lists
                model.removeElement(selectedChName);
                model.removeElement(previousChName);                
                aModel.removeElement(selectedChName);
                aModel.removeElement(previousChName);
                CHCurrNext.remove(selectedChName);
                CHTimers.remove(selectedCHIndex);
                CHTimers.remove(previousCHIndex);
                ListChannels.remove(selectedChName);
                ListChannels.remove(previousChName);
                lstChannels.revalidate();
                lstNextChannel.revalidate();
//                System.out.println("List Next Channels Remove: "+CHCurrNext);
                model.insertElementAt(selectedChName, previousCHIndex);
                model.insertElementAt(previousChName, selectedCHIndex);
                aModel.insertElementAt(selectedChName, previousCHIndex);
                aModel.insertElementAt(previousChName, selectedCHIndex);
                CHCurrNext.add(previousCHIndex, previousChName);
                CHTimers.add(previousCHIndex, selectedCHTimer);
                CHTimers.add(selectedCHIndex, previousCHTimer);                
                ListChannels.add(previousCHIndex, selectedChName);
                ListChannels.add(selectedCHIndex, previousChName);
                lstChannels.revalidate();
                lstNextChannel.revalidate();      
                lstChannels.setSelectedIndex(previousCHIndex);
//                System.out.println("List Next Channels Insert: "+CHCurrNext);
                CHCurrNext.remove(previous2CHIndex);
                lstNextChannel.revalidate();
                CHCurrNext.add(previous2CHIndex, selectedChName);
                lstNextChannel.revalidate();
            }
        }
    }//GEN-LAST:event_btnUpActionPerformed

    private void btnClearAllChActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAllChActionPerformed
        int result = JOptionPane.showConfirmDialog(this,"All Channels will be Deleted !!!","Attention",JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JFileChooser.APPROVE_OPTION) {
            ArrayList<String> sourceChI = MasterChannels.getInstance().getChannels();
            if (sourceChI.size()>0) {
                do {
                    for (int a=0; a< sourceChI.size(); a++) {
                        String removeSc = sourceChI.get(a);
                        MasterChannels.getInstance().removeChannel(removeSc);
                        removeChannels(removeSc, a);
                    }
                } while (sourceChI.size()>0);
                resetBtnStates(evt);
            }
        } else {
            ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Delete All Channels Cancelled!");
            ResourceMonitor.getInstance().addMessage(label);
        }
    }//GEN-LAST:event_btnClearAllChActionPerformed

    private void lstChannelsComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_lstChannelsComponentAdded
        if (lstChannels.getSelectedIndex() != -1) {
            btnClearAllCh.setEnabled(true);
        }
    }//GEN-LAST:event_lstChannelsComponentAdded

    private void tglRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglRemoteActionPerformed
        if (tglRemote.isSelected()) {
            remote.setPort(remPort);
            remote.start();
        } else {
            remote.stop();
        }
    }//GEN-LAST:event_tglRemoteActionPerformed

    private void btnStopOnlyStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopOnlyStreamActionPerformed
        MasterChannels.getInstance().stopOnlyStream();
        for (Stream s : streamS){
                s.updateStatus();
            }
        Tools.sleep(30);
        if (inTimer){
            RemoteStopCHTimerActionPerformed();
        } else {
            RemoteStopCHTimerOnlyActionPerformed();
        }
        ResourceMonitorLabel label = new ResourceMonitorLabel(System.currentTimeMillis()+10000, "Streams Stopped.");
        ResourceMonitor.getInstance().addMessage(label);
        System.gc();
    }//GEN-LAST:event_btnStopOnlyStreamActionPerformed

    private void lstChannelsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstChannelsMouseClicked
        if (evt.getClickCount() == 2 && !evt.isConsumed()) {
            evt.consume();
            if (lstChannels.isEnabled()){
                btnSelect.doClick();
            }
        }
    }//GEN-LAST:event_lstChannelsMouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar CHProgressTime;
    private javax.swing.JSpinner ChDuration;
    private javax.swing.JButton StopCHTimer;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAutoPlayList;
    private javax.swing.JButton btnClearAllCh;
    private javax.swing.JButton btnDown;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRenameCh;
    private javax.swing.JButton btnSelect;
    private javax.swing.JButton btnStopAllStream;
    private javax.swing.JButton btnStopOnlyStream;
    private javax.swing.JButton btnUp;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblChName;
    private static javax.swing.JList lstChannels;
    private javax.swing.JScrollPane lstChannelsScroll;
    private javax.swing.JComboBox lstNextChannel;
    private javax.swing.JToggleButton tglRemote;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
    
}
