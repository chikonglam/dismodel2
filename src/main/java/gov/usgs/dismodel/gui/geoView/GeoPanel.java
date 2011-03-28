package gov.usgs.dismodel.gui.geoView;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.util.StatusBar;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

 public class GeoPanel extends JPanel{
	 	private JPanel toolbar;
        private WorldWindowGLCanvas wwd;
        private StatusBar statusBar;
        private SimulationDataModel simModel;
        private DisplayStateStore displaySettings;
        
        // Constructs a JPanel to hold the WorldWindow
        public GeoPanel(Dimension canvasSize, boolean includeStatusBar, SimulationDataModel simModel, DisplayStateStore displaySettings)
        {
            super(new BorderLayout());
            //state vars
            this.simModel = simModel;
            this.displaySettings = displaySettings;
            
            // Create the toolbar
            this.toolbar = new JPanel();
            this.add(toolbar, BorderLayout.NORTH);

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
                statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                statusBar.setEventSource(wwd);
            }
            

            
        }
    }




