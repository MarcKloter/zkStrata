package zkstrata.utils;

import org.apache.commons.text.TextStringBuilder;

public class ErrorUtils {
    public static String sanitize(String s) {
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\t') buf.append(" ");
            else if (c == '\u000B') buf.append(" ");
            else buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Partially from 'The Definitive ANTLR 4 Reference' page 156.
     */
    public static String underlineError(String input, int start, int stop, int line, int position) {
        TextStringBuilder builder = new TextStringBuilder();

        String errorLineNumber = String.valueOf(line);
        String separator = " | ";
        for (int i = 0; i < errorLineNumber.length(); i++) builder.append(' ');
        builder.appendln(separator);

        builder.append(errorLineNumber);
        builder.append(separator);

        String[] lines = input.split("\\r?\\n");
        String errorLine = lines[line - 1];
        builder.appendln(errorLine);

        for (int i = 0; i < errorLineNumber.length(); i++) builder.append(' ');
        builder.append(separator);

        for (int i = 0; i < position; i++) builder.append(' ');
        if (start >= 0 && stop >= 0) {
            for (int i = start; i <= stop; i++) builder.append('^');
        }
        builder.appendNewLine();
        return builder.build();
    }
}
