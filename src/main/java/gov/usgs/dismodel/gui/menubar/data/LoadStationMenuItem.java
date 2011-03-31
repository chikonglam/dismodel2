package gov.usgs.dismodel.gui.menubar.data;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class LoadStationMenuItem extends DataChangingMenuItem {

	private static final long serialVersionUID = 8545485440232929739L;

	public LoadStationMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
		this.addDataChangeEventListener(allGuiVars.getMainFrame());
	}
	@Override
	public void menuItemClickAction(ActionEvent e) {
		List<Label> stations = SaveAndLoad.loadStationsFile(allGuiVars.getMainFrame());
		if (stations != null && stations.size() > 0){
			LLH centerOfStations = Label.centroidLLH(stations);
			
			//center map at the center of the stations
			allGuiVars.getDisplaySettings().setCenterOfMap(  new LatLon( 
					Angle.fromDegrees(centerOfStations.getLatitude().toDeg()), 
					Angle.fromDegrees(centerOfStations.getLongitude().toDeg()) ) );
			
			//defaults the origin at the center of the stations
			allGuiVars.getSimModel().setOrigin(centerOfStations);
			
			allGuiVars.getSimModel().setStations((ArrayList<Label>)stations);

			fireDataChangeEvent();
			
		}
	}
	

}
