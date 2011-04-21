package gov.usgs.dismodel.calc.greens.dialogs;

import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

public class SourceViewer extends SourceViewBase implements DataChangeEventFrier, GuiUpdateRequestListener {

    private static final long serialVersionUID = -7717260561926398248L;
    AllGUIVars allGuiVars;
    private SimulationDataModel simModel = null;
    private ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();

    public SourceViewer(JFrame owner, String title, AllGUIVars allGuiVars) {
        super(owner, false);
        setTitle(title);
        this.simModel = allGuiVars.getSimModel();
        this.allGuiVars = allGuiVars;
        this.addDataChangeEventListener(allGuiVars.getMainFrame());
        uiInit();
    }

    private void uiInit() {
        sourceTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Enabled", "Model" }) {
            Class[] types = new Class[] { java.lang.Boolean.class, java.lang.String.class };
            boolean[] canEdit = new boolean[] { true, false };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        DefaultTableModel tableModel = (DefaultTableModel) sourceTable.getModel();
        List<DisplacementSolver> sourceModels = simModel.getSourceModels();
        final int modelLen = sourceModels.size();
        for (int iter = 0; iter < modelLen; iter++) {
            tableModel.addRow(new Object[] { new Boolean(true), sourceModels.get(iter).toString() });
        }

        sourceTable.getColumnModel().getSelectionModel()
                .setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        sourceTable.getColumnModel().getColumn(0).setMinWidth(60);
        sourceTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        sourceTable.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    @Override
    protected void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    @Override
    protected void buttonEditActionPerformed(java.awt.event.ActionEvent evt) {
        int[] rowsSelected = sourceTable.getSelectedRows();
        List<DisplacementSolver> sourceModels = simModel.getSourceModels();
        for (int selectedRow : rowsSelected) {
            DisplacementSolver curModel = sourceModels.get(selectedRow);
            JDialog dialog = curModel.toJDialog(this, "Edit / View Source", selectedRow, allGuiVars);
            dialog.setVisible(true);
        }
    }

    @Override
    protected void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO account for already opened windows
        int[] rowsSelected = sourceTable.getSelectedRows();
        ArrayList<DisplacementSolver> sourceModels = simModel.getSourceModels();
        ArrayList<DisplacementSolver> sourceLB = simModel.getSourceLowerbound();
        ArrayList<DisplacementSolver> sourceUB = simModel.getSourceUpperbound();
        ArrayList<DisplacementSolver> fittedModels = simModel.getFittedModels();
        for (int selectedRow : rowsSelected) {
            sourceModels.remove(selectedRow);
            sourceLB.remove(selectedRow);
            sourceUB.remove(selectedRow);
            if (fittedModels.size() > selectedRow) fittedModels.remove(selectedRow);
        }
        uiInit();
        fireDataChangeEvent();
    }

    @Override
    public void guiUpdateAfterStateChange() {
        // TODO implement Gui updating after data change
    }

    @Override
    public void addDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.add(listener);

    }

    @Override
    public void removeDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.remove(listener);
    }

    public void fireDataChangeEvent() {
        for (DataChangeEventListener listener : this.dataChgListeners) {
            listener.updateAfterDataChange();
        }
    }

}
