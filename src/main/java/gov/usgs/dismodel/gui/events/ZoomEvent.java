package gov.usgs.dismodel.gui.events;

import java.util.EventObject;

public class ZoomEvent extends EventObject{
	private double axisSpan;

	public ZoomEvent(Object source, double axisSpan) {
		super(source);
		this.axisSpan = axisSpan;
	}

	public double getAxisSpan() {
		return axisSpan;
	}
	
	
}
