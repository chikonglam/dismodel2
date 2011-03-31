package gov.usgs.dismodel.gui.events;

public interface GuiUpdateRequestFrier {
	public void addGuiUpdateRequestListener(GuiUpdateRequestListener listener);
	public void removeGuiUpdateRequestListener(GuiUpdateRequestListener listener);
}
