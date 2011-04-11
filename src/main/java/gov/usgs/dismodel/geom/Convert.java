package gov.usgs.dismodel.geom;

import java.util.Calendar;
import java.util.GregorianCalendar;
import us.fed.fs.fpl.optimization.Fzero;
import us.fed.fs.fpl.optimization.Fzero_methods;

/**
 * Conversion Utils for ITRFXyz, LLH, and LocalUTM 
 * For simple conversions, use .to(TARGET) converting class methods or converting constructors instead,
 * @see ITRFXyz, LatLon, LLH, LocalENU
 * @author Chi Lam
 *
 */
public class Convert {
    private static final class CONFIG{
        public static final double CONV_REL_ERR = 2.220446049250313e-16;
        public static final double CONV_ABS_ERR = 2.220446049250313e-16;
        public static final double CONV_PHIP_LO = -Math.PI;
        public static final double CONV_PHIP_HI = Math.PI;
    }

    private static final double a = Earth.RADIUS_APPROX;
    private static final double f = Earth.FLATTENING;
    private static final double b = (1-f)*a;
    private static final double k0 = Earth.TRAN_MERCATOR_SCALE;
    private static final double k02 = (Math.pow(k0, 2d));
    private static final double e = (Math.sqrt(((2d * f) - (Math.pow(f, 2d)))));                               
    private static final double E2 = (Math.pow(e, 2d)) / (1d - (Math.pow(e, 2d)));
    private static final double e1 = ((1d - (Math.sqrt((1d - (Math.pow(e, 2d)))))) / (1d + (Math.sqrt((1d - (Math.pow(e, 2d)))))));
    private static final double ECCENTRICITY_SQ = 2d * f - Math.pow(f, 2d);
    private static final double SECOND_ECCENTRICITY_SQ = ( Math.pow(a, 2d) - Math.pow(b, 2d) ) / ( Math.pow(b, 2d) );
    private static final double n = (f / (2d - f));
    private static final double n2 = (Math.pow(n, 2d));
    private static final double n3 = (Math.pow(n, 3d));
    private static final double n4 = (Math.pow(n, 4d));
    private static final double n5 = (Math.pow(n, 5d));
    private static final double Ap = (a * (((1d - n) + ((5d * (n2 - n3)) / 4d)) + ((81d * (n4 - n5)) / 64d)));
    private static final double Bp = (((3d / 2d) * a) * (((n - n2) + ((7d * (n3 - n4)) / 8d)) + ((55d * n5) / 64d)));
    private static final double Cp = (((15d / 16d) * a) * ((n2 - n3) + (0.75d * (n4 - n5))));
    private static final double Dp = (((35d / 48d) * a) * ((n3 - n4) + ((11d * n5) / 16d)));
    private static final double Ep = (((315d / 512d) * a) * (n4 - n5));
    private static final double ep4 = (Math.pow(SECOND_ECCENTRICITY_SQ, 2d));
    private static final double ep6 = (Math.pow(SECOND_ECCENTRICITY_SQ, 3d));
    private static final double ep8 = (Math.pow(SECOND_ECCENTRICITY_SQ, 4d));
    private static final double UTM_MIN_LAT = -82d;
    private static final double UTM_MAX_LAT = 86d;
    private static final double TM_FN = 0; 
    private static final double TM_FE = 0;

    private static class PhipFunct implements Fzero_methods{
        //derived vars
        private double n;
        private double n2;
        private double n3;
        private double n4;
        private double n5;
        private double Ap;
        private double Bp;
        private double Cp;
        private double Dp;
        private double Ep;
        private double N_k0;
        
        
        
        public PhipFunct(final double a, final double f, final double N, final double k0) {
            super();
            //setting derived vars 
            n = f/(2-f);
            n2 = n*n;
            n3 = n2*n;
            n4 = n4*n;
            n5 = n4*n;
            Ap = (a * (((1d - n) + ((5d * (n2 - n3)) / 4d)) + ((81d * (n4 - n5)) / 64d)));
            Bp = (((3d / 2d) * a) * (((n - n2) + ((7d * (n3 - n4)) / 8d)) + ((55d * n5) / 64d)));
            Cp = (((15d / 16d) * a) * ((n2 - n3) + (0.75d * (n4 - n5))));
            Dp = (((35d / 48d) * a) * ((n3 - n4) + ((11d * n5) / 16d)));
            Ep = (((315d / 512d) * a) * (n4 - n5));
            N_k0 = N / k0;
        }

