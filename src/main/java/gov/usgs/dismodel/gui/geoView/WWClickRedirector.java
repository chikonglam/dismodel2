package gov.usgs.dismodel.gui.geoView;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.events.GeoPosClickFrier;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class WWClickRedirector implements MouseListener, GeoPosClickFrier{
	final WorldWindow worldWindCanvus;
	final SimulationDataModel simMoldel;
	ArrayList<GeoPosClickListener> geoPosListeners = new ArrayList<GeoPosClickListener>();

	
	public WWClickRedirector(WorldWindow worldWindCanvus, SimulationDataModel simMoldel) {
		super();
		this.worldWindCanvus = worldWindCanvus;
		this.simMoldel = simMoldel;
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		if (evt.getButton() != MouseEvent.BUTTON1  || evt.getClickCount() != 1){	//single click
			return;
		}
		
		Position mouseLatLon = worldWindCanvus.getCurrentPosition();
		if (mouseLatLon != null){
			LLH origin = simMoldel.getOrigin();
			
			LLH clickedPosition = new LLH(mouseLatLon.getLatitude().getDegrees(), mouseLatLon.getLongitude().getDegrees(), mouseLatLon.getElevation() );
			System.out.println("[mouse clicked: x:" + evt.getX() + " y:" + evt.getY() + " ] = " + clickedPosition );
			LocalENU clickENU = new LocalENU(clickedPosition, origin);
			
			for (GeoPosClickListener listener : geoPosListeners){		//refire the events to listeners
				listener.latLonClicked(clickedPosition);
				listener.LocalENUClicked(clickENU);
			}
		}
		evt.consume();
	}
	
	@Override
	public void addGeoPosClickListener(GeoPosClickListener listener) {
		this.geoPosListeners.add(listener);
	}

	@Override
	public void removeGeoPosClickListener(GeoPosClickListener listener) {
		this.geoPosListeners.remove(listener);
	}


	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}


}
