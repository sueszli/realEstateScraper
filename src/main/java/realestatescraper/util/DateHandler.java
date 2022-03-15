package realestatescraper.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateHandler {

    public static String getCurrentDate() {
        // get current date
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDateTime now = LocalDateTime.now();
        return "[" + dtf.format(now) + "]";
    }
}