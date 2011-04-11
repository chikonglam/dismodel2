package gov.usgs.dismodel;

import javax.swing.JFrame;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;

public interface RestorableSourceDialog {
    int getModelIndex();

    boolean isCreateNew();
    
}
