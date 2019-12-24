package zkstrata.exceptions;

public class InternalCompilerException extends RuntimeException {
    public InternalCompilerException(String message, Object... params) {
        super(String.format(message, params));
    }

    public InternalCompilerException(Throwable cause, String message, Object... params) {
        super(String.format(message, params), cause);
    }
}
