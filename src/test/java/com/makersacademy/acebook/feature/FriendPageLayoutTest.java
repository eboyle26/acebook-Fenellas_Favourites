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
public class FriendPageLayoutTest {
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
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait2.until(ExpectedConditions.invisibilityOfElementLocated(By.id("splash-screen")));

        driver.findElement(By.id("friends_button")).click();

    }

    @AfterAll
    public void tearDown() {
        driver.close();
    }

    //check friend page headers

    @Test
    public void checkSignedInUserViewsFriendPageHeader() {
        String friendSubheaderText = driver.findElement(By.id("friends-header")).getText();
        assertEquals("Friends", friendSubheaderText);
    }

    @Test
    public void checkFindPeopleHeaderDisplays() {
        String findFriendSubheaderText = driver.findElement(By.id("find-friends-header")).getText();
        assertEquals("Find people", findFriendSubheaderText);
    }

    @Test
    public void checkViewFriendsHeaderDisplays() {
        String viewFriendSubheaderText = driver.findElement(By.id("your-friends-header")).getText();
        assertEquals("Your friends", viewFriendSubheaderText);
    }

    @Test
    public void checkYouHaveNoFriends() {
        String noFriendSubheaderText = driver.findElement(By.id("noFriendsMessage")).getText();
        assertEquals("You don't have any friends yet.", noFriendSubheaderText);
    }

    @Test
    public void checkNavBar() {
        String navBarText = driver.findElement(By.className("navbar")).getText();
        assertEquals("Acebook\n" +
                "Home\n" +
                "Profile\n" +
                "Friends\n" +
                "Messages\n" +
                "Notifications\n" +
                "Signed in as\n" +
                "test1@example.com\n" +
                "Log out"
                , navBarText);
    }

    @Test
    public void checkSearchBarAppears() {
        String searchBarPlaceholderText = driver.findElement(By.id("userSearch")).getAttribute("placeholder");
        assertEquals("Search for a user...", searchBarPlaceholderText);
    }
}
