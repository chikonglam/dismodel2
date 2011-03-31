package gov.usgs.dismodel.gui.menubar;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

public abstract class DataChangingMenuItem extends JMenuItem implements DataChangeEventFrier{
	private static final long serialVersionUID = 376779782775180460L;
	ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();
	protected AllGUIVars allGuiVars;
	ActionListener action = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			menuItemClickAction(e);
		}
	};
	
	public DataChangingMenuItem(String title, AllGUIVars allGuiVars) {
		super(title);
		this.allGuiVars = allGuiVars;
		this.addActionListener(action);
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
	
	
	public abstract void menuItemClickAction(ActionEvent e);
	
}
