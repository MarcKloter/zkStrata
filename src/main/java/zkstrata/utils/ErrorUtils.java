package zkstrata.utils;

import org.apache.commons.text.TextStringBuilder;
import zkstrata.exceptions.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ErrorUtils {
    /**
     * Sanitizes the given String.
     * Required to remove specific symbols from inputs to ensure erroneous symbols get underlined properly.
     */
    public static String sanitize(String s) {
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\t') buf.append(" ");
            else if (c == '\u000B') buf.append(" ");
            else buf.append(c);
        }
        return buf.toString();
    }

    public static String underline(String statement, Position position) {
        return processLine(sanitize(statement), List.of(position), position.getLine());
    }

    public static String underline(String statement, List<Position> positions) {
        Set<Integer> lineNumbers = positions.stream().map(Position::getLine).collect(Collectors.toSet());
        return lineNumbers.stream().map(
                lineNumber -> processLine(sanitize(statement), positions.stream()
                        .filter(position -> position.getLine() == lineNumber)
                        .sorted(Comparator.comparing(Position::getPosition))
                        .collect(Collectors.toList()), lineNumber)
        ).collect(Collectors.joining());
    }

    /**
     * Partially adapted from 'The Definitive ANTLR 4 Reference' page 156.
     */
    private static String processLine(String statement, List<Position> positions, int errorLineNumber) {
        TextStringBuilder builder = new TextStringBuilder();

        String separator = " | ";
        int lineNumberLength = String.valueOf(errorLineNumber).length();
        for (int i = 0; i < lineNumberLength; i++) builder.append(' ');
        builder.appendln(separator);

        builder.append(errorLineNumber);
        builder.append(separator);

        String[] lines = statement.split("\\r?\\n");
        String errorLine = lines[errorLineNumber - 1];
        builder.appendln(errorLine);

        for (int i = 0; i < lineNumberLength; i++) builder.append(' ');
        builder.append(separator);

        int pointer = 0;
        for (Position position : positions) {
            int offsetLength = position.getPosition() - pointer;
            for (int i = 0; i < offsetLength; i++) builder.append(' ');
            int targetLength = position.getTarget().length();
            for (int i = 0; i < targetLength; i++) builder.append('^');
            pointer += offsetLength + targetLength;
        }

        builder.appendNewLine();
        return builder.build();
    }
}
