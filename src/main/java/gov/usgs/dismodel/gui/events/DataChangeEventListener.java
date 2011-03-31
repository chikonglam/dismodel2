package gov.usgs.dismodel.gui.events;

import java.util.EventListener;

public interface DataChangeEventListener extends EventListener {
	public void updateAfterDataChange();
}
