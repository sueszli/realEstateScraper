package realestatescraper.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class SeleniumConfig {

    @PostConstruct
    void postConstruct() {
        WebDriverManager.chromedriver().setup();
    }

    // Only a single driver instance to improve performance
    @Bean
    public ChromeDriver driver() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); -> causes bugs
        options.addArguments("start-maximized");
        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions");

        return new ChromeDriver(options);
    }
}
