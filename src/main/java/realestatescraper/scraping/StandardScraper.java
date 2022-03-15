package realestatescraper.scraping;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
public class StandardScraper {
    private static final ArrayList<String> LINKS = new ArrayList<>();
    private static final int PAUSE_LINKS = 1250;
    private static final String PRODUCTS_FILENAME = "standardProducts" + DateHandler.getCurrentDate();
    private static String totalonMarket;

    private final ChromeDriver driver;

    public void scrape() throws InterruptedException {
        setPreferences();
        scrapeLinks();
        scrapeProducts();

        System.out.println("\n\n\n");
        printGreen("FINAL STATS:");
        System.out.println("Items on the market: " + totalonMarket);
        System.out.println("Scraped links: " + LINKS.size() + " (partially advertisement)");
        System.out.println("Scraped products: " + (CSVHandler.getSize(PRODUCTS_FILENAME) - 1));
        System.out.println("\n\n\n");
    }





    private void setPreferences() throws InterruptedException {
        String url = "https://immobilien.derstandard.at/immobiliensuche/i/kaufen/wohnung/wien";
        driver.get(url);
        log.info("Reached StandardScraper");

        driver.findElement(By.xpath("//div[@class='ewMoreOptions immosearch-type more-options-btn']/span")).click();
        driver.findElement(By.xpath("//input[@id='Price_MinValue']")).sendKeys(Preferences.MIN_PRICE);
        driver.findElement(By.xpath("//input[@id='Price_MaxValue']")).sendKeys(Preferences.MAX_PRICE + Keys.ENTER);

        driver.findElement(By.xpath("(//footer/div/div)[2]/button")).click();
        log.info("Agreed to terms and conditions");

        Thread.sleep(1000);
        totalonMarket = driver.findElement(By.xpath("//h1[@class='headerImmoSearch']")).getText().split(" ")[0];
        log.info("There are currently {} items on the market", totalonMarket);
    }





    private void scrapeLinks() throws InterruptedException {
        while (true) {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(PAUSE_LINKS);

            List<WebElement> as = driver.findElements(By.xpath("//li[contains(@class, 'box resultitem clearfix')]//h2/a"));
            for (WebElement a : as) {
                try {
                    String link = a.getAttribute("href");
                    if (!LINKS.contains(link)) {
                        LINKS.add(link);
                    }
                } catch (Exception ignored) {
                    log.error("Link could not be extracted from anchor");
                }
            }
            log.info("Scraped [{}/{}] links", LINKS.size(), totalonMarket);

            // go to next page
            try {
                driver.findElement(By.xpath("(//ul[@id='immoSearchPagingSection']/li)[3]/a")).click();
            } catch (Exception e) {
                log.info("Reached last page");
                break;
            }
        }
    }





    private void scrapeProducts() {
        // HEADER
        CSVHandler.appendCSV(PRODUCTS_FILENAME,
                new String[]{
                        "URL",
                        "TITLE",
                        "DISTRICT",
                        "TYPE",
                        "LIVING SPACE",
                        "NUM OF ROOMS",
                        "PRICE",
                        "ATTRIBUTES",
                        "BROKERS COMMISION",
                        "SELLER NAME",
                        "SELLING COMPANY",
                        "PHONE NUMBER 1",
                        "PHONE NUMBER 2",
                }
        );

        // scrape products
        AtomicInteger current = new AtomicInteger(1);
        LINKS.forEach(link -> {
            ArrayList<String> temp = getRow(link);
            ArrayList<String> data = new ArrayList<>();

            // remove line breaks
            temp.forEach(elem -> {
                if (elem.contains("\n")) {
                    data.add(elem.replace("\n", ""));
                }
                data.add(elem);
            });

            log.info("Scraped [{}/{}] of stored links", current.getAndIncrement(), LINKS.size());
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

        // title
        try {
            String a = driver.findElement(By.xpath("//div[@class='immoDetailHeader']/h1")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // district
        try {
            String a = driver.findElement(By.xpath("/html[1]/body[1]/main[1]/div[1]/div[2]/div[3]/h2[1]/span[1]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // type
        try {
            String a = driver.findElement(By.xpath("(//h2[@class='metainfo']/span)[2]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // living space
        try {
            String a = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix']/li/span)[1]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // num of rooms
        try {
            String a = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix']/li/span)[2]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // price
        try {
            String a = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix']/li/span)[3]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // attributes
        try {
            List<WebElement> asElems = driver.findElements(By.xpath("(//div[@class='inner'])[1]/span"));
            String a = "";
            for (WebElement aElem : asElems) {
                a += aElem.getText() + " ";
            }
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // broker commission
        try {
            String a = driver.findElement(By.xpath("(//ul[@class='costAndAreaList']/li)[last()]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // seller name
        try {
            String a = driver.findElement(By.xpath("//div[@class='headerTwo']")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // company
        try {
            String a = driver.findElement(By.xpath("(//div[@class='contact-image-container']/div)[5]")).getText();
            attributes.add(a);
        } catch (Exception e) {
            attributes.add("NULL");
        }

        // phone number
        try {
            driver.findElement(By.xpath("//a[@class='show-number-btn']")).click();
            List<WebElement> nums = driver.findElements(By.xpath("//div[@class='headerThree']"));
            for (WebElement n : nums) {
                attributes.add(n.getText());
            }
            for (int i = 0; i < 2 - nums.size(); i++) {
                attributes.add("NULL");
            }
        } catch (Exception ignored) {
            for (int i = 0; i < 2; i++) {
                attributes.add("NULL");
            }
        }

        return attributes;
    }
}
