package com.Integral.Automation_Assignment;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Task1 {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        // Set the path to the chromedriver executable
        System.setProperty("webdriver.chrome.driver", "WebDriver/chromedriver-win64/chromedriver.exe");

        // Initialize the Chrome driver
        driver = new ChromeDriver();

        // Initialize WebDriverWait with a timeout of 10 seconds
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @DataProvider(name = "subscriptionData")
    public Object[][] subscriptionData() {
        return new Object[][] {
            {"en", "sa", "SAR", new double[]{15.0, 25.0, 60.0}},
            {"en", "kw", "KWD", new double[]{1.2, 2.5, 4.8}},
            {"en", "bh", "BHD", new double[]{2.0, 3.0, 6.0}},
            {"ar", "sa", "ريال سعودي", new double[]{15.0, 25.0, 60.0}},
            {"ar", "kw", "دينار كويتي", new double[]{1.2, 2.5, 4.8}},
            {"ar", "bh", "دينار بحريني", new double[]{2.0, 3.0, 6.0}}
        };
    }

    @Test(dataProvider = "subscriptionData")
    public void validateSubscriptionPackages(String language, String country, String expectedCurrency, double[] expectedPrices) {
        // Log test start
        Reporter.log("Starting test for language: " + language + ", country: " + country + ", expected currency: " + expectedCurrency, true);

        // Open the webpage
        driver.get("https://subscribe.stctv.com/sa-en");

        // Switch language if necessary
        if (language.equals("ar")) {
            WebElement languageSwitcher = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#translation-btn")));
            languageSwitcher.click();
            // Wait for the page to reload after language switch
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2.mobile-hidden")));
        }

        // Verify if the "Jawwy TV" link element exists and is visible
        WebElement jawwyTVLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#jawwy-logo-web")));
        Assert.assertTrue(jawwyTVLink.isDisplayed(), "Jawwy TV link is not displayed");

        // Verify if the "Choose Your Plan" element exists and is visible
        WebElement chooseYourPlan = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2.mobile-hidden")));
        Assert.assertTrue(chooseYourPlan.isDisplayed(), "Choose Your Plan element is not displayed");

        // Select the country
        WebElement countrySelector = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#country-btn")));
        countrySelector.click();
        WebElement countryOption = driver.findElement(By.cssSelector("#" + country));
        countryOption.click();

        // Wait for the page to load and the country selection to be applied
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#country-btn")));

        // Verify the subscription packages
        verifyPackages(language, expectedCurrency, expectedPrices);
    }

    public void verifyPackages(String language, String expectedCurrency, double[] expectedPrices) {
        String[] packageNamesEn = {"lite", "classic", "premium"};
        String[] packageNamesAr = {"لايت", "الأساسية", "بريميوم"};
        String[] packageNames = language.equals("ar") ? packageNamesAr : packageNamesEn;

        for (int i = 0; i < packageNames.length; i++) {
            String packageName = packageNames[i];
            double expectedPrice = expectedPrices[i];

            // Construct the package name selector based on language
            String packageNameSelector = language.equals("ar") ? "#name-" + packageName : "#name-" + packageName;

            // Find the package element name
            WebElement packageElementName = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(packageNameSelector)));
            String packageElementNameText = packageElementName.getText().toLowerCase();

            // Find the package element currency
            WebElement packageElementCurrency = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='currency-" + packageName + "']/i")));
            String packagePriceText = packageElementCurrency.getText();
            WebElement packagePriceNumber = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='currency-" + packageName + "']/b")));

            Double packagePriceNumberText = Double.parseDouble(packagePriceNumber.getText());

            // Log the package details
            Reporter.log("Package: " + packageName + " | Name: " + packageElementNameText + " | Price: " + packagePriceNumberText + " | Price Currency: " + packagePriceText, true);

            // Assertions: verify the package name and currency
            Assert.assertTrue(packageElementNameText.contains(packageName), packageName + " package name is not correct. Expected: " + packageName + ", Found: " + packageElementNameText);
            Assert.assertTrue(packagePriceText.contains(expectedCurrency), packageName + " package currency is not correct. Expected: " + expectedCurrency + ", Found: " + packagePriceText);

            // Assertions: verify the package price
            Assert.assertEquals(packagePriceNumberText, expectedPrice, 0.01, packageName + " package price is not correct. Expected: " + expectedPrice + ", Found: " + packagePriceNumberText);
        }
    }

    @AfterClass
    public void tearDown() {
        // Log test completion
        Reporter.log("Test completed", true);

        // Close the browser
        driver.quit();
    }
}
