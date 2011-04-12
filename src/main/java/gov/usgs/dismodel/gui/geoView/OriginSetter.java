package gov.usgs.dismodel.gui.geoView;

import java.util.ArrayList;

import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;

public class OriginSetter extends OriginSetterBase implements DataChangeEventFrier, GeoPosClickListener{
	//TODO implement center on orgin
	
	private static final long serialVersionUID = -4902931096521845266L;
	final private AllGUIVars allGuiVars;
	final private SimulationDataModel simModel;
	final private Dismodel2 mainFrame;
	
	ArrayList<DataChangeEventListener> changeListeners = new ArrayList<DataChangeEventListener>();  
	
	public OriginSetter(AllGUIVars allGuiVars) {
		super(allGuiVars.getMainFrame());
		this.allGuiVars = allGuiVars;
		this.simModel = allGuiVars.getSimModel();
		this.mainFrame = allGuiVars.getMainFrame();
		this.addDataChangeEventListener(mainFrame);
		mainFrame.addGeoPosClickListener(this);
		
		latLonClicked(simModel.getOrigin());
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
		dispose();
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

}
