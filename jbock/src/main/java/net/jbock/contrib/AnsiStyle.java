package net.jbock.contrib;

import net.jbock.ColorMode;

import java.util.Objects;

record AnsiStyle(boolean noColor) {

    private static final char ESC = 0x1B;
    private static final String CSI = ESC + "[";
    private static final String RESET = CSI + "m";

    static AnsiStyle create(ColorMode colorMode) {
        boolean noco;
        if (colorMode == ColorMode.AUTO) {
            noco = !Objects.toString(System.getenv("NO_COLOR"), "").isEmpty();
        } else {
            noco = colorMode == ColorMode.NO_COLOR;
        }
        return new AnsiStyle(noco);
    }

    private String paint(String text, Style style) {
        if (noColor) {
            return text;
        } else {
            return CSI + style.code + 'm' + text + RESET;
        }
    }

    String bold(String text) {
        return paint(text, Style.BOLD);
    }

    String red(String text) {
        return paint(text, Style.FG_RED);
    }

    private enum Style {

        BOLD(1),
        FG_RED(31);

        final int code;

        Style(int code) {
            this.code = code;
        }
    }
}
