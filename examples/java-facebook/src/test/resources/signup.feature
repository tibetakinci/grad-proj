Feature: Facebook

  Scenario Outline: Signup
    Given I launch facebook homepage on google chrome browser
    When I set parameters <firstname> <lastname> <mail> <password>
    And set birth date to <day> <month> <year>
    And set sex to <sex>
    Then click sign up button to see whether it will sign up or not

    Parameters:
    firstname:{serif, tibet}
    lastname:{akdemir, yilmaz}
    mail:{oqan@akdemir.com, serif@pagi.com}
    password:{Rwtw23, rastgeleSalladim}
    day:{23,3}
    month:{5, 2}
    year:{1974, 2000}
    sex:{male, Female}

    t:2