        @Override
        public double f_to_zero(final double phip) {
            final double S = Ap*phip-Bp*Math.sin(2*phip)+Cp*Math.sin(4*phip)-Dp*Math.sin(6*phip)+Ep*Math.sin(8*phip);
            return (N_k0 - S);
        }
        
    }
    
    //Conventional toTARGET converting functions
    //===========================================
    public static LocalENU toLocalENU(LLH point, LLH projectionTangentPoint){
        return LLH2LocalENU(point, projectionTangentPoint);
    }
    
    public static LocalENU toLocalENU(ITRFXyz point, ITRFXyz projectionTangentPoint){
        return ITRFXyz2LocalENU(point, projectionTangentPoint);
    }
    
    public static LLH toLLH(LocalENU point){
        return LocalENU2LLH(point);
    }
    
    public static LLH toLLH(ITRFXyz point){
        return ITRFXyz2LLH(point);
    }
    
    public static LLH toLLH(LatLon point){
        return new LLH(point.getLatitude(), point.getLongitude(), 0d);
    }
    
    public static ITRFXyz toITRFXyz(LocalENU point){
        return LocalENU2ITRFXyz(point);
    }
    
    public static ITRFXyz toITRFXyz(LLH point){
        return LLH2ITRFXyz(point);
    }
    
    public static ITRFXyz toITRFXyz(ITRF05Xyz point){
        return ITRF05Xyz2ITRFXyz(point);
    }
    
    public static UTM toUTM(LatLon point){
        return LatLon2UTM(point);
    }
    
    public static LatLon toLatLon(UTM point){
        return UTM2LatLon(point);
    }
    
    //Actual conversion implementations
    //=================================
    public static LLH LocalENU2LLH(LocalENU point){
        double x = point.getX();
        double y = point.getY();
        double z = point.getZ();
        
        LLH origin = point.getProjectionTangentPointLLH();
        double phi0 = origin.getLatitude().toRad();
        double lambda0 = origin.getLongitude().toRad();
        
        
       double DN = y - TM_FN;
       double DE = x - TM_FE;
       double DE2 = (Math.pow(DE, 2d));
       double DE3 = (Math.pow(DE, 3d));
       double DE4 = (Math.pow(DE, 4d));
       double DE5 = (Math.pow(DE, 5d));
       double DE6 = (Math.pow(DE, 6d));
       double DE7 = (Math.pow(DE, 7d));
       double DE8 = (Math.pow(DE, 8d));
       
       
       PhipFunct phipfunct = new PhipFunct(a, f, DN, k0);
       double[] lowerbound = new double[]{0, CONFIG.CONV_PHIP_LO};
       double[] upperbound = new double[]{0, CONFIG.CONV_PHIP_HI};
       int[] fzeroStatus = new int[2];
       Fzero.fzero(phipfunct, lowerbound, upperbound, DN/Ap, CONFIG.CONV_REL_ERR, CONFIG.CONV_ABS_ERR, fzeroStatus);
       final double phip = lowerbound[1];
       final double cosp = Math.cos(phip);
       final double cos2p = (Math.pow(cosp, 2d));
       final double cos4p = (Math.pow(cosp, 4d));
       final double cos6p = (Math.pow(cosp, 6d));
       final double cos8p = (Math.pow(cosp, 8d));
       final double tanp = Math.tan(phip);
       final double tan2p = (Math.pow(tanp, 2d));
       final double tan4p = (Math.pow(tanp, 2d));
       final double tan6p = (Math.pow(tanp, 2d));
       final double v = (a / (Math.sqrt((1d - (ECCENTRICITY_SQ * (Math.pow((Math.sin(phip)), 2d)))))));
       final double v2 = Math.pow(v, 2d);
       final double rho = ((v * (1d - ECCENTRICITY_SQ)) / (1d - (ECCENTRICITY_SQ * (Math.pow((Math.sin(phip)), 2d)))));
       final double T10 = ((Math.tan(phip)) / (((2d * rho) * v) * k02));
       final double T11 = ((T10 / ((12d * v2) * k02)) * ((((5d + (3d * tan2p)) + (SECOND_ECCENTRICITY_SQ * cos2p))
               - ((4d * ep4) * cos4p)) - (((9d * tan2p) * SECOND_ECCENTRICITY_SQ) * cos2p)));
       final double T12 = ((T11 / ((30d * v2) * k02)) * ((((((((((((61d + (90d * tan2p)) + ((46d * SECOND_ECCENTRICITY_SQ)
               * cos2p)) + (45d * tan4p)) - (((252d * tan2p) * SECOND_ECCENTRICITY_SQ) * cos2p)) - ((3d * ep4) * cos4p)) + 
               ((100d * ep6) * cos6p)) - (((66d * tan2p) * ep4) * cos4p)) - (((90d * tan4p) * SECOND_ECCENTRICITY_SQ)
                       * cos2p)) + ((88d * ep8) * cos8p)) + (((225d * tan4p) * ep4) * cos4p)) + (((84d * tan2p) * ep6)
                               * cos6p)) - (((192d * tan2p) * ep8) * cos8p)));
       final double T13 = ((T12 / ((56d * v2) * k02)) * (((1385d + (3633d * tan2p)) + (4095d * tan4p)) + (1575d * tan6p)));
       final double T14 = (1/(v*cosp*k0));
       final double T15 = (T14/(6*v2*k02))*(1 + 2*tan2p + SECOND_ECCENTRICITY_SQ*cos2p);
       final double T16 = (T15/(20*v2*k02))*(5 + 6*SECOND_ECCENTRICITY_SQ*cos2p + 28*tan2p - 3*SECOND_ECCENTRICITY_SQ*cos4p + 8*tan2p*SECOND_ECCENTRICITY_SQ*cos2p 
               +24*tan4p - 4*ep6*cos6p + 4*tan4p*ep4*cos4p + 24*tan2p*ep6*cos6p);
       final double T17 = (T16/(42*v2*k02))*(61 + 662*tan2p + 1320*tan4p + 720*tan6p);
       final double phi = phi0 + phip - DE2*T10 + DE4*T11 - DE6*T12 + DE8*T13;
       final double lambda = lambda0 + DE*T14 - DE3*T15 + DE5*T16 - DE7*T17; 
       final double hei = origin.getHeight() + z;
       return new LLH(Angle.fromRad(phi), Angle.fromRad(lambda), hei);
    }
    
