package gov.usgs.dismodel;

import gov.usgs.dismodel.calc.inversion.InversionProgressStats;

public interface InversionListener {
     void update(InversionProgressStats results);
}
