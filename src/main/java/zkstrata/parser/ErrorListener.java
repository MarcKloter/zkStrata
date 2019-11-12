package zkstrata.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.ParserException;
import zkstrata.exceptions.Position;
import zkstrata.parser.exceptions.MissingTokenException;
import zkstrata.parser.exceptions.UnwantedTokenException;

public class ErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
    ) {
        if (e instanceof LexerNoViableAltException)
            unexpectedSymbol(recognizer, ((LexerNoViableAltException) e).getStartIndex(), line, charPositionInLine);
        else if (e instanceof NoViableAltException
                || e instanceof MissingTokenException
                || e instanceof UnwantedTokenException)
            unexpectedToken((Token) offendingSymbol, line, charPositionInLine);
        else if (e instanceof InputMismatchException)
            unexpectedInput((Token) offendingSymbol, line, charPositionInLine);
        else
            throw new InternalCompilerException("Unknown RecognitionException %s", e.getClass());
    }

    private void unexpectedSymbol(Recognizer<?, ?> recognizer, int absolutePosition, int line, int relativePosition) {
        CharStream charStream = (CharStream) recognizer.getInputStream();
        String symbol = charStream.getText(Interval.of(absolutePosition, absolutePosition));
        String message = String.format("Unexpected symbol %s.", symbol);
        throw new ParserException(message, new Position.Relative(symbol, line, relativePosition));
    }

    private void unexpectedToken(Token token, int line, int position) {
        String message = String.format("Unexpected token %s.", token.getText());
        throw new ParserException(message, new Position.Relative(token.getText(), line, position));
    }

    private void unexpectedInput(Token token, int line, int position) {
        String message = String.format("Unexpected input %s.", token.getText());
        throw new ParserException(message, new Position.Relative(token.getText(), line, position));
    }
}