    public static LocalENU LLH2LocalENU(LLH point, LLH projectionTangentPoint){
        double yOffset = LLH2LocalENUProc(projectionTangentPoint, projectionTangentPoint)[INDEX.NORTH] ;
        double [] ENUCoords = LLH2LocalENUProc(point, projectionTangentPoint);
        return new LocalENU(ENUCoords[INDEX.EAST], ENUCoords[INDEX.NORTH]-yOffset, ENUCoords[INDEX.UP], projectionTangentPoint);
    }
    
    public static LatLon UTM2LatLon(UTM point){
        final double E = point.getEasting();
        final double N = point.getNorthing();
   
        final double lambda0_ = point.getCentralMeridian().toRad();
        double FN = 0;
        if (point.getHemisphere() == 'S'){
            FN = 10000000d;
        }
        final double FE = 500000d;
        final double DN = N - FN;
        final double DE = E - FE;
        final double DE2 = DE*DE;
        final double DE3 = DE2*DE;
        final double DE4 = DE3*DE;
        final double DE5 = DE4*DE;
        final double DE6 = DE5*DE;
        final double DE7 = DE6*DE;
        final double DE8 = DE7*DE;
        
        PhipFunct phipfunct = new PhipFunct(a, f, DN, k0);
        double[] lowerbound = new double[]{0, CONFIG.CONV_PHIP_LO};
        double[] upperbound = new double[]{0, CONFIG.CONV_PHIP_HI};
        int[] fzeroStatus = new int[2];
        Fzero.fzero(phipfunct, lowerbound, upperbound, DN/Ap, CONFIG.CONV_REL_ERR, CONFIG.CONV_ABS_ERR, fzeroStatus);
        final double phip = lowerbound[1];
        final double cosp = Math.cos(phip);
        final double cos2p = cosp*cosp;
        final double cos4p = cos2p*cos2p;
        final double cos6p = cos2p*cos4p;
        final double cos8p = cos4p*cos4p;
        final double tanp = Math.tan(phip);
        final double tan2p = tanp*tanp;
        final double tan4p = tan2p*tan2p;
        final double tan6p = tan2p*tan4p;
        final double v = (a / (Math.sqrt((1d - (ECCENTRICITY_SQ * (Math.pow((Math.sin(phip)), 2d)))))));
        final double v2 = v*v;
        final double rho = ((v * (1d - ECCENTRICITY_SQ)) / (1d - (ECCENTRICITY_SQ * (Math.pow((Math.sin(phip)), 2d)))));
        final double T10 = ((Math.tan(phip)) / (((2d * rho) * v) * k02));
        final double T11 = ((T10 / ((12d * v2) * k02)) * ((((5d + (3d * tan2p)) + (SECOND_ECCENTRICITY_SQ * cos2p)) - ((4d * ep4) * cos4p)) - (((9d * tan2p) * SECOND_ECCENTRICITY_SQ) * cos2p)));
        final double T12 = ((T11 / ((30d * v2) * k02)) * ((((((((((((61d + (90d * tan2p)) + ((46d * SECOND_ECCENTRICITY_SQ) * cos2p)) + (45d * tan4p)) - (((252d * tan2p) * SECOND_ECCENTRICITY_SQ) * cos2p)) - ((
                3d * ep4) * cos4p)) + ((100d * ep6) * cos6p)) - (((66d * tan2p) * ep4) * cos4p)) - (((90d * tan4p) * SECOND_ECCENTRICITY_SQ) * cos2p)) + ((88d * ep8) * cos8p)) + (((225d * tan4p)
                * ep4) * cos4p)) + (((84d * tan2p) * ep6) * cos6p)) - (((192d * tan2p) * ep8) * cos8p)));
        final double T13 = ((T12 / ((56d * v2) * k02)) * (((1385d + (3633d * tan2p)) + (4095d * tan4p)) + (1575d * tan6p)));
        final double T14 = (1d / ((v * cosp) * k0));
        final double T15 = ((T14 / ((6d * v2) * k02)) * ((1d + (2d * tan2p)) + (SECOND_ECCENTRICITY_SQ * cos2p)));
        final double T16 = ((T15 / ((20d * v2) * k02)) * ((((((((5d + ((6d * SECOND_ECCENTRICITY_SQ) * cos2p)) + (28d * tan2p)) - ((3d * SECOND_ECCENTRICITY_SQ) * cos4p))
                + (((8d * tan2p) * SECOND_ECCENTRICITY_SQ) * cos2p)) + (24d * tan4p)) - ((4d * ep6) * cos6p)) + (((4d * tan4p) * ep4) * cos4p)) + (((24d * tan2p) * ep6) * cos6p)));
        final double T17 = ((T16 / ((42d * v2) * k02)) * (((61d + (662d * tan2p)) + (1320d * tan4p)) + (720d * tan6p)));
        final double phi = ((((phip - (DE2 * T10)) + (DE4 * T11)) - (DE6 * T12)) + (DE8 * T13));
        final double lambda = ((((lambda0_ + (DE * T14)) - (DE3 * T15)) + (DE5 * T16)) - (DE7 * T17));
        
        return new LatLon(Angle.fromRad(phi), Angle.fromRad(lambda));
    }
    
