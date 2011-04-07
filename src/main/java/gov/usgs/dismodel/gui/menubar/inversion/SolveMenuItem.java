package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.InversionProgress;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.calc.inversion.InversionProgressStats;
import gov.usgs.dismodel.calc.inversion.InversionResults;
import gov.usgs.dismodel.calc.inversion.SimuAnnealCervelli;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class SolveMenuItem extends DataChangingMenuItem implements GuiUpdateRequestListener{
    private static final long serialVersionUID = -33958112440444633L;

    private SimulationDataModel simModel;

    public SolveMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
        this.simModel = allGuiVars.getSimModel();
        
        this.addDataChangeEventListener(allGuiVars.getMainFrame());
        allGuiVars.getMainFrame().addGuiUpdateRequestListener(this);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        if (!simModel.isDistributedFaultProblem()) {
            SASolveWorker saSolver = new SASolveWorker();
            saSolver.execute();
            
        } else {
        }
    }

        

    @Override
    public void guiUpdateAfterStateChange() {
        // TODO Auto-generated method stub
        
    }
    
    
    private class SASolveWorker extends SwingWorker<InversionResults, InversionProgressStats>{          //TODO put this out to a separate class
        InversionProgress p;
        SimuAnnealCervelli sa;
        
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

            /*
             * Solving for many nonlinear params can take a long
             * time, so we use a worker thread and real-time graph
             * to display progress.
             */
            p = new InversionProgress(allGuiVars.getMainFrame(), false);
            p.setSolverWorker(SASolveWorker.this);
            p.setVisible(true);

            sa = new SimuAnnealCervelli(modelArray, stationLocArray, realDispArray,
                    simModel.getCovarWeighter(), lowerbound, upperbound, p);

            
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
            double refH = saResult.getRefHeight();
            simModel.setRefH(refH);
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
    }
}
