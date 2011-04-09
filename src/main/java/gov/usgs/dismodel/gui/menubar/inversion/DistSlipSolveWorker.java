package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.FaultConnectionSpecDialog;
import gov.usgs.dismodel.InversionProgress;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.inversion.DistributedSlipSolver;
import gov.usgs.dismodel.calc.inversion.InversionProgressStats;
import gov.usgs.dismodel.calc.inversion.InversionResults;
import gov.usgs.dismodel.calc.inversion.SimuAnnealCervelli;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class DistSlipSolveWorker extends SwingWorker<InversionResults, InversionProgressStats> implements
        DataChangeEventFrier {
    InversionProgress p;
    SimuAnnealCervelli sa;
    AllGUIVars allGuiVars;
    SimulationDataModel simModel;
    protected ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();

    public DistSlipSolveWorker(AllGUIVars allGuiVars) {
        super();
        this.allGuiVars = allGuiVars;
        this.simModel = allGuiVars.getSimModel();
        this.addDataChangeEventListener(allGuiVars.getMainFrame());
    }

    @Override
    protected InversionResults doInBackground() throws Exception {

        FaultConnectionSpecDialog connDiag = new FaultConnectionSpecDialog(allGuiVars.getMainFrame(), true, simModel);
        connDiag.setVisible(true);
        final DistributedSlipSolver solver = DistributedSlipSolver.make(simModel);
        return solver.calculate();

    }

    @Override
    protected void done() {
        InversionResults dsSoln;
        try {
            dsSoln = get();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            dsSoln = null;
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            dsSoln = null;
        }

        simModel.setFittedModels(new ArrayList<DisplacementSolver>(Arrays.asList(dsSoln.getFittedModels())));
        simModel.setChi2(dsSoln.getChiSquared());
        simModel.setModeledDisplacements(Arrays.asList(dsSoln.getStationDisplacements()));
        simModel.setRefH(dsSoln.getRefHeight());
        fireDataChangeEvent();
        // enuViewer.drawRefHeight(simModel.getRefH(),
        // refHcolor);

        // JOptionPane.showMessageDialog(frame,
        // dsSoln.toString() ,"Solution",
        // JOptionPane.PLAIN_MESSAGE);

        // System.out.println("Sol'n" +
        // Arrays.toString(dsSoln.getFittedModels()));
        // System.out.println(dsSoln.getChi2());
        JFrame frame = allGuiVars.getMainFrame();
        JOptionPane.showMessageDialog(frame, "Dist'ed slips results: \nChi2:" + (dsSoln.getChi2()),
                "Distributed slips done", JOptionPane.PLAIN_MESSAGE);

        // show the results in their source dialogs
        // after solving
        ArrayList<DisplacementSolver> resultSrcs = simModel.getFittedModels();
        simModel.setSourceModels(resultSrcs);

        int dialogLength = resultSrcs.size();
        for (int diagIter = 0; diagIter < dialogLength; diagIter++) {
            DisplacementSolver curSrc = resultSrcs.get(diagIter);
            JDialog curDialog = curSrc.toJDialog(frame, "Edit / view a source", diagIter, allGuiVars);
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