    public static UTM LatLon2UTM(LatLon point){
        Angle longAng = point.getLongitude();
        Angle latAng = point.getLatitude();
        
        int utmZone;
        char hemisphere;
        double FN;
        
        try {
            utmZone = findUTMZone(latAng, longAng);
        }catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        Angle centralMeridian = UTM.getCentralMeridian(utmZone);
        
        double phi = latAng.toRad();
        double lambda = longAng.toRad();
        double lambda0 = centralMeridian.toRad();
        
        double S = Ap*phi-Bp*Math.sin(2*phi)+Cp*Math.sin(4*phi)-Dp*Math.sin(6*phi)+Ep*Math.sin(8*phi);
        double dlambda = lambda - lambda0; 
        
        if (phi<0){
            FN = 10000000;
            hemisphere = 'S';
        } else {
            FN = 0;
            hemisphere = 'N';
        }
        double FE = 500000;
        
        double v = (a / (Math.sqrt((1d - (ECCENTRICITY_SQ * (Math.pow((Math.sin(phi)), 2d)))))));                       
        
        double sinp = Math.sin(phi);
        double cosp = Math.cos(phi);
        double cos2p = (Math.pow(cosp, 2d));
        double cos4p = (Math.pow(cosp, 4d));
        double cos6p = (Math.pow(cosp, 6d));
        double cos8p = (Math.pow(cosp, 8d));
        double tanp = (Math.tan(phi));
        double tan2p = (Math.pow(tanp, 2d));
        double tan4p = (Math.pow(tanp, 4d));
        double tan6p = (Math.pow(tanp, 6d));
        
        double T1 = S*k0;
        double T2 = v*sinp*cosp*k0/2;
        double T3 = (T2*cos2p/12)*(5-tan2p+9*SECOND_ECCENTRICITY_SQ*cos2p+4*ep4*cos4p);
        double T4 = (((T2 * cos4p) / 360d) * ((((((((((61d - (58d * tan2p)) + tan4p) +
                ((270d * SECOND_ECCENTRICITY_SQ) * cos2p)) - (((330d * tan2p) * SECOND_ECCENTRICITY_SQ) * cos2p)) + ((445d * ep4) * 
                 cos4p)) + ((324d * ep6) * cos6p)) - (((680d * tan2p) * ep4) * cos4p)) + ((88d * 
                 ep8) * cos8p)) - (((600d * tan2p) * ep6) * cos6p)) - (((192d * tan2p) * ep8) * 
                 cos8p)));
        double T5 = (((T2 * cos6p) / 20160d) * (((1385d - (3111d * tan2p)) + (543d * tan4p)) - tan6p));
        double T6 = v*cosp*k0;
        double T7 = (((T6 * cos2p) / 6d) * ((1d - tan2p) + (SECOND_ECCENTRICITY_SQ * cos2p)));
        
        double T8 = (((T6 * cos4p) / 120d) * ((((((((5d - (18d * tan2p)) + tan4p) + ((14d * SECOND_ECCENTRICITY_SQ) * cos2p))
                - (((58d * tan2p) * SECOND_ECCENTRICITY_SQ) * cos2p)) + ((13d * ep4) * cos4p)) + ((4d * ep6) * cos6p)) - 
                (((64d * tan2p) * ep4) * cos4p)) - (((24d * tan2p) * ep6) * cos6p)));
        double T9 = (((T6 * cos6p) / 5040d) * (((61d - (479d * tan2p)) + (179d * tan4p)) - tan6p));
        double northing = (FN + ((((T1 + (T2 * (Math.pow(dlambda, 2d)))) + (T3 * (Math.pow(dlambda, 4d)))) + 
                (T4 * (Math.pow(dlambda, 6d)))) + (T5 * (Math.pow(dlambda, 8d)))));
        double easting = (FE + ((((T6 * dlambda) + (T7 * (Math.pow(dlambda, 3d)))) + (T8 * (Math.pow(dlambda, 5d)))) + (T9 * (Math.pow(dlambda, 7d))))); 
        
        return new UTM(hemisphere, utmZone, easting, northing);
    }
    
