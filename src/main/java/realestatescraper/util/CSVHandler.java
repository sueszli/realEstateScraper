package realestatescraper.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static realestatescraper.util.ColoredPrint.printRed;

@Slf4j
public class CSVHandler {
    // see: http://opencsv.sourceforge.net/#writing

    public static final String FILE_PATH = "data/";

    public static void deleteFile(String fileName) {
        if (new File(FILE_PATH + fileName + ".csv").delete()) {
            printRed("WARNING: " + fileName + ".csv was deleted successfully.");
        }
    }

    public static void createCSV(String fileName, List<String[]> data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", false),
                ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            csvw.writeAll(data);
        } catch (IOException ignore) {
        }
    }

    public static void createCSVWithHeader(String fileName, String[] header, List<String[]> data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", false),
                ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            csvw.writeNext(header);
            csvw.writeAll(data);
        } catch (IOException ignore) {
        }
    }

    public static void appendCSV(String fileName, String[] data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", true),
                ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            csvw.writeNext(data);
        } catch (IOException ignore) {
        }
    }

    public static void appendCSV(String fileName, String data) {
        try (CSVWriter csvw = new CSVWriter(new FileWriter(FILE_PATH + fileName + ".csv", true),
                ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            csvw.writeNext(new String[]{data});
        } catch (IOException ignore) {
        }
    }

    public static int getSize(String fileName) {
        try {
            return (int) Files.lines(Paths.get(CSVHandler.FILE_PATH + fileName + ".csv")).count();
        } catch (IOException ignore) {
            log.error("Size of " + fileName + " could not be read");
            return 0;
        }
    }

}
