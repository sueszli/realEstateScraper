package realestatescraper.util;

public class ColoredPrint {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void printBlack(String in) {
        System.out.println(ANSI_BLACK + in + ANSI_RESET);
    }

    public static void printRed(String in) {
        System.out.println(ANSI_RED + in + ANSI_RESET);
    }

    public static void printGreen(String in) {
        System.out.println(ANSI_GREEN + in + ANSI_RESET);
    }

    public static void printYellow(String in) {
        System.out.println(ANSI_YELLOW + in + ANSI_RESET);
    }

    public static void printBlue(String in) {
        System.out.println(ANSI_BLUE + in + ANSI_RESET);
    }

    public static void printPurple(String in) {
        System.out.println(ANSI_PURPLE + in + ANSI_RESET);
    }

    public static void printCyan(String in) {
        System.out.println(ANSI_CYAN + in + ANSI_RESET);
    }

    public static void printWhite(String in) {
        System.out.println(ANSI_WHITE + in + ANSI_RESET);
    }
}
