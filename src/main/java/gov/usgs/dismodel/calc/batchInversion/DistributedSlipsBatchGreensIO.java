package gov.usgs.dismodel.calc.batchInversion;

import gov.usgs.dismodel.DisModel;
import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.calc.SolverException;
import gov.usgs.dismodel.calc.inversion.ConstrainedLinearLeastSquaresSolver;
import gov.usgs.dismodel.calc.inversion.CrossVal;
import gov.usgs.dismodel.calc.inversion.CrossValToMatlabOutputer;
import gov.usgs.dismodel.calc.inversion.EqualityAndBoundsSlipSolver;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * File input and output while inverting distributed slips, batch processing
 * input files including the Green's function.
 * 
 * The user would access these testing utils by choosing
 * "Green's function file(s)..." from the Source menu.
 * 
 * @author cforden
 * 
 */
public class DistributedSlipsBatchGreensIO {

    /*
     * class constants
     * **********************************************************
     * *****************
     */

    // .m MATLAB files
    private static final String COMMENT_TOKEN = "% ";
    private static final String END_ASSIGNMENT_STRING = SaveAndLoad.END_ASSIGNMENT_STRING;
    private static final String OUT_FILE_EXTENSION = SaveAndLoad.M_FILE_EXTENSION;

    private static final String IN_FILE_EXTENSION_LOWER = ".txt";
    private static final String IN_FILE_EXTENSION_UPPER = ".TXT";
    private static final String GREENS_FILE_PREFIX = "Gmatrix_";
    private static final String DISPLACEMENTS_FILE_PREFIX = "dvector_";
    private static final String MOMENT_FILE_PREFIX = "Mo_";
    private static final String SLIP_BOUNDS_FILE_PREFIX = "slip_bounds_";

    private static enum momentReaderState {
        START, CONSTRAINT_TYPE, MOMENT, MODULUS, AREAS
    };

    private static enum momentConstraintType {
        LESS, EQUAL, GREATER
    };

    /*
     * Instance variables
     * ************************************************************************
     */
    protected ConstrainedLinearLeastSquaresSolver solver = null;
    protected double[][] greensMatrix = null;
    protected double[] estimatedSlips = null;
    protected double[] displacements = null;
    private File greens1stInputFile = null;
    private File greensLatestInputFile = null;
    private FileWriter logFile = null;
    private BatchProcessGreensDlg dialog = null;
    private boolean procAllDatasets = false;
    private boolean writeSparse = false;
    private boolean useConstraintsFiles = false;
    private momentConstraintType momCons = momentConstraintType.EQUAL;
    private String outputDirPathname = null;
    private String inputDirPathname = null;

    /**
     * Reads a simple text file format that EHZ uses, into a matrix.
     * 
     * @param file
     * @throws NumberFormatException
     * @throws IOException
     */
    public void readGreensFile(File file) throws NumberFormatException, IOException {
        if (file == null)
            return;
        BufferedReader in = null;
        greensLatestInputFile = file;
        try {
            in = new BufferedReader(new FileReader(file));
            String lineIn;
            int iRow = 0;
            int iColumn = 0;
            double dValue = 0.0;
            class dataEntry {
                public int myRow;
                public int myColumn;
                public double myValue;

                public dataEntry(int row, int column, double value) {
                    myRow = row;
                    myColumn = column;
                    myValue = value;
                }
            }
            /*
             * a priori guess at number of data elements plus smoothing matrix
             * elements
             */
            final int typicalMatrixSize = 2000;
            ArrayList<dataEntry> dataList = new ArrayList<dataEntry>(typicalMatrixSize);
            while ((lineIn = in.readLine()) != null) {
                if (lineIn.startsWith("#"))
                    continue;

                StringTokenizer toks = new StringTokenizer(lineIn, " \t(,)");
                String sData = toks.nextToken();
                dValue = Double.valueOf(sData);

                int idxOpenParen = lineIn.indexOf("(");
                if (idxOpenParen == -1)
                    continue;
                String sRow = toks.nextToken();
                iRow = Integer.decode(sRow);
                String sColumn = toks.nextToken();
                iColumn = Integer.decode(sColumn);
                dataList.add(new dataEntry(iRow, iColumn, dValue));
            }
            /*
             * Assume the largest row, column indices were the last entry in the
             * data file!
             */
            greensMatrix = new double[iRow][iColumn];
            for (int i = 0; i < dataList.size(); i++) {
                greensMatrix[dataList.get(i).myRow - 1][dataList.get(i).myColumn - 1] = dataList.get(i).myValue;
            }
        } finally {
            in.close();
        }
    }

