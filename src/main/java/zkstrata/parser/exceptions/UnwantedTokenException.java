package zkstrata.parser.exceptions;

import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class UnwantedTokenException extends RecognitionException {
    public UnwantedTokenException(Recognizer<?, ?> recognizer, IntStream input, ParserRuleContext ctx) {
        super(recognizer, input, ctx);
    }
}
