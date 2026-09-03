package com.makersacademy.acebook.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.javafaker.Faker;
import com.makersacademy.acebook.repository.FriendRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HomePageLayoutTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRepository friendRepository;

    static WebDriver driver;
    Faker faker;


    @BeforeAll
    public static void setup() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        driver = new ChromeDriver();
        driver.get("http://localhost:8081/");
        //driver.findElement(By.name("username")).sendKeys("test1@example.com");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")))
                .sendKeys("test1@example.com");

        driver.findElement(By.name("password")).sendKeys("P@55qw0rd123456789");
        driver.findElement(By.name("action")).click();

    }

//    @AfterAll
//    public void tearDown() {
//        driver.close();
//    }


    @Test
    public void checkNavBar() {
        String navBarText = driver.findElement(By.className("navbar")).getText();
        assertEquals("Acebook\n" +
                "Home\n" +
                "Profile\n" +
                "Friends\n" +
                "Signed in as\n" +
                "test1@example.com\n" +
                "Log out", navBarText);
    }

    @Test
    public void checkCreatePostDiv() {
        String createPostText = driver.findElement(By.className("create-post")).getText();
        assertEquals("Create a post\n" +
                "What's on your mind?\n" +
                "Add a photo\n" +
                "Add a song\n" +
                "Search", createPostText);
    }

    @Test
    public void checkRecentPostHeaderShows() {
        String recentPostSubheaderText = driver.findElement(By.id("recent-post-header")).getText();
        assertEquals("Recent posts", recentPostSubheaderText);
    }

    @Test
    public void checkOnePostShows() {
        String recentPostText = driver.findElement(By.className("posts")).getText();
        assertEquals("testuser3\n" +
                "03/09/2026 15:49\n" +
                "hello\n" +
                "Like (0)\n" +
                "Comment\n" +
                "Share\n" +
                "Delete", recentPostText);
    }
}