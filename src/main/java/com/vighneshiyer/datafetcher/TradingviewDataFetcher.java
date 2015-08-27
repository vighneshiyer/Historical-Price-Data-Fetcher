package com.vighneshiyer.datafetcher;


import com.google.common.base.Predicate;
import com.vighneshiyer.OHLC;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TradingviewDataFetcher {
    private final WebDriver driver;

    public TradingviewDataFetcher() {
        String chromeDriverPath = null;
        try {
            chromeDriverPath = new File(TradingviewDataFetcher.class.getResource("/chromedriver_win32/chromedriver.exe").toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert chromeDriverPath != null;
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    public synchronized Map<Instant, OHLC> getAllHistoricalDataForSymbol(String symbol, int resolution) { // FX:XAUUSD
        driver.get("https://www.tradingview.com/chart/?symbol=" + symbol);
        System.out.println("Loaded webpage");
        // Close any introduction boxes on the chart
        (new WebDriverWait(driver, 20000)).until(
                ExpectedConditions.visibilityOfElementLocated(By.className("wizard-tooltip")));
        driver.findElement(By.className("wizard-tooltip-stop")).click();
        if (driver.findElements(By.className("onchart-message-close")).size() > 0) {
            driver.findElement(By.className("onchart-message-close")).click();
        }
        System.out.println("Closed intro boxes");

        // Add the plot(time, 'time') line of code in the editor
        driver.findElement(By.cssSelector("span.bottom-toolbar-tab:nth-child(2) span.title")).click();
        (new WebDriverWait(driver, 3000)).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.ace_layer.ace_text-layer .ace_line:nth-child(2)")));

        WebElement editorContent = driver.findElement(By.cssSelector("div.ace_content"));
        WebElement editorTextarea = driver.findElement(By.cssSelector("textarea.ace_text-input"));
        Action clickOnEditor = new Actions(driver).moveToElement(editorContent,
                editorContent.getSize().getWidth() / 2, editorContent.getSize().getHeight() / 2).click().build();
        clickOnEditor.perform();
        for (int i = 0; i < 6; i++) {
            editorTextarea.sendKeys(Keys.BACK_SPACE);
        }
        editorTextarea.sendKeys("time)");

        // Add the study to the chart
        driver.findElement(By.cssSelector(".button.tv-script-add-button")).click();

        // Close the pine editor window
        driver.findElement(By.cssSelector("span.bottom-toolbar-tab:nth-child(2) span.title")).click();

        (new WebDriverWait(driver, 1000)).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                List<WebElement> dataContainers = webDriver.findElements(
                        By.cssSelector(".pane-legend-item-value-container .pane-legend-item-value"));
                return dataContainers.size() == 7 && !dataContainers.get(6).getText().equals("n/a");
            }
        });

        WebElement mainCanvas = driver.findElement(By.cssSelector(
                "table.chart-markup-table tr:nth-child(1) td:nth-child(2) canvas:nth-child(2)"));

        synchronized (driver) {
            try {
                driver.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Action moveToEdge = new Actions(driver)
                .moveToElement(mainCanvas)
                .build();
        moveToEdge.perform();

        Action moveLeftOneStep = new Actions(driver).sendKeys(mainCanvas, Keys.ARROW_LEFT).build();
        Action jiggle = new Actions(driver).moveByOffset(0, -10).moveByOffset(0, 10).build();

        List<WebElement> priceIndicators = driver.findElements(
                By.cssSelector(".pane-legend-item-value-container .pane-legend-item-value"));
        WebElement openPrice = priceIndicators.get(0);
        WebElement highPrice = priceIndicators.get(1);
        WebElement lowPrice = priceIndicators.get(2);
        WebElement closePrice = priceIndicators.get(3);
        WebElement time = priceIndicators.get(6);

        Map<Instant, OHLC> priceData = new LinkedHashMap<>();
        for (int i = 0; i < 500; i++) {
            moveLeftOneStep.perform();
            jiggle.perform();
            try {
                priceData.put(Instant.ofEpochSecond((long)Double.parseDouble(time.getText())),
                        new OHLC(Double.parseDouble(openPrice.getText()),
                                Double.parseDouble(highPrice.getText()),
                                Double.parseDouble(lowPrice.getText()),
                                Double.parseDouble(closePrice.getText())
                        ));
            } catch (NumberFormatException|StaleElementReferenceException e) {
                // Recover by resetting the WebElements that are stale
                priceIndicators = driver.findElements(
                        By.cssSelector(".pane-legend-item-value-container .pane-legend-item-value"));
                openPrice = priceIndicators.get(0);
                highPrice = priceIndicators.get(1);
                lowPrice = priceIndicators.get(2);
                closePrice = priceIndicators.get(3);
                time = priceIndicators.get(6);
            }
        }
        return priceData;
    }

    @Override
    public void finalize() throws Throwable {
        driver.quit();
        super.finalize();
    }
}
