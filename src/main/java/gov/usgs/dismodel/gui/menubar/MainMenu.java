package gov.usgs.dismodel.gui.menubar;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.gui.components.AllGUIVars;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class MainMenu extends JMenuBar {
//	//Menu Actions (must be first, or else menu won't work!)
//	//------------
//	final private ActionListener loadStationAction = new ActionListener(){
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			List<Label> stations = SaveAndLoad.loadStationsFile(allGuiVars.mainFrame);
//			if (stations != null && stations.size() > 0){
//				LLH centerOfStations = Label.centroidLLH(stations);
//				
//				//center map at the center of the stations
//				allGuiVars.displaySettings.setCenterOfMap(  new LatLon( 
//						Angle.fromDegrees(centerOfStations.getLatitude().toDeg()), 
//						Angle.fromDegrees(centerOfStations.getLongitude().toDeg()) ) );
//				//TODO fire display state change event
//				
//				//defaults the origin at the center of the stations
//				allGuiVars.simModel.setOrigin(centerOfStations);
//				//TODO fire data state change event
//				
//				//TODO fire gui state change event
//			}
//			
//		}
//		
//	};
	
	
  //top level (0) menu items
  //------------------------
	final JMenu fileMenu = new JMenu("File");
	final JMenu mapMenu = new JMenu("Map");
	final DataMenu dataMenu = new DataMenu("Data");
	final JMenu sourceMenu = new JMenu("Source");
	final JMenu inversionMenu = new JMenu("Inversion");
	final JMenu helpMenu = new JMenu("Help");

	//other vars
	private static final long serialVersionUID = -2200844778578234292L;
    AllGUIVars allGuiVars;

    
	public MainMenu(AllGUIVars allGuiVars) {
		//Main Menu
		super();
		this.allGuiVars = allGuiVars;
		
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
		final Data_GpsMenu gps = new Data_GpsMenu("GPS");

		//other vars
		private static final long serialVersionUID = -7955614673694946018L;

		
		public DataMenu(String title) {
			super(title);
			this.add(gps);
		}
	}
	
	
	//Level 2 menu items
	//-------------------
	// Data / GPS
	private class Data_GpsMenu extends JMenu{
		//final MenuItemWithActionNFirer loadStation = new MenuItemWithActionNFirer("Load Station Locations & Names...", loadStationAction);
		final JMenuItem loadDisp = new JMenuItem("Load Displacements...");

		//other vars
		private static final long serialVersionUID = 4161855026549914886L;

		public Data_GpsMenu(String title) {
			super(title);
			//this.add(loadStation);
			loadDisp.addActionListener(loadDispAction);
			this.add(loadDisp);

		}

		
		
	}
	

	
	final private ActionListener loadDispAction = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {

		}
		
	};
	
	
	

}