	public static ITRFXyz LLH2ITRFXyz(LLH latLongHei){
		//Shortcut vars
		final double phi = latLongHei.getLatitude().toRad();
		final double lam = latLongHei.getLongitude().toRad();
		final double height = latLongHei.getHeight();
		
		//Convert llh to xyz
		double N = Math.pow(a, 2d) / Math.hypot(a * Math.cos(phi), b * Math.sin(phi) );
		double x = (N + height) * Math.cos(phi) * Math.cos(lam);
		double y = (N + height) * Math.cos(phi) * Math.sin(lam);
		double z = (  N * Math.pow( ( b/a ), 2d ) + height  ) * Math.sin(phi);
		
		return new ITRFXyz(x, y, z);
	}

	public static LLH ITRFXyz2LLH(ITRFXyz xyz){
		//shortcut vars
		double x = xyz.getX();
		double y = xyz.getY();
		double z = xyz.getZ();
		
		//Calculate longitude, latitude, and height
		double p = Math.hypot(x, y); 
		double lon = Math.atan2(y, x);
		double theta = Math.atan((z*a)/(p*b));
		double lat = Math.atan(( z + SECOND_ECCENTRICITY_SQ * b * Math.pow(  Math.sin(   theta   ), 3d  ) ) / ( p - ECCENTRICITY_SQ * a * Math.pow(  Math.cos(   theta   ), 3d) ));
		double N = a / Math.sqrt(1 - ECCENTRICITY_SQ * Math.pow( Math.sin(  lat  ),2d )); 
		double hei = p / Math.cos(lat) - N;
		
		return(new LLH(Angle.fromRad(lat), Angle.fromRad(lon), hei) );	
	}
	
