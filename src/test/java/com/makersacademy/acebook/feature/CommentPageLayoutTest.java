package com.makersacademy.acebook.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.javafaker.Faker;
import com.makersacademy.acebook.repository.CommentRepository;
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
public class CommentPageLayoutTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    CommentRepository commentRepository;

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
        driver.findElement(By.id("comment-button")).click();

    }

    @AfterAll
    public void tearDown() {
        driver.close();
    }


    @Test
    public void checkNavBar() {
        String navBarText = driver.findElement(By.className("navbar")).getText();
        assertEquals("Acebook\n" +
                "Home\n" +
                "Profile", navBarText);
    }

    @Test
    public void checkCommentIsVisible() {
        String commentText = driver.findElement(By.className("comment-body")).getText();
        assertEquals("katiejjbrown@outlook.com 03/09/2026 16:03\n" +
                "hi there", commentText);
    }

    @Test
    public void checkAddCommentVisible() {
        String addCommentText = driver.findElement(By.className("add-comment")).getText();
        assertEquals("Leave a comment\n" +
                "What do you think?\n" +
                "Comment", addCommentText);
    }

    @Test
    public void checkCommentPlaceholder() {
        String commentPlaceholderText = driver.findElement(By.id("content")).getAttribute("placeholder");
        assertEquals("Write a comment...", commentPlaceholderText);
    }

    @Test
    public void checkClearButton() {
        String clearButtonText = driver.findElement(By.id("clear-button")).getAttribute("value");
        assertEquals("Clear", clearButtonText);
    }

    @Test
    public void checkSubmitButton() {
        String submitButtonText = driver.findElement(By.id("submit-comment-button")).getText();
        assertEquals("Comment", submitButtonText);
    }

}