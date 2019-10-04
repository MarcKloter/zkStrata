package zkstrata.exceptions;

/**
 * Wrapper for ICEs.
 */
public class InternalCompilerErrorException extends RuntimeException {
    // TODO: get file and line where exception occured

    public InternalCompilerErrorException(String message) {
        super(message);
    }
}
