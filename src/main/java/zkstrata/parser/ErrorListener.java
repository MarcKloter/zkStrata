package zkstrata.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import zkstrata.exceptions.CompileException;
import zkstrata.exceptions.InternalCompilerErrorException;
import zkstrata.parser.ast.Position;
import zkstrata.parser.exceptions.MissingTokenException;
import zkstrata.parser.exceptions.UnwantedTokenException;

public class ErrorListener extends BaseErrorListener {
    // TODO: can we get the file name? (debug.zkstrata) --> probably not here
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (e instanceof LexerNoViableAltException)
            unexpectedSymbol(recognizer, line, charPositionInLine);
        else if (e instanceof NoViableAltException)
            unexpectedToken(recognizer, (Token) offendingSymbol, line, charPositionInLine);
        else if (e instanceof MissingTokenException)
            unexpectedToken(recognizer, (Token) offendingSymbol, line, charPositionInLine);
        else if (e instanceof InputMismatchException)
            unexpectedInput(recognizer, (Token) offendingSymbol, line, charPositionInLine);
        else if (e instanceof UnwantedTokenException)
            unexpectedToken(recognizer, (Token) offendingSymbol, line, charPositionInLine);
        else
            throw new InternalCompilerErrorException("Unknown RecognitionException");
    }

    private void unexpectedSymbol(Recognizer<?, ?> recognizer, int line, int position) {
        CharStream charStream = (CharStream) recognizer.getInputStream();
        String input = charStream.toString();
        String message = String.format("Unexpected symbol %s.", charStream.getText(Interval.of(position, position)));
        throw new CompileException(message, input, 1, new Position(line, position));
    }

    private void unexpectedToken(Recognizer<?, ?> recognizer, Token token, int line, int position) {
        CommonTokenStream commonTokenStream = (CommonTokenStream) recognizer.getInputStream();
        String input = commonTokenStream.getTokenSource().getInputStream().toString();
        String message = String.format("Unexpected token %s.", token.getText());
        throw new CompileException(message, input, token.getText().length(), new Position(line, position));
    }

    private void unexpectedInput(Recognizer<?, ?> recognizer, Token token, int line, int position) {
        CommonTokenStream commonTokenStream = (CommonTokenStream) recognizer.getInputStream();
        String input = commonTokenStream.getTokenSource().getInputStream().toString();
        String message = String.format("Unexpected input %s.", token.getText());
        throw new CompileException(message, input, token.getText().length(), new Position(line, position));
    }
}
