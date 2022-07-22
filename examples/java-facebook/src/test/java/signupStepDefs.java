import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import static org.junit.Assert.assertEquals;

public class signupStepDefs {

    WebDriver driver;

    @Given("^I launch facebook homepage on google chrome browser$")
    public void iLaunchFacebookHomepageOnGoogleChromeBrowser() {
        System.setProperty("webdriver.chrome.driver", "/Users/tibet/IdeaProjects/chromedriver");
        driver = new ChromeDriver();
        driver.get("https://www.facebook.com");
        driver.manage().window().maximize();
    }

    @When("^I set parameters (.*?) (.*?) (.*?) (.*?)$")
    public void iSetParametersFirstnameLastnameMailPassword(String firstname, String lastname, String mail, String password) {
        driver.findElement(By.name("firstname")).sendKeys(firstname);
        driver.findElement(By.name("lastname")).sendKeys(lastname);
        driver.findElement(By.name("reg_email__")).sendKeys(mail);
        driver.findElement(By.name("reg_email_confirmation__")).sendKeys(mail);
        driver.findElement(By.name("reg_passwd__")).sendKeys(password);
    }

    @And("^set birth date to (.*?) (.*?) (.*?)$")
    public void setBirthDateToBirthdate(String day, String month, String year) {
        Select birthday = new Select(driver.findElement(By.name("birthday_day")));
        birthday.selectByVisibleText(day);
        Select birthmonth = new Select(driver.findElement(By.name("birthday_month")));
        birthmonth.selectByValue(month);
        Select birthyear = new Select(driver.findElement(By.name("birthday_year")));
        birthyear.selectByVisibleText(year);
    }

    @And("^set sex to (.*?)$")
    public void setSexToSex(String sex) {
        if ("male".equals(sex) || "Male".equals(sex)){
            driver.findElement(By.xpath("//input[@value='1']")).click();
        }
        else if ("female".equals(sex) || "Female".equals(sex)){
            driver.findElement(By.xpath("//input[@value='2']")).click();
        }
    }

    @Then("^click sign up button to see whether it will sign up or not$")
    public void clickSignUpButtonToSeeWhetherItWillSignUpOrNot() throws InterruptedException {
        driver.findElement(By.name("websubmit")).click();
        Thread.sleep(15000);
        boolean result = "https://www.facebook.com/".equals(driver.getCurrentUrl());
        assertEquals(false, result);
        driver.close();
    }
}
