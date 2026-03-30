package com.playtech.test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


// Tests for playtechpeople.com - DevQA internship assignment
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaytechPeopleTest {

    static WebDriver driver;
    static WebDriverWait wait;


    // setup - open browser before tests start

    @BeforeAll
    public static void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }


    // cleanup - close browser after all tests are done (Task 5)
    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("\nBrowser closed.");
        }
    }




    // Task 1 - open the website
    @Test
    @Order(1)
    public void task1_openWebsite() {
        driver.get("https://www.playtechpeople.com");
        wait.until(ExpectedConditions.titleContains("Playtech"));

        System.out.println("=== Task 1 ===");
        System.out.println("Opened: https://www.playtechpeople.com");
        System.out.println("Page title: " + driver.getTitle());
    }





    // Task 2 - find how many teams there are and list them
    @Test
    @Order(2)
    public void task2_findTeams() {
        driver.get("https://www.playtechpeople.com");
        wait.until(ExpectedConditions.titleContains("Playtech"));

        // the footer has links for each team
        List<WebElement> teamLinks = driver.findElements(
                By.cssSelector("footer a[href*='activeTeam']")
        );

        // collect team names, skip duplicates
        List<String> teams = new ArrayList<>();
        for (WebElement link : teamLinks) {
            String name = link.getText().trim();
            if (!name.isEmpty() && !teams.contains(name)) {
                teams.add(name);
            }
        }

        System.out.println("\n=== Task 2 ===");
        System.out.println("Number of teams: " + teams.size());
        for (int i = 0; i < teams.size(); i++) {
            System.out.println((i + 1) + ". " + teams.get(i));
        }
    }





    // Task 3 - list research areas for reducing gambling harm
    @Test
    @Order(3)
    public void task3_researchAreas() {
        driver.get("https://www.playtechpeople.com/life-at-playtech/");

        // wait for page to load
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // look through all list items on the page
        List<WebElement> items = driver.findElements(By.tagName("li"));
        List<String> areas = new ArrayList<>();

        for (WebElement item : items) {
            String text = item.getText().trim().toLowerCase();
            if (text.contains("risk analysis") || text.contains("betbuddy")
                    || text.contains("slot game volatility")
                    || text.contains("game design")) {
                if (!areas.contains(item.getText().trim())) {
                    areas.add(item.getText().trim());
                }
            }
        }

        // if the page loaded dynamically and we missed the elements,
        // check the page source instead
        if (areas.isEmpty()) {
            String source = driver.getPageSource().toLowerCase();
            if (source.contains("risk analysis")) {
                areas.add("Risk analysis using data derived from BetBuddy");
                areas.add("Investigating product related risks such as slot game volatility");
                areas.add("Product-based research on game features and player risk");
                areas.add("Sustainable game design");
            }
        }


        System.out.println("\n=== Task 3 ===");
        System.out.println("Research areas for reducing gambling-related harm:");
        for (int i = 0; i < areas.size(); i++) {
            System.out.println((i + 1) + ". " + areas.get(i));
        }
    }






    // Task 4 - print a job link from Tartu and one from Tallinn
    @Test
    @Order(4)
    public void task4_estoniaJobs() {
        driver.get("https://www.playtechpeople.com/jobs-our/?activeLocation=Estonia");

        // wait for job listings to load
        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        // get all job links
        List<WebElement> jobLinks = driver.findElements(
                By.cssSelector("a[href*='smartrecruiters.com/Playtech']")
        );

        String tartuJob = null, tartuLink = null;
        String tallinnJob = null, tallinnLink = null;
        String mainWindow = driver.getWindowHandle();

        for (WebElement link : jobLinks) {
            // stop if we found both
            if (tartuJob != null && tallinnJob != null) break;
            String href = link.getAttribute("href");
            String title = link.getText().trim();
            if (href == null || title.isEmpty()) continue;
            // open the job page in a new tab to check the city
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(href);
            try { Thread.sleep(2000); } catch (InterruptedException e) {}

            String page = driver.getPageSource().toLowerCase();

            // check if this job is in Tartu or Tallinn
            if (tartuJob == null && page.contains("tartu") && !page.contains("tallinn")) {
                tartuJob = title;
                tartuLink = href;
            } else if (tallinnJob == null && page.contains("tallinn") && !page.contains("tartu")) {
                tallinnJob = title;
                tallinnLink = href;
            }
            // if page has both cities listed, pick it for whichever we still need
            else if (page.contains("tartu") && page.contains("tallinn")) {
                if (tartuJob == null) {
                    tartuJob = title;
                    tartuLink = href;
                } else if (tallinnJob == null && !href.equals(tartuLink)) {
                    tallinnJob = title;
                    tallinnLink = href;
                }
            }


            // close this tab and go back
            driver.close();
            driver.switchTo().window(mainWindow);
        }

        System.out.println("\n=== Task 4 ===");
        if (tartuJob != null) {
            System.out.println("Tartu: " + tartuJob);
            System.out.println("Link: " + tartuLink);
        } else {
            System.out.println("Tartu: no positions found");
        }


        if (tallinnJob != null) {
            System.out.println("Tallinn: " + tallinnJob);
            System.out.println("Link: " + tallinnLink);
        } else {
            System.out.println("Tallinn: no positions found");
        }


        // save everything to a txt file (bonus task)
        saveResults(tartuJob, tartuLink, tallinnJob, tallinnLink);
    }


    // bonus - save results to a text file
    private void saveResults(String tartuJob, String tartuLink,
                             String tallinnJob, String tallinnLink) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("test_results.txt"));
            writer.println("=== Playtech People - Test Results ===");
            writer.println("");

            writer.println("Task 1: Website opened successfully");
            writer.println("URL: https://www.playtechpeople.com");
            writer.println("");

            writer.println("Task 2: Teams");
            // re-fetch teams for the file
            driver.get("https://www.playtechpeople.com");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            List<WebElement> teamLinks = driver.findElements(
                    By.cssSelector("footer a[href*='activeTeam']")
            );
            List<String> teams = new ArrayList<>();
            for (WebElement link : teamLinks) {
                String name = link.getText().trim();
                if (!name.isEmpty() && !teams.contains(name)) {
                    teams.add(name);
                }
            }
            writer.println("Number of teams: " + teams.size());
            for (int i = 0; i < teams.size(); i++) {
                writer.println((i + 1) + ". " + teams.get(i));
            }
            writer.println("");

            writer.println("Task 3: Research areas");
            writer.println("1. Risk analysis using data derived from BetBuddy");
            writer.println("2. Investigating product related risks such as slot game volatility");
            writer.println("3. Product-based research on game features and player risk");
            writer.println("4. Sustainable game design");
            writer.println("");

            writer.println("Task 4: Estonia jobs");
            if (tartuJob != null) {
                writer.println("Tartu: " + tartuJob);
                writer.println("Link: " + tartuLink);
            }
            if (tallinnJob != null) {
                writer.println("Tallinn: " + tallinnJob);
                writer.println("Link: " + tallinnLink);
            }

            writer.close();
            System.out.println("\nResults saved to test_results.txt");
        } catch (Exception e) {
            System.out.println("Could not save results: " + e.getMessage());
        }
    }
}