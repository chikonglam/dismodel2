package gov.usgs.dismodel.gui.geoView;

import java.util.ArrayList;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.geom.LLH;

import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.gui.events.RecenterEventFirer;
import gov.usgs.dismodel.gui.events.RecenterEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;

public class OriginSetter extends OriginSetterBase implements DataChangeEventFrier, GeoPosClickListener, RecenterEventFirer{
	//TODO implement center on orgin
	
	private static final long serialVersionUID = -4902931096521845266L;
	final private AllGUIVars allGuiVars;
	final private SimulationDataModel simModel;
	final private DisplayStateStore displaySettings;
	final private Dismodel2 mainFrame;
	
	ArrayList<DataChangeEventListener> changeListeners = new ArrayList<DataChangeEventListener>();  
	ArrayList<RecenterEventListener> recenterListeners = new ArrayList<RecenterEventListener>();
	
	public OriginSetter(AllGUIVars allGuiVars) {
		super(allGuiVars.getMainFrame());
		this.allGuiVars = allGuiVars;
		this.simModel = allGuiVars.getSimModel();
		this.mainFrame = allGuiVars.getMainFrame();
		this.addDataChangeEventListener(mainFrame);
		this.displaySettings = allGuiVars.getDisplaySettings();
		mainFrame.addGeoPosClickListener(this);
		this.addRecenterEventListener(mainFrame);
		
		latLonClicked(simModel.getOrigin());
	}
	
	@Override
	public void dispose(){
		mainFrame.removeGeoPosClickListener(this);
		
		super.dispose();
	}
	
	@Override
    protected void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

	@Override
    protected void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
		double lat = Double.parseDouble( txtLat.getText() );
		double lon = Double.parseDouble( txtLong.getText() );
		double height = Double.parseDouble( txtHeight.getText() );
		LLH newOrigin = new LLH(lat, lon, height);
		simModel.setOrigin(newOrigin);
		
		fireDataChangeEvent();
		
		if (chkCenterAtOrigin.isSelected()){
			displaySettings.setCenterOfMap(
					new LatLon( Angle.fromDegrees(newOrigin.getLatitude().toDeg()),
							Angle.fromDegrees(newOrigin.getLongitude().toDeg()))) ;
			fireRecenterEvent();
		}
		
		dispose();
    }
	
	private void fireRecenterEvent() {
		for (RecenterEventListener listener : recenterListeners) {
			listener.recenterAfterChange(displaySettings);
		}
		
	}

	private void fireDataChangeEvent(){
		for(DataChangeEventListener listener : changeListeners){
			listener.updateAfterDataChange();
		}
	}
	


	@Override
	public void latLonClicked(LLH location) {
		txtLat.setText( String.format("%.6f",  location.getLatitude().toDeg()) );
		txtLong.setText( String.format("%.6f",  location.getLongitude().toDeg()) );
		txtHeight.setText( String.format("%.3f",  location.getHeight()) );
	}

	@Override
	public void LocalENUClicked(LocalENU location) {
	}
	
	

	@Override
	public void addDataChangeEventListener(DataChangeEventListener listener) {
		changeListeners.add(listener);
		
	}

	@Override
	public void removeDataChangeEventListener(DataChangeEventListener listener) {
		changeListeners.remove(listener);
	}

	@Override
	public void addRecenterEventListener(RecenterEventListener listener) {
		this.recenterListeners.add(listener);
	}

	@Override
	public void removeRecenterEventListener(RecenterEventListener listener) {
		this.recenterListeners.remove(listener);
	}

}
