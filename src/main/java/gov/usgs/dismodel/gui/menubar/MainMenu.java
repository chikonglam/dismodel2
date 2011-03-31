package gov.usgs.dismodel.gui.menubar;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.data.LoadDisplacementMenuItem;
import gov.usgs.dismodel.gui.menubar.data.LoadStationMenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class MainMenu extends JMenuBar {


	//other vars
	private static final long serialVersionUID = -2200844778578234292L;
    AllGUIVars allGuiVars;

    
	public MainMenu(AllGUIVars allGuiVars) {
		//Main Menu
		super();
		this.allGuiVars = allGuiVars;
		
	  //top level (0) menu items
	  //------------------------
		final JMenu fileMenu = new JMenu("File");
		final JMenu mapMenu = new JMenu("Map");
		final DataMenu dataMenu = new DataMenu("Data");
		final JMenu sourceMenu = new JMenu("Source");
		final JMenu inversionMenu = new JMenu("Inversion");
		final JMenu helpMenu = new JMenu("Help");
		

		//plumbing
		//---------
		this.setName("menuBar");
        fileMenu.setName("fileMenu");
        this.add(fileMenu);
        mapMenu.setName("mapMenu");
        this.add(mapMenu);
        dataMenu.setName("dataMenu");
        this.add(dataMenu);
        sourceMenu.setName("sourceMenu");
        this.add(sourceMenu);
        inversionMenu.setName("inversionMenu");
        this.add(inversionMenu);
        helpMenu.setName("helpMenu");
        this.add(helpMenu);
        
	}

	//level 1 menu items
	//---------------------
	// Data
	private class DataMenu extends JMenu{
		private static final long serialVersionUID = -7955614673694946018L;
		
		public DataMenu(String title) {
			super(title);
			final Data_GpsMenu gps = new Data_GpsMenu("GPS");
			
			//plumbing
			//--------
			this.add(gps);
		}
	}
	
	
	//Level 2 menu items
	//-------------------
	// Data / GPS
	private class Data_GpsMenu extends JMenu{
		private static final long serialVersionUID = 4161855026549914886L;

		public Data_GpsMenu(String title) {
			super(title);
			final LoadStationMenuItem loadStationsMenu = new LoadStationMenuItem		("Load Station Locations & Names...", allGuiVars);
			final LoadDisplacementMenuItem loadDispMenu = new LoadDisplacementMenuItem		("Load Displacements...", allGuiVars);

			//plumbing
			this.add(loadStationsMenu);
			this.add(loadDispMenu);
		}

		
		
	}
	

	
	
	
	

}


