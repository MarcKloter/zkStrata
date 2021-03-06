package zkstrata.utils;

import org.apache.commons.text.TextStringBuilder;
import zkstrata.exceptions.Position;

import java.util.*;
import java.util.stream.Collectors;

public class ErrorUtils {
    private ErrorUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Underlines all errors that occurred within the statement provided as set of {@link Position.Absolute} using
     * symbol {@code ^}.
     *
     * @param positions set of {@link Position.Absolute}
     * @return formatted message with all lines where an error occurred and underlined positions
     */
    public static String underline(Set<Position.Absolute> positions) {
        TextStringBuilder builder = new TextStringBuilder();
        Map<String, String> statements = positions.stream()
                .collect(Collectors.toMap(Position.Absolute::getSource, Position.Absolute::getStatement, (s1, s2) -> s1));
        for (Map.Entry<String, String> statement : statements.entrySet()) {
            processSource(builder, statement.getKey());
            String value = sanitize(statement.getValue());
            Set<Integer> lineNumbers = positions.stream()
                    .filter(pos -> pos.getSource().equals(statement.getKey()))
                    .map(Position::getLine)
                    .collect(Collectors.toSet());
            for (int lineNumber : lineNumbers) {
                processLine(
                        builder,
                        value,
                        positions.stream()
                                .filter(position -> position.getLine() == lineNumber)
                                .sorted(Comparator.comparing(Position::getPosition))
                                .collect(Collectors.toList()),
                        lineNumber
                );
            }
        }

        return builder.build();
    }

    /**
     * Appends the given source to the provided string builder.
     *
     * @param builder {@link TextStringBuilder} to append to
     * @param source  error source to append to the builder
     */
    private static void processSource(TextStringBuilder builder, String source) {
        String prefix = " --> ";
        builder.append(prefix);
        builder.append(source);
        builder.appendNewLine();
    }

    /**
     * Sanitizes the given {@link String}.
     * Required to remove control characters from inputs to ensure erroneous symbols can be underlined properly.
     *
     * @param s {@link String} to sanitize
     * @return sanitized string
     */
    private static String sanitize(String s) {
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\t') buf.append(" ");
            else if (c == '\u000B') buf.append(" ");
            else buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Appends all errors of the given {@code errorLineNumber} to the provided string builder (underlined using ^).
     * <p>
     * Partially adapted from 'The Definitive ANTLR 4 Reference' page 156.
     *
     * @param builder         {@link TextStringBuilder} string builder to append to
     * @param statement       string representation of the statement to access the error line from
     * @param positions       list of {@link Position} within the error line
     * @param errorLineNumber line number of the statement where errors occurred
     */
    private static void processLine(TextStringBuilder builder, String statement, List<Position> positions, int errorLineNumber) {
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
    }
}
