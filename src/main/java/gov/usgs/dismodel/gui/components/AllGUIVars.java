package gov.usgs.dismodel.gui.components;


import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.gui.geoView.GeoPanel;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;
import gov.usgs.dismodel.gui.ENUView.ENUPanel;

public class AllGUIVars {
	private Dismodel2 mainFrame;
	private GeoPanel wwjPanel;
	private ENUPanel enuPanel;
	private DisplayStateStore displaySettings;
	private SimulationDataModel simModel;
	
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

	public Dismodel2 getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(Dismodel2 mainFrame) {
		this.mainFrame = mainFrame;
	}

	public GeoPanel getWwjPanel() {
		return wwjPanel;
	}

	public void setWwjPanel(GeoPanel wwjPanel) {
		this.wwjPanel = wwjPanel;
	}

	public ENUPanel getEnuPanel() {
		return enuPanel;
	}

	public void setEnuPanel(ENUPanel enuPanel) {
		this.enuPanel = enuPanel;
	}

	public DisplayStateStore getDisplaySettings() {
		return displaySettings;
	}

	public void setDisplaySettings(DisplayStateStore displaySettings) {
		this.displaySettings = displaySettings;
	}

	public SimulationDataModel getSimModel() {
		return simModel;
	}

	public void setSimModel(SimulationDataModel simModel) {
		this.simModel = simModel;
	}
	
	
	
	
}
