package gov.usgs.dismodel.gui.menubar;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.data.LoadCovMenuItem;
import gov.usgs.dismodel.gui.menubar.data.LoadDisplacementMenuItem;
import gov.usgs.dismodel.gui.menubar.data.LoadStationMenuItem;
import gov.usgs.dismodel.gui.menubar.data.ProcessGreensFilesMenuItem;
import gov.usgs.dismodel.gui.menubar.file.SaveProjectFileMenu;
import gov.usgs.dismodel.gui.menubar.inversion.ForwardModelMenuItem;
import gov.usgs.dismodel.gui.menubar.inversion.SmoothingMenuItem;
import gov.usgs.dismodel.gui.menubar.inversion.SolveMenuItem;
import gov.usgs.dismodel.gui.menubar.source.DislocationMenuItem;
import gov.usgs.dismodel.gui.menubar.source.MogiMenuItem;
import gov.usgs.dismodel.gui.menubar.source.SphericalMenuItem;
import gov.usgs.dismodel.gui.menubar.inversion.DistSlipConsMenuItem;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class MainMenu extends JMenuBar {
    // other vars
    private static final long serialVersionUID = -2200844778578234292L;
    AllGUIVars allGuiVars;

    public MainMenu(AllGUIVars allGuiVars) {
        // Main Menu
        super();
        this.allGuiVars = allGuiVars;

        // top level (0) menu items
        // ------------------------
        this.add(new FileMenu("File"));
        this.add(new JMenu("Map"));
        this.add(new DataMenu("Data"));
        this.add(new SourceMenu("Source"));
        this.add(new InversionMenu("Inversion"));
        this.add(new JMenu("Help"));
    }

    // level 1 menu items
    // ---------------------
    // File
    private class FileMenu extends JMenu {
        public FileMenu(String title) {
            super(title);

            this.add(new SaveProjectFileMenu("Save Project...", allGuiVars));
            this.add(new JMenuItem("Open Project..."));
            this.add(new JMenuItem("Exit"));
        }
    }    
    
    // Data
    private class DataMenu extends JMenu {
        public DataMenu(String title) {
            super(title);

            this.add(new Data_GpsMenu("GPS"));
            this.add(new Data_BatchMenu("Batch Process"));
        }

        private static final long serialVersionUID = -7955614673694946018L;
    }

    // Source
    private class SourceMenu extends JMenu {
        public SourceMenu(String title) {
            super(title);

            this.add(new JMenuItem("Edit / view sources..."));
            this.add(new DislocationMenuItem("Add Dislocation...", allGuiVars));
            this.add(new MogiMenuItem("Add Mogi...", allGuiVars));
            this.add(new SphericalMenuItem("Add Spherical...", allGuiVars));
        }

        private static final long serialVersionUID = -1827521201927405646L;
    }

    // Inversion
    private class InversionMenu extends JMenu {
        public InversionMenu(String title) {
            super(title);

            this.add(new SmoothingMenuItem("Smoothing...", allGuiVars));
            this.add(new DistSlipConsMenuItem("Dist Slip Constraints...", allGuiVars));
            this.add(new ForwardModelMenuItem("Forward Model", allGuiVars));
            this.add(new SolveMenuItem("Solve", allGuiVars));
        }

    }

    // Level 2 menu items
    // -------------------
    // Data / GPS
    private class Data_GpsMenu extends JMenu {
        public Data_GpsMenu(String title) {
            super(title);

            this.add(new LoadStationMenuItem("Load Station Locations & Names...", allGuiVars));
            this.add(new LoadDisplacementMenuItem("Load Displacements...", allGuiVars));
            this.add(new LoadCovMenuItem("Load Covariance Matrix...", allGuiVars));
        }

        private static final long serialVersionUID = 4161855026549914886L;
    }

    // Data / Batch Process
    private class Data_BatchMenu extends JMenu {
        public Data_BatchMenu(String title) {
            super(title);

            this.add(new ProcessGreensFilesMenuItem("Green's function file(s)...", allGuiVars));
        }

        private static final long serialVersionUID = -3577880343506143989L;
    }

}
