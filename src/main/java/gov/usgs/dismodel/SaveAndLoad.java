package gov.usgs.dismodel;

import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.sourcemodels.Quake;
import gov.usgs.dismodel.state.SavedState;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;

public class SaveAndLoad {
    
    /** The rather involved pattern used to match CSV's consists of three
     * alternations: the first matches a quoted field, the second unquoted,
     * the third a null field. 4th any space
     */
    public static final String CSV_PATTERN = "\"([^\"]+?)\",?|([^,]+),?|,";
    public static final String SPACES_PATTERN = "([\\S]+)(\\s+|$)"; // (\S+)[\s+|$]
    
    public static String lastDirectory = System.getProperty("user.home");
    
    // .m MATLAB files
    public static final String END_ASSIGNMENT_STRING   = " ];";
    public static final String M_FILE_EXTENSION      = ".m";
    
    
    public static void saveProject(JFrame owner, SavedState state) throws IOException, JAXBException {
        javax.swing.filechooser.FileFilter filter = new FileNameExtensionFilter("Save Project File", "XML");
        File saveFile = chooseSaveFile(owner, filter);
        if (saveFile != null){
            SavedState.writeToXML(state, saveFile);
        }

    }
    
    public static SavedState loadProject(JFrame owner) throws IOException, JAXBException {
        javax.swing.filechooser.FileFilter filter = new FileNameExtensionFilter("Load Project File", "XML");
        File loadFile = chooseFile(owner, filter);
        if (loadFile != null){
        	SavedState state = SavedState.readXML(loadFile);
        	return state;
        } else {
        	return null;
        }
    }
    
    
    
//    public static void loadProject(File fromFile, WWPanel ww) throws IOException {
//        FileInputStream fin = new FileInputStream(fromFile);
//        ObjectInputStream is = new ObjectInputStream(fin);
//        
//        try {
//            LLH llh = (LLH) is.readObject();
//            double zoom = is.readDouble();
//            double heading = is.readDouble();
//            
//            is.close();
//            
//            System.out.println("Setting WW window to:" + llh);
//            
//            gov.nasa.worldwind.geom.Position center = WWPanel.toPosition(llh);
//            
//            // from applications.SAR.AnalysisPanel.updateView()
//            BasicOrbitView view = (BasicOrbitView) ww.wwd.getView();
//            view.setFieldOfView(Angle.fromDegrees(45));
//            view.setCenterPosition(center);
//            view.setZoom(zoom);
//            view.setHeading(Angle.fromDegrees(heading));
//            //view.setPitch(initialPitch);
//             
//            ww.wwd.redraw();
//        } catch (ClassNotFoundException e) {
//            throw new IOException(e);
//        }
//    }
    
