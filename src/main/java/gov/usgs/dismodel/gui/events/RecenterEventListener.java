package gov.usgs.dismodel.gui.events;

import gov.usgs.dismodel.state.DisplayStateStore;

import java.util.EventListener;

public interface RecenterEventListener extends EventListener {
	public void recenterAfterChange(DisplayStateStore displaySettings);
}
