/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */

package gov.usgs.dismodel;

import gov.nasa.worldwind.Configuration;
import gov.usgs.dismodel.gui.ENUView.ENUPanel;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GeoPosClickFrier;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestFrier;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.gui.events.RecenterEventListener;
import gov.usgs.dismodel.gui.events.RecenterEventRedirector;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.gui.geoView.GeoPanel;
import gov.usgs.dismodel.gui.menubar.MainMenu;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class Dismodel2 extends JFrame implements DataChangeEventListener, DataChangeEventFrier, GuiUpdateRequestFrier,
        GeoPosClickFrier, RecenterEventRedirector, ZoomEventListener {
    static {
        // Ensure that menus and tooltips interact successfully with the WWJ
        // window.
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    private static final long serialVersionUID = -4141752858923619028L;
    // the interface vars
    final private static Dimension wwjSize = new Dimension(512, 768); // the
                                                                      // desired
                                                                      // WorldWindow
                                                                      // size
    final private static Dimension enuSize = new Dimension(512, 768); // the
                                                                      // desired
                                                                      // ENU
                                                                      // Panel
                                                                      // size
    private final GeoPanel wwjPanel;
    private final ENUPanel enuPanel;
    private final MainMenu menubar;

    // the state vars
    private DisplayStateStore displaySettings = new DisplayStateStore();
    private SimulationDataModel simModel = new SimulationDataModel();

    // EventListeners
    private ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();
    private ArrayList<GuiUpdateRequestListener> guiChgListeners = new ArrayList<GuiUpdateRequestListener>();
    private ArrayList<RecenterEventListener> recenterListeners = new ArrayList<RecenterEventListener>();

    public Dismodel2() {
        // set the icon
        setIconImage((new ImageIcon(Dismodel2.class.getResource("/gov/usgs/dismodel/resources/equals.png"))).getImage());

        // Create the WorldWindow.
        wwjPanel = new GeoPanel(wwjSize, true, simModel, displaySettings, this);
        enuPanel = new ENUPanel(enuSize, simModel, displaySettings, this);

        // Create a horizontal split pane containing the layer panel and the
        // WorldWindow panel.
        JSplitPane horizontalSplitPane = new JSplitPane();
        horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitPane.setLeftComponent(wwjPanel);
        horizontalSplitPane.setRightComponent(enuPanel);
        horizontalSplitPane.setOneTouchExpandable(true);
        horizontalSplitPane.setContinuousLayout(true); // prevents the pane's
                                                       // being obscured when
                                                       // expanding right

        // Add the vertical split-pane to the frame.
        this.getContentPane().add(horizontalSplitPane, BorderLayout.CENTER);
        this.pack();

        // Center the application on the screen.
        Dimension prefSize = this.getPreferredSize();
        Dimension parentSize;
        java.awt.Point parentLocation = new java.awt.Point(0, 0);
        parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
        this.setLocation(x, y);
        this.setResizable(true);

        // Setting the Nimbus Look and Feel
        setLAF();

        // set menubar
        this.menubar = new MainMenu(new AllGUIVars(this, wwjPanel, enuPanel, displaySettings, simModel));
        // populateMenuBar();
        this.setJMenuBar(menubar);

        // tying the zoom events of both sides
        wwjPanel.addZoomListener(enuPanel);
        enuPanel.addZoomListener(wwjPanel);

        // register the two panels to listen to this
        this.addDataChangeEventListener(wwjPanel);
        this.addDataChangeEventListener(enuPanel);

        // set up the recenter listener, add the ENU panel too if needed
        this.addRecenterEventListener(wwjPanel);

        updateAfterDataChange(); // fire the event to update the gui and the
                                 // data views
    }

    public static void main(String[] args) {
        start("USGS Dismodel");
    }

    public static void start(String appName) {
        if (Configuration.isMacOS() && appName != null) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try {
            final Dismodel2 frame = new Dismodel2();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    frame.setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLAF() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    @Override
    public void addDataChangeEventListener(DataChangeEventListener listener) {
        this.dataChgListeners.add(listener);
    }

    @Override
    public void removeDataChangeEventListener(DataChangeEventListener listener) {
        this.dataChgListeners.remove(listener);
    }

    @Override
    public void updateAfterDataChange() {
        for (DataChangeEventListener listener : this.dataChgListeners) { // first
                                                                         // pass
                                                                         // events
                                                                         // to
                                                                         // the
                                                                         // two
                                                                         // graphical
                                                                         // interfaces
            listener.updateAfterDataChange();
        }

        for (GuiUpdateRequestListener listener : this.guiChgListeners) { // pass
                                                                         // the
                                                                         // event
                                                                         // to
                                                                         // Gui
                                                                         // Update
                                                                         // Listeners
            listener.guiUpdateAfterStateChange();
        }
    }

    @Override
    public void addGuiUpdateRequestListener(GuiUpdateRequestListener listener) {
        this.guiChgListeners.add(listener);
    }

    @Override
    public void removeGuiUpdateRequestListener(GuiUpdateRequestListener listener) {
        this.guiChgListeners.remove(listener);
    }

    @Override
    public void addGeoPosClickListener(GeoPosClickListener listener) {
        wwjPanel.addGeoPosClickListener(listener);
        enuPanel.addGeoPosClickListener(listener);
    }

    @Override
    public void removeGeoPosClickListener(GeoPosClickListener listener) {
        wwjPanel.removeGeoPosClickListener(listener);
        enuPanel.removeGeoPosClickListener(listener);
    }

    @Override
    public void addRecenterEventListener(RecenterEventListener listener) {
        this.recenterListeners.add(listener);
    }

    @Override
    public void removeRecenterEventListener(RecenterEventListener listener) {
        this.recenterListeners.remove(listener);
    }

    @Override
    public void recenterAfterChange(DisplayStateStore displaySettings) {
        for (RecenterEventListener listener : recenterListeners) {
            listener.recenterAfterChange(displaySettings);
        }

    }

    @Override
    public void updateZoomLevelAfterSettingsChanged(DisplayStateStore displaySettings) {
        wwjPanel.updateZoomLevelAfterSettingsChanged(displaySettings);
        enuPanel.updateZoomLevelAfterSettingsChanged(displaySettings);
    }

}
