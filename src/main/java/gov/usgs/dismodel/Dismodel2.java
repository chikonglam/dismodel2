/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.usgs.dismodel;

import gov.nasa.worldwind.*;
import gov.usgs.dismodel.gui.ENUView.ENUPanel;
import gov.usgs.dismodel.gui.geoView.GeoPanel;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.*;


public class Dismodel2 extends JFrame
    {
		//the interface vars
        final private static Dimension wwjSize = new Dimension(512, 768); // the desired WorldWindow size
        final private static Dimension enuSize = new Dimension(512, 768); // the desired ENU Panel size
        private final GeoPanel wwjPanel;
        private final ENUPanel enuPanel;
        
        //the state vars
        private DisplayStateStore displaySettings = new DisplayStateStore();
        private SimulationDataModel simModel = new SimulationDataModel();
        
        
        public Dismodel2()
        {
            // Create the WorldWindow.
            wwjPanel = new GeoPanel(wwjSize, true, simModel, displaySettings);
            enuPanel = new ENUPanel(enuSize, simModel, displaySettings);

            // Create a horizontal split pane containing the layer panel and the WorldWindow panel.
            JSplitPane horizontalSplitPane = new JSplitPane();
            horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            horizontalSplitPane.setLeftComponent(wwjPanel);
            horizontalSplitPane.setRightComponent(enuPanel);
            horizontalSplitPane.setOneTouchExpandable(true);
            horizontalSplitPane.setContinuousLayout(true); // prevents the pane's being obscured when expanding right

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
            
            //Setting the Nimbus Look and Feel 
            setLAF();
            
            //tying the zoom events of both sides
            wwjPanel.addZoomListener(enuPanel);
            enuPanel.addZoomListener(wwjPanel);
        }
    

    public static void main(String[] args)
    {
        start("USGS Dismodel");
    }

    public static void start(String appName)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final Dismodel2 frame = new Dismodel2();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void setLAF() {
        try { 
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}
