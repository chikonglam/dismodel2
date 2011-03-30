package gov.usgs.dismodel.gui.events;

import gov.usgs.dismodel.SimulationDataModel;

import java.util.EventListener;

public interface DataChangeEventListener extends EventListener {
	public void updateAfterDataChange(SimulationDataModel simModel);
}
