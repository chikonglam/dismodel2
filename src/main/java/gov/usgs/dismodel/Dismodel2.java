/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.usgs.dismodel;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.LayerPanel;
import gov.nasa.worldwind.util.StatusBar;

import javax.swing.*;
import java.awt.*;


public class Dismodel2
{
    public static class AppPanel extends JPanel
    {
        private WorldWindowGLCanvas wwd;

        // Constructs a JPanel to hold the WorldWindow
        public AppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(new BorderLayout());

            // Create the WorldWindow and set its preferred size.
            this.wwd = new WorldWindowGLCanvas();
            this.wwd.setPreferredSize(canvasSize);

            // THIS IS THE TRICK: Set the panel's minimum size to (0,0);
            this.setMinimumSize(new Dimension(0, 0));

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            //  Check the code below for click event handling
            //this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

            // Add the WorldWindow to this JPanel.
            this.add(this.wwd, BorderLayout.CENTER);

            // Add the status bar if desired.
            if (includeStatusBar)
            {
                StatusBar statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                statusBar.setEventSource(wwd);
            }
        }
    }

    private static class AppFrame extends JFrame
    {
        private Dimension canvasSize = new Dimension(800, 600); // the desired WorldWindow size

        public AppFrame()
        {
            // Create the WorldWindow.
            final AppPanel wwjPanel = new AppPanel(this.canvasSize, true);
            LayerPanel layerPanel = new LayerPanel(wwjPanel.wwd);

            // Create a horizontal split pane containing the layer panel and the WorldWindow panel.
            JSplitPane horizontalSplitPane = new JSplitPane();
            horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            horizontalSplitPane.setLeftComponent(wwjPanel);
            horizontalSplitPane.setRightComponent(layerPanel);
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
        }
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
            final AppFrame frame = new AppFrame();
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
}