    public File writeOutputFile(JFrame frame, CrossValidationController cvc) {
        String inName = greensLatestInputFile.getName();
        String outName = inName.replace(new String(GREENS_FILE_PREFIX), new String(""));
        final String extension = "txt";
        StringBuilder outPathName = new StringBuilder(this.outputDirPathname);
        outPathName.append("\\");
        if (cvc != null)
            outPathName.append("CrossVal_out_");
        outPathName.append(solver.getAbbreviatedSolverName());
        outPathName.append("_" + outName);

        File out = null;
        if (greens1stInputFile.equals(greensLatestInputFile))
            /*
             * On the first file to be saved, let the user navigate to the
             * desired output directory
             */
            out = getFileToSave(frame, outPathName.toString(), extension);
        else
            /*
             * The user has already navigated to the desired output directory,
             * when s/he saved the first output file. So use that same dir for
             * all the other output files.
             */
            out = new File(outPathName.toString());
        if (out == null)
            return null;
        FileWriter writer = null;
        try {
            writer = new FileWriter(out);
            if (cvc == null) {
                String solverNotes = solver.getSolutionNotes();
                String commentLine = COMMENT_TOKEN + solverNotes.replaceAll("\n", "\n" + COMMENT_TOKEN) + "\n";
                writer.append(commentLine);
                writer.append(this.maybeMomentComment(COMMENT_TOKEN) + "\n");
                for (int i = 0; i < estimatedSlips.length; i++) {
                    String val = Double.toString(estimatedSlips[i]) + "\n";
                    writer.append(val);
                }
            } else {
                CrossVal cv = cvc.getCrossVal();
                double[] CVSS = cv.getCVSS();
                double[] gam = cvc.getGams();
                int numGammas = CVSS.length;
                writer.append("\n\n gamma  " + "\t" + ", SSE avg'd over " + "excluded stations \n\n");
                for (int i = 0; i < numGammas; i++) {
                    String val = gam[i] + "\t" + Double.toString(CVSS[i]) + "\n";
                    writer.append(val);
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
            return null;
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return out;
    }

    private File getFileToSave(JFrame frame, String outName, String extension) {
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text, then MATLAB files", extension);
        fc.setFileFilter(filter);
        fc.setSelectedFile(new File(outName));
        int returnVal = fc.showSaveDialog(frame);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return null;
        File out = fc.getSelectedFile();
        return out;
    }

    /**
     * Writes a .m file for a solution.
     * 
     * If cvc is not null, can write additional file(s), too.
     * 
     * @param frame
     *            Really a DisModel.AppFrame
     * @param slips
     *            estimated slips if a simple inversion had been performed and
     *            cvc == null.
     * @param textFilePathName
     *            typically includes "_CV" if cross-validation had been
     *            performed.
     * @param cvc
     *            null if a simple inversion had been performed
     * @throws IOException
     */
    public void writeMatlabOutputFile(JFrame frame, double[] slips, String textFilePathName,
            CrossValidationController cvc) throws IOException {
        if (textFilePathName == null)
            return;
        StringBuilder outPathName = new StringBuilder(StripTxtExtension(textFilePathName));
        StringBuilder resPathName = new StringBuilder(outPathName);
        outPathName.append(SaveAndLoad.M_FILE_EXTENSION);
        resPathName.append("_res");

        File out = new File(outPathName.toString());
        FileWriter writer = new FileWriter(out);
        try {

            if (cvc == null) { // Regular inversion by solver
                String solverNotesWithDupList = solver.getSolutionNotes();
                /*
                 * The solver passes back some info about the solution. Its
                 * string might or might not contain a new-line, making it
                 * multiline. Multiline header comments contain info that would
                 * duplicate comments following MATLAB values, so we keep only
                 * the first header line.
                 */
                final int endLinePos = solverNotesWithDupList.indexOf('\n');
                String firstSolutionNotesLine = null;
                if (endLinePos == -1)
                    firstSolutionNotesLine = solverNotesWithDupList;
                else
                    firstSolutionNotesLine = solverNotesWithDupList.substring(0, endLinePos);
                String commentLine = COMMENT_TOKEN + firstSolutionNotesLine + " of "
                        + Integer.toString(greensMatrix.length - greensMatrix[0].length) + " displacements.\n";

                writer.append(commentLine);
                writer.append(maybeMomentComment(COMMENT_TOKEN) + "\n");

            } else { // cross validation
                CrossValToMatlabOutputer cvo = new CrossValToMatlabOutputer(cvc);
                cvo.putOut(writer); // appends cross-validation results to the
                                    // .m
                /* Create a separate file for res */
                SaveAndLoad.save2DimDoubleArrayAsM(cvc.getCrossVal().getRes(), resPathName.toString(), "res");
                return;
            }

            double tol = 0.0;
            for (int i = 0; i < slips.length; i++)
                if (slips[i] > tol)
                    tol = slips[i];
            tol *= 1e-8;

            /* MATLAB syntax: assign to variable sh, the following matrix */
            writer.append("sh = [ ");
            for (int i = 0; i < slips.length; i++) {
                StringBuilder val = new StringBuilder(Double.toString(slips[i]));
                double lb = solver.getLowerBound(i);
                double ub = solver.getUpperBound(i);
                boolean boundExists = (!Double.isNaN(lb)) || (Double.isNaN(ub));
                if (boundExists) {
                    double above = slips[i] - lb;
                    double below = ub - slips[i];
                    boolean close = false;

                    val.append("\t" + COMMENT_TOKEN + " ");

                    if (Math.abs(above) < tol) {
                        val.append(Double.toString(above) + " above lb. ");
                        close = true;
                    }
                    if (Math.abs(below) < tol) {
                        val.append(Double.toString(below) + " below ub. ");
                        close = true;
                    }
                    if (!close)
                        val.append("\t\t");

                    val.append("\t");
                    if (!Double.isNaN(lb))
                        val.append(" lb=" + Double.toString(lb));
                    val.append("\t");
                    if (!Double.isNaN(ub))
                        val.append(" ub=" + Double.toString(ub));
                }

                val.append("\n");
                writer.append(val);
            }
            writer.append(END_ASSIGNMENT_STRING);
        } finally {
            writer.close();
        }
    } // writeMatlabOutputFile()

    /**
     * Provide a string to display in the GUI to the user, describing the
     * constraints on the fault's total moment.
     * 
     * @param commentToken
     *            The character that precedes comment-lines.
     * @return
     */
    private String maybeMomentComment(String commentToken) {
        String equalMom;
        String lbMom;
        String ubMom;
        int comments = 0;

        if (!useConstraintsFiles)
            return "";
        EqualityAndBoundsSlipSolver eqSolver = null;
        if (solver instanceof EqualityAndBoundsSlipSolver)
            eqSolver = (EqualityAndBoundsSlipSolver) solver;
        else
            return "";

        if (!Double.isNaN(eqSolver.getMomentEqualityConstraint())) {
            equalMom = "Moment constrained to equal " + eqSolver.getMomentEqualityConstraint() + ".  ";
            comments++;
        } else
            equalMom = "";

        if (!Double.isNaN(eqSolver.getMomentLowerBound())) {
            lbMom = "Moment lower bound = " + eqSolver.getMomentLowerBound() + ".  ";
            comments++;
        } else
            lbMom = "";

        if (!Double.isNaN(eqSolver.getMomentUpperBound())) {
            ubMom = "Moment upper bound = " + eqSolver.getMomentUpperBound() + ".  ";
            comments++;
        } else
            ubMom = "";

        if (comments == 0)
            return "";
        StringBuilder momentComment = new StringBuilder(commentToken + equalMom + lbMom + ubMom
                + "  Units are Pascals " + "times meters cubed.\n");
        if (equalMom.length() > 0)
            momentComment.append(commentToken + "The moment divided by shear " + "modulus must equal "
                    + eqSolver.getMomentEqualityConstraint() / eqSolver.getModulus() + " meters cubed.\n");
        return momentComment.toString();
    }

    /**
     * @param filePathName
     *            can end in no extension or .txt or ".TXT" at the end.
     * @param fileExtension
     * @return The string passed in, without ".txt" or ".TXT" at the end.
     */
    public static String StripTxtExtension(String inPathName) {
        if (inPathName == null)
            return null;

        String retPathName = null;
        if (inPathName.indexOf(".") == -1) {
            retPathName = inPathName + OUT_FILE_EXTENSION;
            return retPathName;
        }

        if ((inPathName.indexOf(IN_FILE_EXTENSION_LOWER) == inPathName.length() - 4)
                || inPathName.indexOf(IN_FILE_EXTENSION_UPPER) == inPathName.length() - 4)
            retPathName = inPathName.substring(0, inPathName.length() - 4);
        return retPathName;
    }

    public void xferSettingsFromDlg() {
        procAllDatasets = dialog.procAllFiles();
        this.useConstraintsFiles = dialog.useMomentFile();
    }

    public void xferSettingsFromDlg(ConstrainedLinearLeastSquaresSolver solver) {
        if (dialog == null)
            throw new SolverException("Please configure the inversion " + "settings by accessing the Inversion menu's "
                    + "'Constrained Linear Least Squares...' item.");
        if (dialog.quadProgCkBx())
            xferQuadProgSettingsFromDlg(dialog, solver);
    }

    private void xferQuadProgSettingsFromDlg(BatchProcessGreensDlg dlg, ConstrainedLinearLeastSquaresSolver solver) {
        EqualityAndBoundsSlipSolver qpSolver = null;
        if (!(solver instanceof EqualityAndBoundsSlipSolver))
            return;
        qpSolver = (EqualityAndBoundsSlipSolver) solver;
        if (dlg.allBlksNonNegCkBx())
            qpSolver.setAllBlksNonNeg();
        else
            qpSolver.removeConstraints();
    }

    /**
     * @return the greensMatrix
     */
    public double[][] getGreensMatrix() {
        return greensMatrix;
    }

    public void setDialog(BatchProcessGreensDlg dialog) {
        this.dialog = dialog;
    }

    /**
     * Solves Green's functions that the user had previously loaded.
     * <p>
     * Can batch-process many data sets in the same directory, if the user had
     * indicated s/he wanted them all solved.
     * 
     * @param frame
     * @param simModel 
     * @param crossValController 
     * @return True if this solves what the user requested, false if other
     *         solver-code should handle the request to solve something.
     *         Typically returns true iff the user had previously loaded a
     *         Green's function.
     */
    public boolean solveLoadedGreensFunctions(JFrame frame, SimulationDataModel simModel, CrossValidationController crossValController) {
        int datasetsProcessed = 0;
        double maxSSE = 0.0;
        String maxSseFilename = null;
        File outFile = null;
        double sse = 0.0;

        if (getGreensMatrix() == null)
            return false;
        if (greens1stInputFile == null)
            return true;
        inputDirPathname = greens1stInputFile.getParent();
        if (inputDirPathname == null || inputDirPathname.length() == 0)
            return true;
        /*
         * Ideally we would log to the output dir, but that makes it difficult
         * to log errors occuring while reading input, before the user chooses
         * the output dir.
         */
        createLogFile(inputDirPathname);
        log("\nProcessing Gmatrix and dvector files.\n\n");
        File inputDir = new File(inputDirPathname);
        File files[] = inputDir.listFiles();
        if (files.length == 0)
            return true;
        xferSettingsFromDlg();
        boolean firstTimeThruLoop = true;
        for (File file : files) {
            if (!procAllDatasets)
                /*
                 * For the case where the user did not want to batch-process all
                 * similar files in the same directory, short-circuit the file
                 * choice.
                 */
                file = greens1stInputFile;
            String fileName = file.getName();
            Date startingTime;
            Date finishTime;

            if (fileName.indexOf(GREENS_FILE_PREFIX) != 0)
                continue;
            if (!isTxtFilename(fileName))
                continue;
            if (firstTimeThruLoop && procAllDatasets) {
                /*
                 * If the user chose a Green's matrix file that happenned to NOT
                 * be the alphabetically first Green's file, pretend s/he chose
                 * the alphabetically first one, so s/he will get the chance to
                 * choose the output dir before the first file processed gets
                 * saved.
                 */
                firstTimeThruLoop = false;
                greens1stInputFile = file;
            }

            greensLatestInputFile = file;
            startingTime = new Date();
            /*
             * Now that we found another Green's function, is there a matching
             * displacement-vector file?
             */
            String datasetName = fileName.substring(GREENS_FILE_PREFIX.length());
            String dvectorFileName = DISPLACEMENTS_FILE_PREFIX + datasetName;
            File latestDisplacementsFile = null;
            for (File otherfile : files) {
                String otherName = otherfile.getName();
                if (dvectorFileName.equals(otherName))
                    latestDisplacementsFile = otherfile;
            }
            if (latestDisplacementsFile == null) {
                logMaybePopErrorDlg(frame, "Couldn't find displacements for " + fileName + "\n\n");
                continue;
            }
            log("Reading Green's matrix from  " + fileName + "\n");
            try {
                logFile.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                readGreensFile(greensLatestInputFile);
            } catch (Exception e1) {
                logMaybePopErrorDlg(frame, "Error reading Green's file " + fileName + ": " + e1.getMessage());
                continue;
            }

            try {
                setDisplacements(SaveAndLoad.loadOneColExtDisp(frame, latestDisplacementsFile));
            } catch (Exception e1) {
                log("While reading displacements file " + dvectorFileName + ": " + e1.getMessage());
                continue;
            }

            CrossValidationController cvc = null;
            try {
                solver = new EqualityAndBoundsSlipSolver(greensMatrix, displacements);
                xferSettingsFromDlg(solver);
                maybeProcessConstraintsFiles(frame);
                if (simModel.isCrossValidate()) {
                    cvc = crossValController;
                    log("Cross validating...\n");
                    cvc.crossValidate(frame, solver);
                } else {
                    estimatedSlips = solver.solve();
                    sse = solver.calcSSE(estimatedSlips);
                }
            } catch (Exception e1) {
                logMaybePopErrorDlg(frame, "While solving " + fileName + ": " + e1.getMessage());
                continue;
            }

            StringBuilder outPathName = new StringBuilder();
            try {
                finishTime = new Date();
                outFile = writeOutputFile(frame, cvc);
                if (outFile == null)
                    return false;
                if (sse > maxSSE) {
                    maxSSE = sse;
                    maxSseFilename = outFile.getName();
                }
                outPathName.append(outFile.getPath());
                outputDirPathname = outFile.getParent();
                writeMatlabOutputFile(frame, estimatedSlips, outPathName.toString(), cvc);
                long millis = finishTime.getTime() - startingTime.getTime();
                log("Processed " + outFile.getName() + " in " + millis / 1000.0 + " seconds.\n");
                if (millis > 60000)
                    log("(" + millis / 60000.0 + " minutes, or " + millis / 3600000.0 + " hours)\n");
                log("\n");
                if (simModel.isCrossValidate()) {
                    /*
                     * Save a copy of the best (generated by multiplying by the
                     * discovered, optimal gamma) Green's matrix, in two
                     * formats.
                     */
                    greensMatrix = crossValController.getCrossVal().getOptGreens();
                    String optGreensPathnameNoExt = outputDirPathname + "\\"
                            + StripTxtExtension(greensLatestInputFile.getName()) + "_optGam";
                    SaveAndLoad.save2DimDoubleArrayAsM(greensMatrix, optGreensPathnameNoExt, "greensOpt");
                    SaveMatrixAsTxt(greensMatrix, optGreensPathnameNoExt, "Green's matrix,including smoothing rows "
                            + "divided by gamma = " + cvc.getCrossVal().getOptGamma());
                } else {
                    /* Save a copy for QA'ing the results with MATLAB, later: */
                    SaveAndLoad.save2DimDoubleArrayAsM(greensMatrix,
                            StripTxtExtension(greensLatestInputFile.getPath()), "g");
                }
            } catch (Exception e1) {
                logMaybePopErrorDlg(frame,
                        "While writing output for " + greensLatestInputFile.getName() + ": " + e1.getMessage());
                /*
                 * If the user Cancels the first file, break out of the loop for
                 * processing all files.
                 */
                if (datasetsProcessed == 0)
                    break;
                continue;
            }
            datasetsProcessed++;
            if (!procAllDatasets)
                break;
        } // Loop over all the data-sets
        log(datasetsProcessed + " datasets Processed.  ");
        if (!simModel.isCrossValidate()) {
            log("The largest SSE was " + maxSSE + " in file " + maxSseFilename + "\n\n\n");
        }
        try {
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* After processing the file(s), allow the user to do something else: */
        greens1stInputFile = null;
        return true;
    }

    /**
     * Can be used for (among other purposes) saving a Green's matrix in a
     * file-format that DisModel can read back in.
     * 
     * @param matrixToWrite
     *            A 2D Java array to be written in a sparse format (zeros are
     *            skipped).
     * @param filePathnameNoExt
     *            The path and filename to be created or overwritten. ".txt"
     *            will automatically be appended.
     * @param comment
     *            Will appear as the top line of the output file, prepended with
     *            "##".
     */
    private void SaveMatrixAsTxt(double[][] matrixToWrite, String filePathnameNoExt, String comment) {
        String outPathName = new String(filePathnameNoExt + ".txt");
        File outFile = new File(outPathName);
        FileWriter writer = null;
        try {
            writer = new FileWriter(outFile);
            writer.append("## " + comment + "\n");
            for (int row = 0; row < matrixToWrite.length; row++)
                for (int col = 0; col < matrixToWrite[0].length; col++) {
                    double value = matrixToWrite[row][col];
                    if (writeSparse && value == 0.0)
                        continue;
                    Formatter f = new Formatter();
                    f.format("% 10.6f (", value);
                    String ds = f.toString();
                    writer.append(ds);
                    writer.append(Integer.toString(row + 1) + "," + (col + 1) + ")\n");
                }
        } catch (Exception e) {
            log(e.getMessage());
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void logMaybePopErrorDlg(JFrame frame, String errMsg) {
        log("Fault-grid error " + errMsg + "\n\n");
        /*
         * Raise a dialog box if the problem has occurred in the first processed
         * file; otherwise just log the error message and allow batch-processing
         * to continue.
         */
        if (greens1stInputFile.equals(greensLatestInputFile))
            JOptionPane.showMessageDialog(frame, errMsg, "Fault-grid error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Use special files to set various constraints.
     * 
     * If those files do not exist, or if the solver is not applicable to those
     * constraints, do not do anything.
     */
    private void maybeProcessConstraintsFiles(JFrame frame) {
        if (!useConstraintsFiles)
            return;
        EqualityAndBoundsSlipSolver eqSolver = null;
        if (solver instanceof EqualityAndBoundsSlipSolver)
            eqSolver = (EqualityAndBoundsSlipSolver) solver;
        else
            return;

        processMomentFile(frame, eqSolver);
        processSlipBoundsFile(frame, eqSolver);
    }

    /**
     * If a file with constraints on distributed slips of individual subfaults
     * (blocks), exists in the same directory, read it in and pass its
     * constraints to the solver.
     * 
     * Jessica and Chris used this for testing the algorithm.
     */
    private void processSlipBoundsFile(JFrame frame, EqualityAndBoundsSlipSolver eqSolver) {
        File slipBoundsFile = getConstraintsFile(SLIP_BOUNDS_FILE_PREFIX, "slip-bounds");
        if (slipBoundsFile == null)
            return;
        BufferedReader in = null;
        try {
            String lineIn;
            int index = -1;
            double lb = Double.NaN;
            double ub = Double.NaN;

            in = new BufferedReader(new FileReader(slipBoundsFile));
            while ((lineIn = in.readLine()) != null) {
                StringTokenizer toks = new StringTokenizer(lineIn, " \t(,)");
                String sIndex, sLb, sUb;

                if (lineIn.startsWith("index"))
                    continue;

                sIndex = toks.nextToken();
                index = Integer.decode(sIndex) - 1;

                sLb = toks.nextToken();
                lb = Double.parseDouble(sLb);
                eqSolver.setLowerBoundForABlockSlip(index, lb);

                sUb = toks.nextToken();
                ub = Double.parseDouble(sUb);
                eqSolver.setUpperBoundForABlockSlip(index, ub);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logMaybePopErrorDlg(frame, "Reading slip-bounds file " + slipBoundsFile.getName() + ": " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds a file based on a passed prefix that begins its filename. Typically
     * used to find a file listing constraints for solving distributed slips.
     * 
     * @param constraintsFilePrefix
     *            the sought file will begin with this.
     * @param fileKindDescription
     *            shown to the user in error/progress messages.
     * @return the file found
     */
    private File getConstraintsFile(String constraintsFilePrefix, String fileKindDescription) {
        String datasetName = greensLatestInputFile.getName().substring(GREENS_FILE_PREFIX.length());
        String dirPathName = greensLatestInputFile.getParent();
        File dir = new File(dirPathName);
        File files[] = dir.listFiles();
        String constraintFilename = constraintsFilePrefix + datasetName;
        File constraintFile = null;
        for (File file : files) {
            String name = file.getName();
            if (constraintFilename.equals(name))
                constraintFile = file;
        }
        if (constraintFile == null) {
            log("Couldn't find " + fileKindDescription + " file " + constraintFilename + "\n");
            return constraintFile;
        }
        log("Processing constraints in " + fileKindDescription + " file " + constraintFilename + "\n");
        try {
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return constraintFile;
    }

    /**
     * If a file with constraints on moment exists in the same directory, read
     * it in and pass its constraints to the solver.
     * 
     * Jessica and Chris used this for testing the algorithm.
     */
    private void processMomentFile(JFrame frame, EqualityAndBoundsSlipSolver eqSolver) {
        /*
         * The first dozen lines here could be replaced by a call to
         * getConstraintsFile() if you wanted to consolidate this code.
         */

        /* Is there a matching moment-constraint file? */
        String datasetName = greensLatestInputFile.getName().substring(GREENS_FILE_PREFIX.length());
        String dirPathName = greensLatestInputFile.getParent();
        File dir = new File(dirPathName);
        File files[] = dir.listFiles();
        String momentFilename = MOMENT_FILE_PREFIX + datasetName;
        File momentFile = null;
        for (File otherfile : files) {
            String otherName = otherfile.getName();
            if (momentFilename.equals(otherName))
                momentFile = otherfile;
        }
        if (momentFile == null) {
            log("Couldn't find moment-constraint file " + momentFilename + "\n");
            return;
        }
        log("Processing moment constraints in " + momentFilename + "\n");
        try {
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(momentFile));
            String lineIn;
            momentReaderState state = momentReaderState.START;
            double dValue = 0.0;
            double moment = 0.0;
            double modulus = 0.0;
            ArrayList<Double> areaList = new ArrayList<Double>();
            double[] areas;
            int numMoments = 0;
            int numModuli = 0;
            int numAreas = 0;

            while ((lineIn = in.readLine()) != null) {
                if (lineIn.startsWith("#")) {
                    switch (state) {
                    case START:
                        if (-1 != lineIn.indexOf("Moment constraint"))
                            state = momentReaderState.MOMENT;
                        continue;
                    case MOMENT:
                        if (-1 != lineIn.indexOf("Shear modulus (Pa)"))
                            state = momentReaderState.MODULUS;
                        continue;
                    case MODULUS:
                        if (-1 != lineIn.indexOf("subfault areas (m^2)"))
                            state = momentReaderState.AREAS;
                        continue;
                    }
                }
                StringTokenizer toks = new StringTokenizer(lineIn, " \t(,)");
                if (toks.hasMoreTokens()) {
                    if (state == momentReaderState.START && -1 != lineIn.indexOf("Estimated moment constrained to")) {
                        state = momentReaderState.MOMENT;
                        setConstraintType(lineIn);
                        continue;
                    }
                    String sData = toks.nextToken();
                    dValue = Double.valueOf(sData);
                    switch (state) {
                    case START:
                        throw new SolverException("Expecting ## Moment " + "constraint (N m), found number");
                    case MOMENT:
                        if (numMoments > 0)
                            throw new SolverException("Too many moments");
                        numMoments++;
                        moment = dValue;
                        continue;
                    case MODULUS:
                        if (numModuli > 0)
                            throw new SolverException("Too many moduli");
                        numModuli++;
                        modulus = dValue;
                        continue;
                    case AREAS:
                        numAreas++;
                        areaList.add(dValue);
                        continue;
                    }
                }
            } // while while ((lineIn = in.readLine
            if (numAreas != greensMatrix[0].length) {
                int rows = greensMatrix.length;
                int cols = greensMatrix[0].length;

                /*
                 * Maybe there are smoothing equations appended to the rows of
                 * equations; that would also be acceptable.
                 */
                if (numAreas != rows - cols)
                    throw new SolverException("Found " + numAreas + " subfault areas.  Expected " + rows
                            + " or, if smoothing, " + (rows - cols));
            }
            areas = new double[areaList.size()];
            for (int i = 0; i < areas.length; i++)
                areas[i] = areaList.get(i);
            eqSolver.setSubfaultAreas(areas);
            switch (momCons) {
            case LESS:
                eqSolver.setMomentUpperBound(moment, modulus);
                break;
            case EQUAL:
                eqSolver.setMoment(moment, modulus);
                break;
            case GREATER:
                eqSolver.setMomentLowerBound(moment, modulus);
                break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log("Reading moment-constraint file " + momentFilename + ": " + e.getMessage());
        }

        finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // maybeProcessMomentFile()

    private void setConstraintType(String lineIn) {
        int countSpecs = 0;
        if (lineIn.indexOf("<=") != -1) {
            momCons = momentConstraintType.LESS;
            countSpecs++;
        }
        if (lineIn.indexOf(">=") != -1) {
            momCons = momentConstraintType.GREATER;
            countSpecs++;
        }
        if (lineIn.indexOf("equal") != -1) {
            momCons = momentConstraintType.EQUAL;
            countSpecs++;
        }
        if (countSpecs != 1)
            throw new SolverException("Bad moment constraint specification");
    }

    private void log(String msg) {
        try {
            logFile.append(msg);
            System.out.print(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLogFile(String dirPathName) {
        try {
            logFile = new FileWriter(dirPathName + "\\GreensInversionLog.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTxtFilename(String fileName) {
        if (fileName.length() < IN_FILE_EXTENSION_LOWER.length())
            return false;
        final int posDot = fileName.lastIndexOf('.');
        if (posDot != fileName.length() - IN_FILE_EXTENSION_LOWER.length())
            return false;
        if (fileName.substring(posDot).equals(IN_FILE_EXTENSION_LOWER))
            return true;
        if (fileName.substring(posDot).equals(IN_FILE_EXTENSION_UPPER))
            return true;
        return false;
    }

    public void setDisplacements(double[] displacements) {
        this.displacements = displacements;
    }

    public void set1stGreensFile(File greensFile) {
        greens1stInputFile = greensFile;
    }
}