	public static ITRFXyz ITRF05Xyz2ITRFXyz(ITRF05Xyz point){
	    final double t0 = (double) ITRF05Xyz.DEFAULT_EPOCH_YEAR;
	    final double [] ds = new double[]{  0.0001, -0.0008,  -0.0058};
	    final double [] dr = new double[]{ -0.0002,  0.0001,  -0.0018};
	    final double Rscale = Math.PI /(180d*3600d);
	    final double [] Rp  = matrixMultiply(Rscale, new double[]{ 0.000, 0.000, 0.000});
	    final double [] Rrp = matrixMultiply(Rscale, new double[]{ 0.000, 0.000, 0.000});
	    final double Rx = Rp[0];
	    final double Ry = Rp[1];
	    final double Rz = Rp[2];
	    final double Rrx = Rrp[0];
	    final double Rry = Rrp[1];
	    final double Rrz = Rrp[2];
	    final double[][] Rs = new double[][]{{1, Rz, -Ry}, {-Rz, 1, Rx}, {Ry, -Rx, 1}};
	    final double ssc = 0.40;
	    final double src = 0.08;
	    
	    final GregorianCalendar curPointCal = point.getEpoch();

	    GregorianCalendar epochYearEnd = new GregorianCalendar( ITRF05Xyz.DEFAULT_TIME_ZONE );
	    epochYearEnd.set(curPointCal.get(Calendar.YEAR), Calendar.DECEMBER, 31);
	    
	    final double t =  (double) curPointCal.get(Calendar.YEAR) + ( (double) curPointCal.get(Calendar.DAY_OF_YEAR) / (double) epochYearEnd.get(Calendar.DAY_OF_YEAR) );  
	    final double [][] d  = new double[][] {{ds[0]+dr[0]*(t-t0), ds[1]+dr[1]*(t-t0), ds[2]+dr[2]*(t-t0)}};
	    
	    final double[][] Rt = new double[][]{{0, Rrz*(t-t0), -Rry*(t-t0)}, {-Rrz*(t-t0), 0, Rrx*(t-t0)}, {Rry*(t-t0), -Rrx*(t-t0), 0}};
	    double[][] R = matrixAdd(Rs, Rt);
	    final double sc = 1E-9d*(ssc + src*(t-t0));
	    
	    final double [][] tempSum = matrixTranspose(matrixMultiply( matrixMultiply((1+sc), R), new double[][]{{point.getX()},{point.getY()},{point.getZ()}}));
	    final double [][] retTemp =  matrixAdd(d, tempSum );
	    final double retX = retTemp[0][INDEX.X];
	    final double retY = retTemp[0][INDEX.Y];
	    final double retZ = retTemp[0][INDEX.Z];
	    return new ITRFXyz(retX, retY, retZ);
	}
	
	public static LocalENU ITRFXyz2LocalENU(ITRFXyz xyz, ITRFXyz projectionTangentPoint){
	    return (LLH2LocalENU(ITRFXyz2LLH(xyz), ITRFXyz2LLH(projectionTangentPoint)));
	};
	
	/**
	 * Batch converter from ITRFXyz to LocalENU
	 * Provided for interface compatibility
	 * @param xyzArray
	 * @param projectionTangentPoint
	 * @return
	 * @deprecated
	 */
	public static LocalENU[] ITRFXyz2LocalENU(ITRFXyz[] xyzArray, ITRFXyz projectionTangentPoint){
	   LLH projTanPtLLH = ITRFXyz2LLH(projectionTangentPoint);
	   int arrayLen = xyzArray.length;
	   LocalENU[] outArray = new LocalENU[arrayLen];
	   for (int iter = (arrayLen - 1); iter >=0; iter--){
	       outArray[iter] = LLH2LocalENU(ITRFXyz2LLH(xyzArray[iter]), projTanPtLLH);
	   }
	   return outArray;
	}

	public static ITRFXyz LocalENU2ITRFXyz(LocalENU point){
	    LLH pointLLH = LocalENU2LLH(point);
	    return (LLH2ITRFXyz(pointLLH));
	}
	
	/**
	 * Indices to traverse an ENU double[] array after a conversion
	 * @author Chi Lam
	 *
	 */
	protected static final class INDEX{
		public static final int EAST = 0;
		public static final int NORTH = 1;
		public static final int UP = 2;
		public static final int X = 0;
		public static final int Y = 1;
		public static final int Z = 2;
	}
	
	/**
	 * @param lon double, longitude of the origin in degrees 
	 * @param lat double, latitude of the origin in degrees
	 * @return A double[row][col] 3x3 transformation matrix
	 */
	protected static double[][] makeXyz2ENUMat(LatLon projectTangentPoint){
		final double lonR = projectTangentPoint.getLongitude().toRad();
		final double latR = projectTangentPoint.getLatitude().toRad();
		final double s1 = Math.sin(lonR);
		final double s2 = Math.sin(latR);
		final double c1 = Math.cos(lonR);
		final double c2 = Math.cos(latR);
		return(new double[][]{{-s1, c1, 0},{-s2*c1, -s2*s1, c2},{c2*c1, c2*s1, s2}});
	}
	
