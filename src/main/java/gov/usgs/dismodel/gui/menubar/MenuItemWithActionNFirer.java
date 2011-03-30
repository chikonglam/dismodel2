package gov.usgs.dismodel.gui.menubar;

import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

public abstract class MenuItemWithActionNFirer extends JMenuItem implements DataChangeEventFrier{
	private static final long serialVersionUID = 376779782775180460L;
	ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();
	ActionListener action = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			menuItemClickAction(e);
		}
	};
	
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
	
	public abstract void menuItemClickAction(ActionEvent e);
	
}
