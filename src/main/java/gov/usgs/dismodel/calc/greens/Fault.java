package gov.usgs.dismodel.calc.greens;

public interface Fault {
    double getX1();
    double getY1();
    double getX2();
    double getY2();
    double getXc();
    double getYc();
    double getDepth();
    double getStrike();
    double getDip();
    double getAspectRatio();
    double getLength();
    double getWidth();
    double getStrikeSlip();
    double getDipSlip();
    double getOpening();
    double getLowerX1();
    double getUpperX1();
    double getLowerY1();
    double getUpperY1();
    double getLowerX2();
    double getUpperX2();
    double getLowerY2();
    double getUpperY2();
    double getLowerXC();
    double getLowerYC();
    double getUpperXC();
    double getUpperYC();
    double getLowerUp();
    double getUpperUp();
    double getUp();
    boolean isTopCoords();
    int getGroup();
}
