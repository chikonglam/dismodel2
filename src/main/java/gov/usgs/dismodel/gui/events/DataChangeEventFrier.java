package gov.usgs.dismodel.gui.events;

public interface DataChangeEventFrier {
	public void addDataChangeEventListener(DataChangeEventListener listener);
	public void removeDataChangeEventListener(DataChangeEventListener listener);
}
