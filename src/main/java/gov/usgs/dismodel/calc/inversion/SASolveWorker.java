package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.InversionProgress;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.calc.inversion.InversionProgressStats;
import gov.usgs.dismodel.calc.inversion.InversionResults;
import gov.usgs.dismodel.calc.inversion.SimuAnnealCervelli;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.state.SimulationDataModel;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class SASolveWorker extends SwingWorker<InversionResults, InversionProgressStats> implements DataChangeEventFrier{
    InversionProgress p;
    SimuAnnealCervelli sa;
    AllGUIVars allGuiVars;
    SimulationDataModel simModel;
    protected ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();
    
    public SASolveWorker(AllGUIVars allGuiVars) {
        super();
        this.allGuiVars = allGuiVars;
        this.simModel = allGuiVars.getSimModel();
        this.addDataChangeEventListener(allGuiVars.getMainFrame());
    }

    @Override
    protected InversionResults doInBackground() throws Exception {
        DisplacementSolver[] modelArray = simModel.getSourceModels().toArray(new DisplacementSolver[0]);
        LocalENU[] stationLocArray = simModel.getStationLocations(simModel.getOrigin()).toArray(new LocalENU[0]);
        List<VectorXyz> vectors = simModel.getMeasuredUnrefdDispVectors();
        XyzDisplacement[] realDispArray = new XyzDisplacement[vectors.size()];
        for (int i = 0; i < realDispArray.length; i++) {
            realDispArray[i] = vectors.get(i).getDisplacement();
        }
        DisplacementSolver[] lowerbound = simModel.getSourceLowerbound().toArray(new DisplacementSolver[0]);
        DisplacementSolver[] upperbound = simModel.getSourceUpperbound().toArray(new DisplacementSolver[0]);

        simModel.getCovarWeighter().setCovarToIdentMatrixIfUnset(simModel);
        /*
         * Solving for many nonlinear params can take a long
         * time, so we use a worker thread and real-time graph
         * to display progress.
         */
        p = new InversionProgress(allGuiVars.getMainFrame(), false);
        p.setSolverWorker(SASolveWorker.this);
        p.setVisible(true);
        

        sa = new SimuAnnealCervelli(modelArray, stationLocArray, realDispArray,
                simModel.getCovarWeighter(), lowerbound, upperbound, simModel.getRefH(), p);

        
        return sa.calculate();
    }

    @Override
    protected void done() {
        p.dispose();
        InversionResults saResult;
        try {
            saResult = get();
        } catch (CancellationException e) {
            System.err
                    .println("Cancelled. Outputting best results so far");
            saResult = sa.getInversionResults();
            JOptionPane.showMessageDialog( allGuiVars.getMainFrame(),
                            "The inversion was cancelled; therefore, only the best set of params so far is shown.",
                            "Inversion cancelled", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            saResult = null;
        } 
        simModel.setFittedModels(new ArrayList<DisplacementSolver>( Arrays.asList(saResult.getFittedModels())));
        simModel.setSourceModels(new ArrayList<DisplacementSolver>( Arrays.asList(saResult.getFittedModels())));
        simModel.setChi2(saResult.getChiSquared());
        simModel.setModeledDisplacements(Arrays.asList(saResult.getStationDisplacements()));
        fireDataChangeEvent();
        

        JOptionPane.showMessageDialog(allGuiVars.getMainFrame(), saResult.toWrappedStrings(), "Solution", JOptionPane.PLAIN_MESSAGE);

        // show the results in their source dialogs
        // after solving
        ArrayList<DisplacementSolver> resultSrcs = new ArrayList<DisplacementSolver>( Arrays.asList(saResult.getFittedModelsB4ShiftedHeight()) ); 
        
        simModel.setSourceModels(resultSrcs);
        
        int dialogLength = resultSrcs.size();
        for (int diagIter = 0; diagIter < dialogLength; diagIter++) {
                DisplacementSolver curSrc = resultSrcs.get(diagIter);
            JDialog curDialog = curSrc.toJDialog( allGuiVars.getMainFrame(), "Edit / view a source", diagIter, allGuiVars);
            curDialog.setVisible(true);
        }
    }

    private void fireDataChangeEvent() {
        for (DataChangeEventListener listener : dataChgListeners) {
            listener.updateAfterDataChange();
        }
    }

    @Override
    public void addDataChangeEventListener(DataChangeEventListener listener) {
        this.dataChgListeners.add(listener);
    }

    @Override
    public void removeDataChangeEventListener(DataChangeEventListener listener) {
        this.dataChgListeners.remove(listener);
    }
}