    public static  List<XyzDisplacement> loadOneColDisp(JFrame owner) {
        File file = loadFile(owner);
        if (file != null) {
            LineNumberReader reader = null;
            try {
                reader = new LineNumberReader(new FileReader(file));
                System.out.println("Parsing the file " + file.getName());
                List<XyzDisplacement> vectors = new ArrayList<XyzDisplacement>();
                String firstLine = reader.readLine();
                while (  firstLine != null ){
                    String xS = firstLine;
                    /* Skip comment header, the first line of some test-files. */
                    if (xS.indexOf("#") != -1) {
                        firstLine = reader.readLine();
                    	continue;
                    }
                    String yS = reader.readLine();
                    String zS = reader.readLine();
                    double x = Double.parseDouble(xS);
                    double y = Double.parseDouble(yS);
                    double z = Double.parseDouble(zS);
                    
                    XyzDisplacement xyz = new XyzDisplacement(x, y, z);
                    vectors.add(xyz);
                    System.out.println("A new displacement added: "  + xyz);
                    firstLine = reader.readLine();
                }
                                
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
                return vectors;
                
            } catch (IOException e) {
                String msg = e.getMessage();
                if (reader != null) {
                    msg = "Error reading on line number " + reader.getLineNumber() + " \n"+e.getMessage(); 
                }
                JOptionPane.showMessageDialog(owner, msg, "Load error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
    
    public static File loadFile(JFrame owner) {
        javax.swing.filechooser.FileFilter filter = 
            new FileNameExtensionFilter("Load Text File", "txt");
        File file = chooseFile(owner, filter);
        if (file != null) {
            return file;
        }
        return null;
    }
    
 
/* commented now to make the code compile, will update in a bit
    
//    
//    public static void loadKMLFile(JFrame owner, MapView enu, WWPanel wwjPanel) {
//        javax.swing.filechooser.FileFilter filter = 
//            new FileNameExtensionFilter("KML/KMZ File", "kml", "kmz");
//        File file = chooseFile(owner, filter);
//        if (file != null) {
//            // Construct a KMLRoot and call its parse method.
//            System.out.println("Parsing file " + file);
//            KMLRoot root;
//            try {
//                root = new KMLRoot(file);
//                root.parse();
//                System.out.println("Parsing complete, adding to WW layer");
//                try {
//                    KMLController kmlController = new KMLController(root);
//                    final RenderableLayer layer = new RenderableLayer();
//                    layer.addRenderable(kmlController);
//                    layer.setName("KML Layer");
//                    WWPanel.insertBeforePlacenames(wwjPanel.wwd, layer);
//                } catch (Exception e) {
//                    System.err.print(e);
//                }
//                
//                System.out.println("Adding KML to Cartesian map");
//                
//                KMLDocument doc = null;
//                AVList fieldsAV = root.getFields();
//                if (fieldsAV == null) {
//                    throw new IOException("Document unreadable. Is the document KML 2.2?");
//                }
//                Collection<Object> fields = fieldsAV.getValues();
//                for (Object object : fields) {
//                    if (object instanceof KMLDocument) {
//                        doc = (KMLDocument) object;
//                    }
//                }
//                // Obtain its Feature element if it has one.
//                KMLAbstractFeature feature = root.getFeature();
//                
//                if (doc == null && feature == null) {
//                    throw new IOException("Unable to parse file");
//                }
//                if (doc != null) {
//                	
//                    List<KMLAbstractFeature> features = doc.getFeatures();
//                    for (KMLAbstractFeature kmlAbstractFeature : features) {
//                        parseFeature(kmlAbstractFeature, enu);
//                    }
//                } else {
//                    parseFeature(feature, enu);
//                }
//            } catch (Exception e) {
//                JOptionPane.showMessageDialog(owner, 
//                        e.getMessage(),
//                        "Load error", JOptionPane.ERROR_MESSAGE);
//                e.printStackTrace();
//            }
//        }
//    }
//    
//    public static void parseFeature(KMLAbstractFeature feature, MapView enu) {
//        String name = feature.getName();
//        System.out.println("Processing feature " + name);
//        KMLStyle style = null; // feature.getStyleUrlResolved();  //line color never worked in JZY, changed to null to be compatible with new WorldWind  
//        
//        
//        if (feature instanceof KMLPlacemark) {
//            KMLPlacemark placemark = (KMLPlacemark) feature;
//            
//            KMLAbstractGeometry geometry = placemark.getGeometry();
//            if (geometry instanceof KMLPoint) {
//                KMLPoint point = (KMLPoint) geometry;
//                Position position = point.getCoordinates();
//                enu.addLabel(new Label(WWPanel.toLLH(position), name), java.awt.Color.GRAY);
//            } else if (geometry instanceof KMLLineString) {
//                java.awt.Color color = null;
//                if (style != null) {
//                    KMLLineStyle lineStyle = style.getLineStyle();
//                    if (lineStyle != null) {
//                        String colorString = lineStyle.getColor(); // aabbggrr
//                        if (colorString != null && colorString.length() == 8) {
//                            int r = Integer.parseInt(colorString.substring(6, 8), 16);
//                            int g = Integer.parseInt(colorString.substring(4, 6), 16);
//                            int b = Integer.parseInt(colorString.substring(2, 4), 16);
//                            int a = Integer.parseInt(colorString.substring(0, 2), 16);
//                            color = new java.awt.Color(r, g, b, a);
//                        }
//                    }
//                }
//                
//                KMLLineString line = (KMLLineString) geometry;
//                if (line.getCoordinates() != null) {
//                    List<? extends Position> positions = line.getCoordinates().list;
//                    List<LLH> llhList = new ArrayList<LLH>(positions.size());
//                    for (Position position : positions) {
//                        llhList.add(WWPanel.toLLH(position));
//                    }
//                    System.out.println("Found lineString of size "
//                            + positions.size());
//                    enu.addPolyline(llhList, color);
//                }
//            }
//        } else if (feature instanceof KMLFolder) {
//            KMLFolder folder = (KMLFolder) feature;
//            List<KMLAbstractFeature> features = folder.getFeatures();
//            for (KMLAbstractFeature kmlAbstractFeature : features) {
//                parseFeature(kmlAbstractFeature, enu);
//            }
//        } else if (feature instanceof KMLDocument) {
//            KMLDocument doc = (KMLDocument) feature;
//            List<KMLAbstractFeature> features = doc.getFeatures();
//            for (KMLAbstractFeature kmlAbstractFeature : features) {
//                parseFeature(kmlAbstractFeature, enu);
//            }
//        }
//    }
// 
 * 
 * */
    protected static File chooseFile(JFrame owner, javax.swing.filechooser.FileFilter filter) {
        JFileChooser fc = new JFileChooser(lastDirectory);
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            lastDirectory = file.getParentFile().getAbsolutePath();
            return file;
        } else {
            return null;
        }
    }
    
    protected static File chooseSaveFile(JFrame owner, javax.swing.filechooser.FileFilter filter) {
        JFileChooser fc = new JFileChooser(lastDirectory);
        fc.setFileFilter(filter);
        int returnVal = fc.showSaveDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            lastDirectory = file.getParentFile().getAbsolutePath();
            return file;
        } else {
            return null;
        }
    }
    
    public static List<Label> loadStationsFile(JFrame owner) {
        javax.swing.filechooser.FileFilter filter = 
            new FileNameExtensionFilter("Text/CSV File", "txt", "csv");
        File file = chooseFile(owner, filter);
        if (file != null) {
            LineNumberReader reader = null;
            try {
                reader = new LineNumberReader(new FileReader(file));
                System.out.println("Parsing the file " + file.getName());
                
                String firstLine = reader.readLine();
                Pattern pattern;
                if (firstLine.contains(",")) {
                    pattern = Pattern.compile(CSV_PATTERN);
                } else {
                    System.out.println("No commas found, whitespace will determine columns.");
                    pattern = Pattern.compile(SPACES_PATTERN);
                }
                
                List<String> columnNames = parse(firstLine, pattern);
                System.out.println("Found " + columnNames.size() + " column headers:" + columnNames);
                
                if (columnNames.size() < 4) {
                    String msg = "Found insufficient columns in the CSV file. Columns found: " + columnNames.size();
                    JOptionPane.showMessageDialog(owner, msg, "Load error",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                
                LoadStationsDialog dialog = 
                    new LoadStationsDialog(owner, true, columnNames.toArray(new String[1]), reader, pattern);
                dialog.setVisible(true);
                
                List<Label> stations = dialog.getResult();
                System.out.println("Loaded " + stations);
                
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
                return stations;
                
            } catch (IOException e) {
                String msg = e.getMessage();
                if (reader != null) {
                    msg = "Error reading on line number " + reader.getLineNumber() + " \n"+e.getMessage(); 
                }
                JOptionPane.showMessageDialog(owner, msg, "Load error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public static List<Quake> loadQuakesFile(JFrame owner) {
        javax.swing.filechooser.FileFilter filter = 
            new FileNameExtensionFilter("Text/CSV File", "txt", "csv");
        File file = chooseFile(owner, filter);
        if (file != null) {
            LineNumberReader reader = null;
            try {
                reader = new LineNumberReader(new FileReader(file));
                System.out.println("Parsing the file " + file.getName());
                
                String firstLine = reader.readLine();
                Pattern pattern;
                if (firstLine.contains(",")) {
                    pattern = Pattern.compile(CSV_PATTERN);
                } else {
                    System.out.println("No commas found, whitespace will determine columns.");
                    pattern = Pattern.compile(SPACES_PATTERN);
                }
                
                List<String> columnNames = parse(firstLine, pattern);
                System.out.println("Found " + columnNames.size() + " column headers:" + columnNames);
                
                if (columnNames.size() < 4) {
                    String msg = "Found insufficient columns in the CSV file. Columns found: " + columnNames.size();
                    JOptionPane.showMessageDialog(owner, msg, "Load error",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                
                LoadQuakeDialog dialog = 
                    new LoadQuakeDialog(owner, true, columnNames.toArray(new String[1]), reader, pattern);
                dialog.setVisible(true);
                
                List<Quake> quakes = dialog.getResult();
                System.out.println("Loaded " + quakes.size() + " earthquakes.");
                
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
                return quakes;
                
            } catch (IOException e) {
                String msg = e.getMessage();
                if (reader != null) {
                    msg = "Error reading on line number " + reader.getLineNumber() + " \n"+e.getMessage(); 
                }
                JOptionPane.showMessageDialog(owner, msg, "Load error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
    
    
    /** Parse one line.
     * @return List of Strings, minus their double quotes
     */
    public static List<String> parse(String line, Pattern pattern) {
      List<String> list = new ArrayList<String>();
      Matcher m = pattern.matcher(line);
      // For each field
      while (m.find()) {
        String match = m.group();
        if (match == null)
          break;
        match = match.trim();
        if (match.endsWith(",")) {  // trim trailing ,
          match = match.substring(0, match.length() - 1);
        }
        if (match.startsWith("\"")) { // assume also ends with
          match = match.substring(1, match.length() - 1);
        }
        if (match.length() == 0)
          match = null;
        list.add(match);
      }
      return list;
    }
    
    public static class LoadedData {
        public enum DataType {STATIONS, EARTHQUAKES, GREENS_FUNCT, 
            EXTENDED_DISPS, MODEL};
        public DataType isOfType = DataType.STATIONS;
        boolean wasCancelled=true;
        
        
        public List<Label> stations;
    }

    /**
     * Navigates to and reads in a displacements-file that can contain
     * pseudo-data appended below actual displacements. That pseudo-data
     * typically is zero and part of smoothing equations with the Green's
     * function matrix.
     * 
     * @param frame
     * @return
     * @throws IOException 
     */
    public static double[] findLoadOneColExtDisp(JFrame owner) throws IOException {
        File file = loadFile(owner);
        return loadOneColExtDisp(owner, file);
    }
    
    
    /**
     * Reads in the specified displacements-file that can contain
     * pseudo-data appended below actual displacements. That pseudo-data
     * typically is zero and part of smoothing equations with the Green's
     * function matrix.
     * 
     * @param frame
     * @return
     * @throws IOException 
     */
    public static double[] loadOneColExtDisp(JFrame owner, File file) 
                throws IOException {
        double[] retAry = null;
        
        if (file != null) {
            LineNumberReader reader = null;
            reader = new LineNumberReader(new FileReader(file));
            System.out.println("Parsing the extended displacement file " + 
                    file.getName());
            List<Double> disps = new ArrayList<Double>();
            String firstLine = reader.readLine();
            while (  firstLine != null ){
                String xS = firstLine;
                /* Skip comment header, the first line of some test-files. */
                if (xS.indexOf("#") != -1) {
                    firstLine = reader.readLine();
                    continue;
                }
                disps.add(Double.parseDouble(xS));
                firstLine = reader.readLine();
            }
            try {
                reader.close();
            } catch (IOException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            retAry = new double[disps.size()];
            for (int i = 0; i < disps.size(); i++)
                retAry[i] = disps.get(i);
        }
        return retAry;
    }
    
    /**
     * Saves any 2-dimensional array of doubles as a .m file so it can be read
     * by MATLAB. 
     * 
     * @param data
     *            The 2-dimensional array to save as a MATLAB file.
     * @param filePathName
     *            If only a name without a path, the last used directory
     *            will be used.
     */
    public static void save2DimDoubleArrayAsM(double[][] data, 
            String outPathName, String varName) {
        if (outPathName == null)
            return;
        /* If the calling method specified only a name without a path, 
         * then default to the app's current directory. */
        if (outPathName.indexOf("\\") == -1)
            outPathName = lastDirectory + "\\" + outPathName;
        outPathName = outPathName + M_FILE_EXTENSION;
        FileWriter writer = null;
        try {
            writer = new FileWriter(outPathName);
            /* MATLAB syntax: assign to variable g, the following matrix */
            writer.append(varName + " = [ ");
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    writer.append(Double.toString(data[i][j]));
                    if (j < data[i].length - 1)
                        writer.append(',');
                }
                writer.append(";\n");
            }
            writer.append(END_ASSIGNMENT_STRING);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "Save error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }
    

}
