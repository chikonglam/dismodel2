package gov.usgs.dismodel.gui.events;

import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;

import java.util.EventListener;

public interface GeoPosClickListener extends EventListener{
	public void latLonClicked(LLH location);
	public void LocalENUClicked(LocalENU location);
}
