package gov.usgs.dismodel.gui.components;


import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.gui.geoView.GeoPanel;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;
import gov.usgs.dismodel.gui.ENUView.ENUPanel;

public class AllGUIVars {
	public Dismodel2 mainFrame;
	public GeoPanel wwjPanel;
	public ENUPanel enuPanel;
	public DisplayStateStore displaySettings;
	public SimulationDataModel simModel;
	
	public AllGUIVars(Dismodel2 mainFrame, GeoPanel wwjPanel,
			ENUPanel enuPanel, DisplayStateStore displaySettings,
			SimulationDataModel simModel) {
		super();
		this.mainFrame = mainFrame;
		this.wwjPanel = wwjPanel;
		this.enuPanel = enuPanel;
		this.displaySettings = displaySettings;
		this.simModel = simModel;
	}
	
	
}