	//lightweight Matrix Utils
	/**
	 * Multiply two double[row][col] arrays: R = A * B
	 * @param a double[row][col] array A (left)
	 * @param b double[row][col] array B (right)
	 * @return double[row][col] product array (R)
	 */
	protected static double[][] matrixMultiply(final double[][] a,	final double[][] b){
		final int m = a.length;
		final int n = a[0].length;
		final int p = b.length;
		final int q = b[0].length;
		if (n != p) return (null);
		double[][] c = new double[m][q]; 
		final double[] bColJ = new double[p];
		for (int j = 0; j < q; j++) {
			for (int k = 0; k < p; k++) {
				bColJ[k] = b[k][j];
			}
			for (int i = 0; i < m; i++) {
				final double[] aRowI = a[i];
				double s = 0;
				for (int k = 0; k < p; k++) {
					s += aRowI[k] * bColJ[k];
				}
				c[i][j] = s;
			}
		}
		return(c);
	}
	
	protected static double[][] matrixMultiply(final double a, final double[][] b){
	    final int rowCt = b.length;
	    final int colCt = b[0].length;
	    double [][] tempOut = new double[rowCt][colCt];
	    for (int rowIter = (rowCt-1); rowIter >= 0; rowIter--){
	        for (int colIter = (colCt-1); colIter >= 0; colIter--){
	            tempOut[rowIter][colIter] = b[rowIter][colIter] * a;
	        }
	    }
	    return tempOut;
	}
	
	protected static double[] matrixMultiply(final double a, final double[] b){
	   final int matLen = b.length;
	   double[] tempOut = new double[matLen];
	   for (int iter = (matLen-1); iter >= 0 ; iter--){
	       tempOut[iter] = b[iter] * a;
	   }
	   return tempOut;
	}
	
	
	/**
	 * Transpose a double[row][col] matrix
	 * @param matrixIn double[row][col], matrix to be transposed (the matrix won't be changed)
	 * @return double[row][col], tranposed matrixIn
	 */
	protected static double[][] matrixTranspose(final double[][] matrixIn){
		final int m = matrixIn.length;
		final int n = matrixIn[0].length;
		double[][] outArray = new double[n][m];
		for (int iter1 = 0; iter1 < m; iter1++){
			for (int iter2 = 0; iter2 < n; iter2++){
				outArray[iter2][iter1] = matrixIn[iter1][iter2];
			}
		}
		return (outArray);
	}
	
	
	protected static double[][] matrixAdd(final double[][] a, final double[][] b){
        final int rowCt = a.length;
        final int colCt = a[0].length;
        double [][] tempOut = new double[rowCt][colCt];
        for (int rowIter = (rowCt-1); rowIter >= 0; rowIter--){
            for (int colIter = (colCt-1); colIter >= 0; colIter--){
                tempOut[rowIter][colIter] = a[rowIter][colIter] + b[rowIter][colIter];
            }
        }
        return tempOut;
	}

