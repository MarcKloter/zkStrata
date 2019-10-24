package zkstrata.utils;

import org.apache.commons.text.TextStringBuilder;
import zkstrata.exceptions.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ErrorUtils {
    private ErrorUtils() {
        throw new IllegalStateException("Utility class");
    }

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

    public static String underline(Position.Absolute position) {
        TextStringBuilder builder = new TextStringBuilder();
        processSource(builder, position.getSource());
        processLine(builder, sanitize(position.getStatement()), List.of(position), position.getLine());
        return builder.build();
    }

    public static String underline(List<Position.Absolute> positions) {
        TextStringBuilder builder = new TextStringBuilder();
        Map<String, String> statements = positions.stream()
                .collect(Collectors.toMap(Position.Absolute::getSource, Position.Absolute::getStatement, (s1, s2) -> s1));
        for (Map.Entry<String, String> statement : statements.entrySet()) {
            processSource(builder, statement.getKey());
            String value = sanitize(statement.getValue());
            Set<Integer> lineNumbers = positions.stream().map(Position::getLine).collect(Collectors.toSet());
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

    private static void processSource(TextStringBuilder builder, String source) {
        String prefix = " --> ";
        builder.append(prefix);
        builder.append(source);
        builder.appendNewLine();
    }

    /**
     * Partially adapted from 'The Definitive ANTLR 4 Reference' page 156.
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
