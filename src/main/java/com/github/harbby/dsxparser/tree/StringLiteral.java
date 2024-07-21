package com.github.harbby.dsxparser.tree;

import java.util.Objects;
import java.util.PrimitiveIterator;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static java.util.Objects.requireNonNull;

public class StringLiteral extends Literal {
    private final String value;

    public StringLiteral(String value) {
        this(null, value);
    }

    public StringLiteral(NodeLocation location, String value) {
        super(location);
        requireNonNull(value, "value is null");
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getValueAsString() {
        return getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringLiteral that = (StringLiteral) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return formatStringLiteral(this.getValue());
    }

    @Override
    public String doGenSql() {
        return formatStringLiteral(value);
    }

    private static boolean charMatches(char startInclusive, char endInclusive, String sequence) {
        for (int i = sequence.length() - 1; i >= 0; i--) {
            char c = sequence.charAt(i);
            if (!(startInclusive <= c && c <= endInclusive)) {
                return false;
            }
        }
        return true;
    }

    static String formatStringLiteral(String s) {
        s = s.replace("'", "''");
//        if (CharMatcher.inRange((char) 0x20, (char) 0x7E).matchesAllOf(s)) {
//            return "'" + s + "'";
//        }
        if (charMatches((char) 0x20, (char) 0x7E, s)) {
            return "'" + s + "'";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("U&'");
        PrimitiveIterator.OfInt iterator = s.codePoints().iterator();
        while (iterator.hasNext()) {
            int codePoint = iterator.nextInt();
            checkArgument(codePoint >= 0, "Invalid UTF-8 encoding in characters: %s", s);
            if (isAsciiPrintable(codePoint)) {
                char ch = (char) codePoint;
                if (ch == '\\') {
                    builder.append(ch);
                }
                builder.append(ch);
            } else if (codePoint <= 0xFFFF) {
                builder.append('\\');
                builder.append(String.format("%04X", codePoint));
            } else {
                builder.append("\\+");
                builder.append(String.format("%06X", codePoint));
            }
        }
        builder.append("'");
        return builder.toString();
    }

    private static boolean isAsciiPrintable(int codePoint) {
        if (codePoint >= 0x7F || codePoint < 0x20) {
            return false;
        }
        return true;
    }
}
