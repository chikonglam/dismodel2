package gov.usgs.dismodel.gui.jzy3dOverrides;

import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import gov.usgs.dismodel.gui.ENUView.EnuViewerJzy2;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import net.masagroup.jzy3d.chart.Chart;
import net.masagroup.jzy3d.chart.controllers.mouse.ChartMouseController;
import net.masagroup.jzy3d.maths.BoundingBox3d;

public class JzyMouseListener extends ChartMouseController implements ZoomEventFirer{
	
	//EnuViewerJzy2 enuController;
	DisplayStateStore displaySettings;
	ArrayList<ZoomEventListener> zoomListeners = new ArrayList<ZoomEventListener>();
	
    public JzyMouseListener(DisplayStateStore displaySettings) {
		super();
		this.displaySettings = displaySettings;
	}
    
	protected void zoom(final float factor){
		double oldAxisSpan = displaySettings.getChartSpan();
		double newAxisSpan = factor * oldAxisSpan;
		displaySettings.setChartSpan(newAxisSpan);
		for (ZoomEventListener listener : zoomListeners) {
			listener.updateZoomLevel(displaySettings); 
		}
	}

	@Override
	public void addZoomListener(EventListener listener) {
		zoomListeners.add((ZoomEventListener)listener);
	}

	@Override
	public void removeZoomListener(EventListener listener) {
		zoomListeners.remove((ZoomEventListener)listener);
	}
}