package gov.usgs.dismodel.gui.events;

import java.util.EventListener;



public interface ZoomEventFirer{
	public void addZoomListener(EventListener listener);
	public void removeZoomListener(EventListener listener);
	
	

}
