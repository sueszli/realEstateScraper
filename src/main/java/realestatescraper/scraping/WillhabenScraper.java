package realestatescraper.scraping;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import realestatescraper.Preferences;
import realestatescraper.util.CSVHandler;
import realestatescraper.util.DateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static realestatescraper.util.ColoredPrint.printGreen;

@Slf4j
@Service
@AllArgsConstructor
public class WillhabenScraper {

    private static final ArrayList<String> LINKS = new ArrayList<>();
    private static final String PRODUCTS_FILENAME = "willhabenProducts" + DateHandler.getCurrentDate();

    private static String totalonMarket;

    private final ChromeDriver driver;

    public void scrape() {
        setPreferences();
        scrapeLinks();
        scrapeProducts();

        System.out.println("\n\n\n");
        printGreen("FINAL STATS:");
        System.out.println("Items on the market: " + totalonMarket);
        System.out.println("Scraped links: " + LINKS.size());
        System.out.println("Scraped products: " + (CSVHandler.getSize(PRODUCTS_FILENAME)-1));
        System.out.println("\n\n\n");
    }





    private void setPreferences() {
        String url = "https://www.willhaben.at/iad/immobilien/eigentumswohnung/eigentumswohnung-angebote?";
        url += "&rows=5";

        url += "&PRICE_FROM=" + Preferences.MIN_PRICE;
        url += "&PRICE_TO=" + Preferences.MAX_PRICE;
        url += "&areaId=900"; // vienna
        for (String d : Preferences.DISTRICTS) {
            url += "&areaId=" + d;
        }

        driver.get(url);
        log.info("Reached WillhabenScraper with given preferences");

        driver.findElement(By.cssSelector("button#didomi-notice-agree-button")).click();
        log.info("Accepted terms and conditions");

        totalonMarket = driver.findElement(By.xpath("//h1[@data-testid='result-list-title']")).getText().split(" ")[0];
        log.info("There are currently {} items on the market.", totalonMarket);
    }





    private void scrapeLinks() {
        while (true) {
            // timed exactly so that 5 anchors load
            for (int i = 0; i < 3; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            }

            // fetch anchors
            List<WebElement> as = driver.findElements(By.xpath("//a[starts-with(@href,'/iad/immobilien/d/eigentumswohnung/wien/')]"));
            for (WebElement a : as) {
                LINKS.add(a.getAttribute("href"));
            }
            log.info("Scraped [{}/{}] of links", LINKS.size(), totalonMarket);

            try {
                String nextPage = driver
                        .findElement(By.xpath("//a[@data-testid='pagination-top-next-button']"))
                        .getAttribute("href");
                driver.get(nextPage);
            } catch (NoSuchElementException | NullPointerException e) {
                log.info("Reached last page");
                break;
            }
        }
    }





    private void scrapeProducts() {

        // set header
        CSVHandler.appendCSV(PRODUCTS_FILENAME,
                new String[]{
                        "URL",
                        "DISTRICT",
                        "HEADER",
                        "ADDRESS",
                        "LIVING SPACE IN m²",
                        "NUM OF ROOMS",
                        "TYPE 1",
                        "TYPE 2",
                        "ALTBAU VS NEUBAU",
                        "LAST CHANGE OF PAGE",
                        "PRICE",
                        "BROKER COMISSION",
                        "ADDITIONAL INFORMATION",
                        "SELLER NAME",
                        "PHONE NUMBER 1",
                        "PHONE NUMBER 2",
                }
        );

        AtomicInteger current = new AtomicInteger(1);
        LINKS.forEach( link -> {
            ArrayList<String> data = getRow(link);
            log.info("Scraped [{}/{}] of stored links - {}", current.getAndIncrement(), LINKS.size(), data);
            CSVHandler.appendCSV(PRODUCTS_FILENAME, data.toArray(String[]::new));
        });
    }

    private ArrayList<String> getRow(String url) {
        // go to page
        try {
            driver.get(url);
        } catch (Exception e) {
            log.error("Reached invalid URL while scraping products: " + url);
            return new ArrayList<>();
        }
        ArrayList<String> attributes = new ArrayList<>();

        // url
        attributes.add(url);

        // district
        attributes.add(url.substring(69, 69 + 4));

        // header
        try {
            String a = driver.findElement(By.xpath("//h1[@data-testid='ad-detail-header']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // address
        try {
            String a = driver.findElement(By.xpath("//div[@data-testid='object-location-address']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // living space in m²
        try {
            String a = driver.findElement(By.xpath("//div[@data-testid='ad-detail-teaser-attribute-0']/span")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // number of rooms
        try {
            String a = driver.findElement(By.xpath("//div[@data-testid='ad-detail-teaser-attribute-1']/span")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // type 1
        try {
            String a = driver.findElement(By.xpath("(//div[@data-testid='ad-detail-teaser-attribute-2'])[1]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // type 2
        try {
            String a = driver.findElement(By.xpath("(//div[@data-testid='attribute-value'])[1]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // Altbau vs. Neubau
        try {
            String a = driver.findElement(By.xpath("(//div[@data-testid='attribute-value'])[2]")).getText();
            if (a.equals("Neubau") || a.equals("Altbau")) {
                attributes.add(a);
            } else {
                attributes.add("NULL");
            }
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // last change
        try {
            String a = driver.findElement(By.xpath("//span[@data-testid='ad-detail-ad-edit-date-top']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // price
        try {
            String a = driver.findElement(By.xpath("//span[@data-testid='contact-box-price-box-price-value-0']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // broker commission
        try {
            String a = driver.findElement(By.xpath("//span[@data-testid='price-information-freetext-attribute-value-0']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // additional information
        try {
            String a = driver.findElement(By.xpath("//span[@data-testid='price-information-freetext-attribute-value-0']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // contact: company / person
        try {
            String a = driver.findElement(By.xpath("//span[@data-testid='top-contact-box-seller-name']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // contact: phone number
        try {
            driver.findElement(By.xpath("//button[@data-testid='top-contact-box-phone-number-button']")).click();
            List<WebElement> as = driver.findElements(By.xpath("//div[@data-testid='top-contact-box-phone-number-box']/div/a"));
            for (WebElement a : as) {
                attributes.add(a.getText());
            }
            for (int i = 0; i < 2 - as.size(); i++) {
                attributes.add("NULL");
            }
        } catch (Exception e) {
            for (int i = 0; i < 2; i++) {
                attributes.add("NULL");
            }
        }

        return attributes;
    }
}