	   private static int findUTMZone(Angle latitude, Angle longitude) throws Exception {
	        double Long_Rad = longitude.toRad();
	        double Lat_Degrees = latitude.toDeg();
	        double Long_Degrees = longitude.toDeg();
	        
	        long temp_zone;
	        
	        if ((Lat_Degrees < UTM_MIN_LAT) || (Lat_Degrees > UTM_MAX_LAT))
	        { 
	            throw new Exception("Latitude out of range");
	        }
	        if ((Long_Rad < -Math.PI) || (Long_Rad > (2 * Math.PI)))
	        { 
	            throw new Exception("Longitude out of range");
	        }

	        if (Long_Rad < 0){
	            Long_Rad += (2 * Math.PI) + 1.0e-10;
	        }
	        if (Long_Rad < Math.PI)
	            temp_zone = (long) (31 + (Math.toDegrees(Long_Rad) / 6.0));
	        else
	            temp_zone = (long) ((Math.toDegrees(Long_Rad) / 6.0) - 29);
	        if (temp_zone > 60)
	            temp_zone = 1;
	        
	        /* UTM special cases */
	        if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > -1) && (Long_Degrees < 3))
	            temp_zone = 31;
	        if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > 2) && (Long_Degrees < 12))
	            temp_zone = 32;
	        if ((Lat_Degrees > 71) && (Long_Degrees > -1) && (Long_Degrees < 9))
	            temp_zone = 31;
	        if ((Lat_Degrees > 71) && (Long_Degrees > 8) && (Long_Degrees < 21))
	            temp_zone = 33;
	        if ((Lat_Degrees > 71) && (Long_Degrees > 20) && (Long_Degrees < 33))
	            temp_zone = 35;
	        if ((Lat_Degrees > 71) && (Long_Degrees > 32) && (Long_Degrees < 42))
	            temp_zone = 37;
	        return (int) temp_zone;
	}
	    
	    private static double[] LLH2LocalENUProc(LLH point, LLH projectionTangentPoint){
	        

	        double LAMBDA0 = projectionTangentPoint.getLongitude().toRad();
	        //double PHI0 = projectionTangentPoint.getLatitude().toRad();
	        
	        //shift coordinates
	        double lambda = point.getLongitude().toRad();
	        double phi = point.getLatitude().toRad();

	        
	        // Ellipsoid parameters (DMA, 1989, 2-2.1) 
	        double v = (a / (Math.sqrt((1d - (ECCENTRICITY_SQ * (Math.pow((Math.sin(phi)), 2d)))))));
	        double S = (((((Ap * phi) - (Bp * (Math.sin((2d * phi))))) + (Cp * (Math.sin((4d * phi))))) - (Dp * (Math.sin((6d * phi))))) + (Ep * (Math.sin((8d * phi)))));
	        
	        //Universal Transversal Mercator projection parameters (DMA, 1989, 2-2.1)
	        double dlambda = lambda - LAMBDA0;
	        
	        // Terms used to calculate general equations (DMA, 1989, 2-2.3)
	        double sinp = (Math.sin(phi));
	        double cosp = (Math.cos(phi));
	        double cos2p = (Math.pow(cosp, 2d));
	        double cos4p = (Math.pow(cosp, 4d));
	        double cos6p = (Math.pow(cosp, 6d));
	        double cos8p = (Math.pow(cosp, 8d));
	        double tanp = (Math.tan(phi));
	        double tan2p = (Math.pow(tanp, 2d));
	        double tan4p = (Math.pow(tanp, 4d));
	        double tan6p = (Math.pow(tanp, 6d));
	        
	        double T1 = S*k0;
	        double T2 = ((((v * sinp) * cosp) * k0) / 2d);
	        double T3 = (((T2 * cos2p) / 12d) * (((5d - tan2p) + ((9d * SECOND_ECCENTRICITY_SQ) * cos2p)) + ((4d * ep4) * cos4p)));
	        double T4 = (((T2 * cos4p) / 360d) * ((((((((((61d - (58d * tan2p)) + tan4p) + ((270d * SECOND_ECCENTRICITY_SQ) * cos2p)) - (((330d * tan2p) * SECOND_ECCENTRICITY_SQ)
	                * cos2p)) + ((445d * ep4) * cos4p)) + ((324d * ep6) * cos6p)) - (((680d * tan2p) * ep4) * cos4p)) + ((88d * ep8) * cos8p)) - (((600d * tan2p) * ep6) *
	                cos6p)) - (((192d * tan2p) * ep8) * cos8p)));
	        double T5 = (((T2 * cos6p) / 20160d) * (((1385d - (3111d * tan2p)) + (543d * tan4p)) - tan6p));
	        double T6 = v*cosp*k0;
	        double T7 = (T6*cos2p/6)*(1-tan2p+SECOND_ECCENTRICITY_SQ*cos2p);
	        double T8 = (((T6 * cos4p) / 120d) * ((((((((5d - (18d * tan2p)) + tan4p) + ((14d * SECOND_ECCENTRICITY_SQ) * cos2p)) - (((58d * tan2p) * SECOND_ECCENTRICITY_SQ
	                ) * cos2p)) + ((13d * ep4) * cos4p)) + ((4d * ep6) * cos6p)) - (((64d * tan2p) * ep4) * cos4p)) - (((24d * tan2p) * ep6) * cos6p)));
	        double T9 = (T6*cos6p/5040)*(61-479*tan2p+179*tan4p-tan6p);
	        
	        //Conversion of geographic coordinates to grid coordinates (DMA, 1989, 2-5)
	        double Y = (TM_FN + ((((T1 + (T2 * (Math.pow(dlambda, 2d)))) + (T3 * (Math.pow(dlambda, 4d)))) + (T4 * (Math.pow(dlambda, 6d)))) + (T5 * (Math.pow(dlambda, 8d)))
	        ));
	        double X = (TM_FE + ((((T6 * dlambda) + (T7 * (Math.pow(dlambda, 3d)))) + (T8 * (Math.pow(dlambda, 5d)))) + (T9 * (Math.pow(dlambda, 7d)))));
	        
	        double Z = point.getHeight() - projectionTangentPoint.getHeight();
	        
	        return new double[] {X, Y, Z}; 
	    }
}

