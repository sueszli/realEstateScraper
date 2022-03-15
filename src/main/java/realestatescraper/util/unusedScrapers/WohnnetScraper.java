package realestatescraper.util.unusedScrapers;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import realestatescraper.Preferences;
import realestatescraper.util.CSVHandler;
import realestatescraper.util.DateHandler;
import realestatescraper.util.DistrictConverter;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static realestatescraper.util.ColoredPrint.printGreen;

@Slf4j
@Service
@AllArgsConstructor
public class WohnnetScraper {
    private static final String LINKS_FILENAME = "wohnnetLinks" + DateHandler.getCurrentDate();
    private static final int PAUSE_LINKS = 3000;
    private static final String PRODUCTS_FILENAME = "wohnnetProducts" + DateHandler.getCurrentDate();

    private static String totalonMarket;

    private final ChromeDriver driver;

    public void setPreferences() throws InterruptedException {
        String url = "https://www.wohnnet.at/immobilien/eigentumswohnungen/wien?";

        // districts
        url += "unterregionen=";
//        for (int d : Preferences.DISTRICTS) {
//            url += "--g9" + DistrictConverter.getTwoDigitDistrict(d) + "01";
//        }
        url = url.substring(0, url.length() - 2);

        // price
        url += "&preis=" + Preferences.MIN_PRICE + "-" + Preferences.MAX_PRICE;

        driver.get(url);
        log.info("Reached Wohnnet and set preferences");

        Thread.sleep(2000);
        driver.findElement(By.xpath("//a[@id='CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll']")).click();
        log.info("Agreed to terms and conditions");

        totalonMarket = driver.findElement(By.xpath("//body/section[@id='adjust']/div[1]/div[1]/div[2]/div[1]/div[2]/p[1]")).getText().split(" ")[0];
    }

    public void scrapeLinks() throws InterruptedException {
        ArrayList<String> allLinks = new ArrayList<>();

        while (true) {
            Thread.sleep(PAUSE_LINKS);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

            List<WebElement> as = driver.findElements(By.cssSelector(".btn-realty-details"));
            for (WebElement a : as) {
                try {
                    String link = a.getAttribute("href");
                    if (!allLinks.contains(link)) {
                        allLinks.add(link);
                    }
                } catch (Exception ignored) {
                    log.error("Link could not be extracted from anchor");
                }
            }
            log.info("Scraped [{}/{}] links", allLinks.size(), totalonMarket);

            // go to next page
            try {
                driver.findElement(By.xpath("//i[@class='fas fa-chevron-right']/ancestor::a")).click();
            } catch (Exception e) {
                log.info("Reached last page");
                break;
            }
        }

        allLinks.forEach(l -> CSVHandler.appendCSV(LINKS_FILENAME, l));
        log.info("Copied all links to memory");
    }

    public void scrapeProducts() {
        int current = 1;
        int total = CSVHandler.getSize(LINKS_FILENAME);

        CSVHandler.appendCSV(PRODUCTS_FILENAME,
                new String[]{
                        "URL", "HEADER", "ADDRESS", "PRICE", "LIVING SPACE", "NUM OF ROOMS", "TYPE"
                }
        );

        // scrape products
        try (CSVReader csvr = new CSVReader(new FileReader(CSVHandler.FILE_PATH + LINKS_FILENAME + ".csv"))) {

            String[] nextLine;
            while ((nextLine = csvr.readNext()) != null) {
                ArrayList<String> data = getRow(nextLine[0]);
                log.info("Scraped [{}/{}] of stored links", current++, total);
                CSVHandler.appendCSV(PRODUCTS_FILENAME, data.toArray(String[]::new));
            }

        } catch (IOException | CsvValidationException ignore) {
        }
    }

    private ArrayList<String> getRow(String url) {
        // go to page
        try {
            driver.get(url);
            Thread.sleep(1000);
            driver.findElement(By.cssSelector(".expand-button")).click();
        } catch (Exception e) {
            log.error("Reached invalid URL while scraping products: " + url);
            return new ArrayList<>();
        }
        ArrayList<String> attributes = new ArrayList<>();

        // url
        attributes.add(url);

        // header
        try {
            String a = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/main[1]/div[1]/div[5]/section[1]/div[2]/p[1]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // address 1
        try {
            String a = driver.findElement(By.xpath("//body/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[2]/div[5]/a[1]/div[1]/div[2]")).getText();
            attributes.add(a);
        } catch (Exception e1) {
            // address 2
            try {
                String a = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[2]/div[6]/a[1]/div[1]/div[2]")).getText();
                attributes.add(a);
            } catch (Exception e) {
                attributes.add("NULL");
            }
        }

        // address 2
        try {
            String a = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[2]/div[6]/a[1]/div[1]/div[2]")).getText();
            attributes.add(a);
        } catch (Exception e1) {
            // address 1
            try {
                String a = driver.findElement(By.xpath("//body/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[2]/div[5]/a[1]/div[1]/div[2]")).getText();
                attributes.add(a);
            } catch (Exception e) {
                attributes.add("NULL");
            }
        }

        // price
        try {
            String a = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[1]/div[1]/p[1]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // living space
        try {
            String a = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[2]/div[3]/b[1]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // num of rooms
        try {
            String a = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[2]/div[4]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // type
        try {
            String a = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/main[1]/div[1]/div[1]/section[1]/div[2]/div[2]/span[2]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        return attributes;
    }

    public void displayStats() {
        System.out.println("\n\n\n");
        printGreen("FINAL STATS:");
        System.out.println("Items on the market: " + totalonMarket);
        System.out.println("Scraped links: " + CSVHandler.getSize(LINKS_FILENAME) + " (partially advertisement)");
        System.out.println("Scraped products: " + (CSVHandler.getSize(PRODUCTS_FILENAME) - 1));
        System.out.println("\n\n\n");
    }

    public static ArrayList<String[]> getProductsArrayList() throws IOException {
        // split lines and return arraylist
        return Files.lines(Paths.get(CSVHandler.FILE_PATH + PRODUCTS_FILENAME + ".csv"))
                .map(e -> e.split(";")).collect(Collectors.toCollection(ArrayList::new));
    }
}
