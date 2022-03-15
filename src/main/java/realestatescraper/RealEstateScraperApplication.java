package realestatescraper;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import realestatescraper.scraping.StandardScraper;
import realestatescraper.scraping.WillhabenScraper;
import realestatescraper.util.unusedScrapers.WohnnetScraper;
import realestatescraper.util.unusedScrapers.GoogleScraper;

import javax.annotation.PostConstruct;

@AllArgsConstructor
@SpringBootApplication
public class RealEstateScraperApplication {

    private StandardScraper standardScraper;
    private WillhabenScraper willhabenScaper;

    public static void main(String[] args) {
        SpringApplication.run(RealEstateScraperApplication.class, args);
    }

    @PostConstruct
    private void init() throws InterruptedException {
        standardScraper.scrape();
        // willhabenScaper.scrape();
    }
}
