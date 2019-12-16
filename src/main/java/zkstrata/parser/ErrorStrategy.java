package zkstrata.parser;

import org.antlr.v4.runtime.*;
import zkstrata.parser.exceptions.MissingTokenException;
import zkstrata.parser.exceptions.UnwantedTokenException;

/**
 * ANTLR error strategy that overwrites two methods of {@link DefaultErrorStrategy} to enable distinguishing between
 * missing and unwanted tokens in the error listener by throwing dedicated exceptions {@link UnwantedTokenException}
 * and {@link MissingTokenException}.
 */
public class ErrorStrategy extends DefaultErrorStrategy {
    @Override
    protected void reportUnwantedToken(Parser recognizer) {
        Token offendingToken = recognizer.getCurrentToken();
        RecognitionException e = new UnwantedTokenException(recognizer, recognizer.getInputStream(), recognizer.getRuleContext());

        recognizer.notifyErrorListeners(offendingToken, "", e);
    }

    @Override
    protected void reportMissingToken(Parser recognizer) {
        Token offendingToken = recognizer.getCurrentToken();
        RecognitionException e = new MissingTokenException(recognizer, recognizer.getInputStream(), recognizer.getRuleContext());

        recognizer.notifyErrorListeners(offendingToken, "", e);
    }
}
