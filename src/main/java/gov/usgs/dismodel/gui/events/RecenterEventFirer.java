package gov.usgs.dismodel.gui.events;

public interface RecenterEventFirer {
	public void addRecenterEventListener(RecenterEventListener listener);
	public void removeRecenterEventListener(RecenterEventListener listener);
}
