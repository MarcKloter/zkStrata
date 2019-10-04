package zkstrata.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;
import zkstrata.parser.exceptions.MissingTokenException;
import zkstrata.parser.exceptions.UnwantedTokenException;

public class ErrorStrategy extends DefaultErrorStrategy {
    @Override
    protected void reportUnwantedToken(Parser recognizer) {
        if (inErrorRecoveryMode(recognizer))
            return;

        beginErrorCondition(recognizer);

        Token t = recognizer.getCurrentToken();
        String tokenName = getTokenErrorDisplay(t);
        IntervalSet expecting = getExpectedTokens(recognizer);
        String msg = "extraneous input " + tokenName + " expecting " + expecting.toString(recognizer.getVocabulary());
        RecognitionException e = new UnwantedTokenException(recognizer, recognizer.getInputStream(), recognizer.getRuleContext());

        recognizer.notifyErrorListeners(t, msg, e);
    }

    @Override
    protected void reportMissingToken(Parser recognizer) {
        if (inErrorRecoveryMode(recognizer))
            return;

        beginErrorCondition(recognizer);

        Token t = recognizer.getCurrentToken();
        IntervalSet expecting = getExpectedTokens(recognizer);
        String msg = "missing " + expecting.toString(recognizer.getVocabulary()) + " at " + getTokenErrorDisplay(t);
        RecognitionException e = new MissingTokenException(recognizer, recognizer.getInputStream(), recognizer.getRuleContext());

        recognizer.notifyErrorListeners(t, msg, e);
    }
}
