package gov.usgs.dismodel.gui.menubar.file;

import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.RecenterEventFirer;
import gov.usgs.dismodel.gui.events.RecenterEventListener;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SavedState;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LoadProjectMenuItem extends DataChangingMenuItem implements RecenterEventFirer, ZoomEventFirer{
	ArrayList<RecenterEventListener> recenterListeners = new ArrayList<RecenterEventListener>();
	ArrayList<ZoomEventListener> zoomListeners = new ArrayList<ZoomEventListener>();

	public LoadProjectMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
		this.addDataChangeEventListener(allGuiVars.getMainFrame());
		this.addRecenterEventListener(allGuiVars.getMainFrame());
		this.addZoomListener(allGuiVars.getMainFrame());
	}

	@Override
	public void menuItemClickAction(ActionEvent e) {
		JFrame frame = allGuiVars.getMainFrame();
        try {
            SavedState newState = SaveAndLoad.loadProject(frame);
            if (newState != null){
	            SimulationDataModel oldSimModel = allGuiVars.getSimModel();
	            DisplayStateStore oldDisplaySettings = allGuiVars.getDisplaySettings();
	            oldSimModel.replaceWith(newState.getSimModel());
	            oldDisplaySettings.replaceWith(newState.getDisplaySettings());
	            
	            fireRecenterEvent();
	            fireDataChangeEvent();
	            fireZoomEvent();
            }
            
        } catch (Exception exp) {
        	exp.printStackTrace();
            JOptionPane.showMessageDialog(frame, exp.getMessage(),
                    "Load error", JOptionPane.ERROR_MESSAGE);
        }
		
	}
	
	private void fireZoomEvent() {
		DisplayStateStore displaySettings = allGuiVars.getDisplaySettings();
		for (ZoomEventListener listener : zoomListeners) {
			listener.updateZoomLevelAfterSettingsChanged(displaySettings);
		}
	}

	private void fireRecenterEvent() {
		DisplayStateStore displaySettings = allGuiVars.getDisplaySettings();
		for (RecenterEventListener listener : recenterListeners) {
			listener.recenterAfterChange(displaySettings);
		}
	}

	@Override
	public void addRecenterEventListener(RecenterEventListener listener) {
		recenterListeners.add(listener);
		
	}

	@Override
	public void removeRecenterEventListener(RecenterEventListener listener) {
		recenterListeners.remove(listener);
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
