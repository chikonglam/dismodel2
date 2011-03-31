package gov.usgs.dismodel.gui.events;

import java.util.EventListener;

public interface GuiUpdateRequestListener extends EventListener{
	public void guiUpdateAfterStateChange();
}
