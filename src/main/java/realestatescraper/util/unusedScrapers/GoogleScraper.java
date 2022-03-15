package realestatescraper.util.unusedScrapers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j // only shows info, warn, error
@AllArgsConstructor
public class GoogleScraper {

    private static final String URL = "https://www.google.com/";

    private static final int NUM_PAGES = 3;
    private static final int PAUSE = 0; //increase when using VPN -> page loads slower

    private static final ArrayList<String> RESULT_LIST = new ArrayList<>();
    private static final ArrayList<String> DOMAIN_LIST = new ArrayList<>();

    private final ChromeDriver driver;

    public void scrape(String seachQuery) throws InterruptedException {
        log.info("Search query: " + seachQuery);
        driver.get(URL);
        log.info("Reached " + driver.getTitle());

        // terms and conditions
        driver.findElement(By.id("L2AGLb")).click();

        // search and press enter
        driver.findElement(By.name("q")).sendKeys(seachQuery + Keys.ENTER);

        // iterate through pages
        int counter = 0;
        for (int i = 0; i < NUM_PAGES; i++) {
            Thread.sleep(PAUSE);

            readAnchors();
            counter++;
            try {
                driver.findElement(By.id("pnnext")).click();
            } catch (Exception e) {
                log.info("No more pages");
                break;
            }
        }

        // remove duplicate entries in domains
        log.info("Scraped " + counter + " pages.");

        // print results
        for (String result : RESULT_LIST) {
            System.out.println(result);
        }

        driver.close();
        driver.quit();
    }

    private void readAnchors() {
        List<WebElement> anchors = driver.findElements(By.tagName("a"));

        for (WebElement anchor : anchors) {
            if (anchor.getAttribute("href") != null) {
                String link = anchor.getAttribute("href");
                // check if link is a result and unique
                if (!link.contains("google") && !DOMAIN_LIST.contains(link.split("/")[2])) {
                    DOMAIN_LIST.add(link.split("/")[2]);
                    RESULT_LIST.add(link);
                }
            }
        }

        log.info("Found " + RESULT_LIST.size() + " unique links");
    }

}
