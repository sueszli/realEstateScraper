package realestatescraper.util.unusedScrapers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import realestatescraper.util.CSVHandler;
import realestatescraper.util.DateHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StandardNeubauScraper {
    private static final String NEUBAUPRODUCTS_FILENAME = "standardNeubauProducts" + DateHandler.getCurrentDate();
    private static final int PAUSE_NEUBAU = 750;
    private static String totalonMarket;

    private final ChromeDriver driver;


    public void getNeubau() throws InterruptedException {
        String url = "https://immobilien.derstandard.at/immobiliensuche/i/neubau/wien";
        driver.get(url);
        log.info("Reached StandardScraper");

        driver.findElement(By.xpath("(//footer/div/div)[2]/button")).click();
        log.info("Agreed to terms and conditions");

        String total = driver.findElement(By.xpath("//h1[@class='headerProjectSearch']")).getText();
        log.info("Total of Neubau items on market: " + total.split(" ")[0]);


        // scrape links
        ArrayList<String> allLinks = new ArrayList<>();

        while (true) {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(PAUSE_NEUBAU);

            // get meta-data and links from results
            List<WebElement> containers = driver.findElements(By.xpath("//div[@class='result-data-container'][.//a[contains(@href,'https://immobilien')]]"));
            for (WebElement c : containers) {
                try {
                    String link = c.findElement(By.xpath(".//a")).getAttribute("href");
                    List<WebElement> ms = c.findElements(By.xpath(".//div[@class='result-data-cont']/span[@class='result-data']/span"));
                    String meta = ms.get(2).getText();

                    if (meta.equals("Kaufpreis") && !allLinks.contains(link)) {
                        allLinks.add(link);
                    }
                } catch (Exception ignored) {
                    log.error("A result could not be read");
                }
            }

            // go to next page
            try {
                driver.findElement(By.xpath("(//ul[@id='immoSearchProjectsPagingSection']/li)[3]/a")).click();
            } catch (Exception e) {
                log.info("Reached last page");
                break;
            }
        }
        log.info("Scraped {} Neubau-links of the type 'Kaufen'", allLinks.size());

        CSVHandler.appendCSV(NEUBAUPRODUCTS_FILENAME, new String[]{
                "URL", "AVAILABLE APPARTMENTS UNITS", "AREA IN M²", "NUM OF ROOMS", "PRICE RANGE", "DATE OF CONSTRUCTION", "ADDRESS", "ATTRIBUTES"
        });

        // scrape products
        int counter = 0;
        int totalSize = allLinks.size();
        for (String link : allLinks) {
            driver.get(link);

            String available = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix box box-buttons']/li/span)[1]")).getText();
            String squareMeters = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix box box-buttons']/li/span)[2]")).getText();
            String rooms = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix box box-buttons']/li/span)[3]")).getText();
            String price = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix box box-buttons']/li/span)[4]")).getText();
            String construction = driver.findElement(By.xpath("(//ul[@class='quick-details clearfix box box-buttons']/li/span)[5]")).getText();
            String address = driver.findElement(By.xpath("(//ul[@class='']/li/span)[1]")).getText();
            String attributes = "";
            List<WebElement> as = driver.findElements(By.xpath("//div[@class='box immoProjectAusstattung clearfix']/ul/li"));
            for (WebElement a : as) {
                attributes += a.getText() + " ";
            }

            String[] row = new String[]{
                    link, available, squareMeters, rooms, price, construction, address, attributes
            };
            CSVHandler.appendCSV(NEUBAUPRODUCTS_FILENAME, row);
            log.info("Read [{}/{}] of Neubau items - {}", ++counter, totalSize, row);

        }
    }
}
