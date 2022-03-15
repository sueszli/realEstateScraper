package realestatescraper.util.codeSnippets;

import io.github.sukgu.Shadow;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import realestatescraper.Preferences;
import realestatescraper.util.DateHandler;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ShadowRoot {

    private static final String LINKS_FILENAME = "immoweltLinks" + DateHandler.getCurrentDate();
    private static final int PAUSE_LINKS = 2500;
    private static final String PRODUCTS_FILENAME = "immoweltProducts" + DateHandler.getCurrentDate();

    private static String totalonMarket;

    private final ChromeDriver driver;

    /**
     * FAILED PROJECT -> PAGE DOESN'T LOAD
     * DONT DELETE - CONTAINS SOLUTION TO USING SHADOW ROOTS
     */
    @PostConstruct
    private void init() throws InterruptedException {
        //setPreferences();
        //scrapeLinks();
    }


    public void setPreferences() throws InterruptedException {
        String url = "https://www.immowelt.at/liste/wien/wohnungen/kaufen?";
        url += "primi=" + Preferences.MIN_PRICE;
        url += "&prima=" + Preferences.MAX_PRICE;

        driver.get(url);
        log.info("Reached Immowelt");
        log.info("Set price preferences");

        Shadow shadow = new Shadow(driver);
        Thread.sleep(2000);
        shadow.findElementByXPath("//button[@data-testid='uc-accept-all-button']").click();
        log.info("Accepted terms and conditions");

//        for (int d : Preferences.DISTRICTS) {
//            Thread.sleep(1000);
//            driver.findElement(By.cssSelector("#addLocation")).click();
//            driver.findElement(By.cssSelector(".locationinput")).sendKeys(String.valueOf(d) + Keys.ENTER);
//        }
        log.info("Set district preferences");


        totalonMarket = driver.findElement(By.xpath("//h1")).getText().split(" ")[0];
    }

    public void scrapeLinks() throws InterruptedException {
        ArrayList<String> allLinks = new ArrayList<>();

        while (true) {
            Thread.sleep(PAUSE_LINKS);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

            List<WebElement> as = driver.findElements(By.xpath("//a[starts-with(@href, '/expose/')]"));
            for (WebElement a : as) {
                try {
                    String link = a.getAttribute("href");
                    if (link.contains("?")) {
                        link = link.split("\\?")[0];
                    }
                    if (!allLinks.contains(link)) {
                        allLinks.add(link);
                    }
                } catch (Exception ignored) {
                }
            }
            log.info("Scraped [{}/{}] links", allLinks.size(), totalonMarket);

            // go to next page
            try {
                driver.findElement(By.cssSelector("#nlbPlus")).click();
            } catch (Exception e) {
                log.info("Reached last page");
                break;
            }
        }

        // allLinks.forEach(l -> CSVHandler.appendCSV(LINKS_FILENAME, l));
        allLinks.forEach(System.out::println);
        log.info("Copied all links to memory");
        driver.close();
    }
}
