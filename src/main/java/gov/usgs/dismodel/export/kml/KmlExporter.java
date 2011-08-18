package gov.usgs.dismodel.export.kml;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;

public class KmlExporter {
    private static final String EXPORT_NAME = "Dismodel KML Export";
    
    public static void toKMLFile(File outputFile, SimulationDataModel simModel, DisplayStateStore displaySettings) throws FileNotFoundException{
	final Kml kmlOut = new Kml();
	final Document kmlDoc = kmlOut.createAndSetDocument().withName(EXPORT_NAME);
	ArrayList<DisplacementSolver> allModels = simModel.getFittedModels();
	for (DisplacementSolver model : allModels){
	    Feature modelEle = model.toKMLElement(simModel, displaySettings);
	    if (modelEle != null) {
		kmlDoc.addToFeature(modelEle);
	    }
	}
    
	kmlOut.marshal(outputFile);
    }
}
