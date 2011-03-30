package gov.usgs.dismodel.gui.components;

import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

public class MenuItemWithActionNFirer extends JMenuItem implements DataChangeEventFrier{
	private static final long serialVersionUID = 376779782775180460L;
	ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();
	
	public MenuItemWithActionNFirer(String title, ActionListener action) {
		super(title);
		addActionListener(action);
	}

	@Override
	public void addDataChangeEventListener(DataChangeEventListener listener) {
		dataChgListeners.add(listener);
		
	}

	@Override
	public void removeDataChangeEventListener(DataChangeEventListener listener) {
		dataChgListeners.remove(listener);
		
	}
	
}
