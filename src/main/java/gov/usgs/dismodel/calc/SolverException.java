package gov.usgs.dismodel.calc;

/**
 * When a calculation goes bad. We intend to create only this type of Exception
 * inside calc code. The GUI should catch exceptions, show the user their
 * messages, and allow the user to modify settings and try the calculation
 * again. Of course, we might accidentally throw various Java Exceptions as
 * well.
 * 
 * @author cforden
 * 
 */
public class SolverException extends RuntimeException {
    private static final long serialVersionUID = -4802041286760977379L;

    public SolverException(String msg) {
        super(msg);
    }
}
