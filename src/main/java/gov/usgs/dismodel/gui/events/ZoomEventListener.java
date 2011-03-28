package gov.usgs.dismodel.gui.events;

import gov.usgs.dismodel.state.DisplayStateStore;

import java.util.EventListener;

public interface ZoomEventListener extends EventListener {
	public abstract void updateZoomLevel(DisplayStateStore displaySettings);
	
}
