package gov.usgs.dismodel.calc.inversion;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum ConstraintType {
    @XmlEnumValue(value = "LESS_THAN_OR_EQUAL")         LESS_THAN_OR_EQUAL, 
    @XmlEnumValue(value = "EQUAL")                      EQUAL, 
    @XmlEnumValue(value = "GREATER_THAN_OR_EQUAL")      GREATER_THAN_OR_EQUAL
}
