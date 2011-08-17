package gov.usgs.dismodel.gui.menubar.view;

import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.gui.VectorScaleAndColorChooser;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LoadKMLMenuItem extends DataChangingMenuItem {

    public LoadKMLMenuItem(String title, AllGUIVars allGuiVars) {
	super(title, allGuiVars);
	this.addDataChangeEventListener(allGuiVars.getMainFrame());
    }

    private static byte[] readFileAsString(File filePath) throws java.io.IOException {
	byte[] buffer = new byte[(int) filePath.length()];
	BufferedInputStream f = null;
	try {
	    f = new BufferedInputStream(new FileInputStream(filePath));
	    f.read(buffer);
	} finally {
	    if (f != null)
		try {
		    f.close();
		} catch (IOException ignored) {
		}
	}
	return buffer;
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
	JFrame frame = allGuiVars.getMainFrame();
	final DisplayStateStore displaySettings = allGuiVars.getDisplaySettings();

	javax.swing.filechooser.FileFilter filter = new FileNameExtensionFilter("KML/KMZ File", "kml", "kmz");
	File file = SaveAndLoad.chooseFile(frame, filter);
	if (file != null) {
	    System.out.println("Reading in KML file " + file);
	    try {
	        byte[] KMLString = readFileAsString(file);
	        displaySettings.setLoadedKML(KMLString);
	        UUID loadedKMLUUID = UUID.randomUUID();
	        displaySettings.setLoadedKMLUUID(loadedKMLUUID);
	        fireDataChangeEvent();
            } catch (IOException e1) {
	        System.err.println("KML Read failed!");
	        e1.printStackTrace();
            }
	}


    }
}
