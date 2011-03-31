package gov.usgs.dismodel.gui.menubar;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

public abstract class DataChangingMenuItem extends ActionMenuItem implements DataChangeEventFrier{
	private static final long serialVersionUID = 376779782775180460L;
	protected ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();
	
	public DataChangingMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
	}

	@Override
	public void addDataChangeEventListener(DataChangeEventListener listener) {
		dataChgListeners.add(listener);
	}

	@Override
	public void removeDataChangeEventListener(DataChangeEventListener listener) {
		dataChgListeners.remove(listener);
	}
	
	public void fireDataChangeEvent(){
		for(DataChangeEventListener listener : dataChgListeners){
			listener.updateAfterDataChange();
		}
	}
	
	
	
	
}
