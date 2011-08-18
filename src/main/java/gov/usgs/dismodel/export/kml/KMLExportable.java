package gov.usgs.dismodel.export.kml;

import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;
import de.micromata.opengis.kml.v_2_2_0.Feature;

public interface KMLExportable {
     public Feature toKMLElement(SimulationDataModel simModel, DisplayStateStore displaySettings);
}
