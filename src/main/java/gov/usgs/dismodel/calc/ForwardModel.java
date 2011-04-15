package gov.usgs.dismodel.calc;

import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;

import java.util.ArrayList;
import java.util.List;

public class ForwardModel {
    static public boolean forwardAllFixedSrcs(SimulationDataModel simModel) {
        boolean allFixed = true;

        ArrayList<DisplacementSolver> models2solve = simModel.getSourceModels();
        ArrayList<DisplacementSolver> models2solveLB = simModel.getSourceLowerbound();
        ArrayList<DisplacementSolver> models2solveUB = simModel.getSourceUpperbound();

        int modelLen = models2solve.size();

        ArrayList<DisplacementSolver> fittedModels = new ArrayList<DisplacementSolver>(modelLen);
        simModel.setFittedModels(fittedModels);

        for (int modelIter = 0; modelIter < modelLen; modelIter++) {
            DisplacementSolver curVal = models2solve.get(modelIter);
            DisplacementSolver curLB = models2solveLB.get(modelIter);
            DisplacementSolver curUB = models2solveUB.get(modelIter);

            if (DisplacementSolver.areAllVarsFixed(curVal, curLB, curUB)) {
                fittedModels.add(curVal);
            } else {
                fittedModels.add(null);
                allFixed = false;
            }
        }

        updateModelDisp(simModel);

        return allFixed;
    }

    private static void updateModelDisp(SimulationDataModel simModel) {
        ArrayList<DisplacementSolver> modelArray = simModel.getFittedModels();
        int modelCt = modelArray.size();

        LLH origin = simModel.getOrigin();
        List<LocalENU> stationPositions = simModel.getStationLocations(origin);
        int numStation = stationPositions.size();

        ArrayList<XyzDisplacement> modeledDispArray = new ArrayList<XyzDisplacement>(numStation);
        simModel.setModeledDisplacements(modeledDispArray);

        for (int stationIter = 0; stationIter < numStation; stationIter++) {
            double xDisp = 0d;
            double yDisp = 0d;
            double zDisp = 0d;
            for (int modelIter = modelCt - 1; modelIter >= 0; modelIter--) {
                DisplacementSolver curModel = modelArray.get(modelIter);
                if (curModel != null) {
                    XyzDisplacement curDisp = curModel.solveDisplacement((stationPositions.get(stationIter)));
                    xDisp += curDisp.getX();
                    yDisp += curDisp.getY();
                    zDisp += curDisp.getZ();
                }
            }
            modeledDispArray.add(new XyzDisplacement(xDisp, yDisp, zDisp));
        }
    }
}
