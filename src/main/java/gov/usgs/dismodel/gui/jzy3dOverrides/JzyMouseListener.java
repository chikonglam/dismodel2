package gov.usgs.dismodel.gui.jzy3dOverrides;

import java.util.ArrayList;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import net.masagroup.jzy3d.chart.controllers.mouse.ChartMouseController;

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
			listener.updateZoomLevelAfterSettingsChanged(displaySettings); 
		}
	}

	@Override
	public void addZoomListener(ZoomEventListener listener) {
		zoomListeners.add(listener);
	}

	@Override
	public void removeZoomListener(ZoomEventListener listener) {
		zoomListeners.remove(listener);
	}
}