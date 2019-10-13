package zkstrata.exceptions;

public class InternalCompilerException extends RuntimeException {
    // TODO: get file and line where exception occured

    public InternalCompilerException(String message) {
        super(message);
    }

    public InternalCompilerException(Throwable cause, String message) {
        super(message, cause);
    }

    public InternalCompilerException(String message, Object... params) {
        super(String.format(message, params));
    }

    public InternalCompilerException(Throwable cause, String message, Object... params) {
        super(String.format(message, params), cause);
    }
}
