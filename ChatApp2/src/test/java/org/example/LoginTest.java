package org.example;
// test/LoginTest.java
import org.example.chatapp.auth.Login;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals; // Add this import for assertEquals

public class LoginTest {

    private Login loginApp;

    @BeforeEach
    public void setUp() {
        loginApp = new Login();
        // Clear the static users map before each test to ensure test isolation
        Login.clearUsersForTesting();
    }

    // --- Username Tests ---
    @Test
    public void testUsernameCorrectlyFormatted() {
        assertTrue(loginApp.checkUserName("john_"), "Username 'john_' should be valid.");
        assertTrue(loginApp.checkUserName("s_"), "Username 's_' should be valid.");
    }

    @Test
    public void testUsernameIncorrectlyFormattedNoUnderscore() {
        assertFalse(loginApp.checkUserName("john"), "Username 'john' should be invalid (no underscore).");
    }

    @Test
    public void testUsernameIncorrectlyFormattedTooLong() {
        assertFalse(loginApp.checkUserName("john_doe"), "Username 'john_doe' should be invalid (too long).");
    }

    @Test
    public void testUsernameIncorrectlyFormattedNoSpecialChars() {
        assertFalse(loginApp.checkUserName("john.doe"), "Username 'john.doe' should be invalid (contains non-alphanumeric, non-underscore chars).");
    }

    // --- Password Complexity Tests ---
    @Test
    public void testPasswordComplexityCorrectlyFormatted() {
        assertTrue(loginApp.checkPasswordComplexity("Pa$$w0rd!"), "Password 'Pa$$w0rd!' should be valid.");
        assertTrue(loginApp.checkPasswordComplexity("MyP@ss123"), "Password 'MyP@ss123' should be valid.");
    }

    @Test
    public void testPasswordComplexityTooShort() {
        assertFalse(loginApp.checkPasswordComplexity("Short1!"), "Password 'Short1!' should be invalid (too short).");
    }

    @Test
    public void testPasswordComplexityNoCapital() {
        assertFalse(loginApp.checkPasswordComplexity("password1!"), "Password 'password1!' should be invalid (no capital).");
    }

    @Test
    public void testPasswordComplexityNoNumber() {
        assertFalse(loginApp.checkPasswordComplexity("Password!!"), "Password 'Password!!' should be invalid (no number).");
    }

    @Test
    public void testPasswordComplexityNoSpecialChar() {
        assertFalse(loginApp.checkPasswordComplexity("Password123"), "Password 'Password123' should be invalid (no special character).");
    }

    // --- Cell Phone Number Tests ---
    @Test
    public void testCellPhoneNumberCorrectlyFormatted() {
        assertTrue(loginApp.checkCellPhoneNumber("+27712345678"), "Cell phone '+27712345678' should be valid.");
        assertTrue(loginApp.checkCellPhoneNumber("+27831234567"), "Cell phone '+27831234567' should be valid.");
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormattedNoPlus27() {
        assertFalse(loginApp.checkCellPhoneNumber("0712345678"), "Cell phone '0712345678' should be invalid (missing +27).");
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormattedTooShort() {
        assertFalse(loginApp.checkCellPhoneNumber("+2771234567"), "Cell phone '+2771234567' should be invalid (too short).");
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormattedTooLong() {
        assertFalse(loginApp.checkCellPhoneNumber("+277123456789"), "Cell phone '+277123456789' should be invalid (too long).");
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormattedInvalidChars() {
        assertFalse(loginApp.checkCellPhoneNumber("+27ABCDE1234"), "Cell phone '+27ABCDE1234' should be invalid (non-digit chars).");
    }

    // --- Registration and Login Flow Tests ---
    @Test
    public void testRegisterAndLoginSuccess() {
        String regMessage = loginApp.registerUser("user_", "Passw0rd!", "+27831234567", "John", "Doe");
        Assertions.assertEquals("User registered successfully.", regMessage, "Registration should be successful.");

        assertTrue(loginApp.loginUser("user_", "Passw0rd!"), "Login should be successful with correct credentials.");
        Assertions.assertEquals("Welcome John Doe, it is great to see you again.", loginApp.returnLoginStatus(true), "Login status message should be correct.");
    }

    @Test
    public void testRegisterFailureThenLoginFailure() {
        // Test with invalid username (no underscore)
        String regMessage1 = loginApp.registerUser("user", "password", "071", "Jane", "Smith");
        Assertions.assertNotEquals("User registered successfully.", regMessage1, "Registration should fail due to invalid username input.");
        assertFalse(loginApp.loginUser("user", "password"), "Login should fail if registration failed or wrong credentials.");

        // Test with invalid password (too short)
        String regMessage2 = loginApp.registerUser("test_user", "short", "+27831234567", "Test", "User");
        Assertions.assertNotEquals("User registered successfully.", regMessage2, "Registration should fail due to invalid password input.");
        assertFalse(loginApp.loginUser("test_user", "short"), "Login should fail if registration failed or wrong credentials.");

        // Test with invalid phone number (missing +27)
        String regMessage3 = loginApp.registerUser("another_user", "Passw0rd1!", "0712345678", "Another", "User");
        Assertions.assertNotEquals("User registered successfully.", regMessage3, "Registration should fail due to invalid phone number input.");
        assertFalse(loginApp.loginUser("another_user", "Passw0rd1!"), "Login should fail if registration failed or wrong credentials.");

        // Assert failure message for any login attempt after failed registration
        Assertions.assertEquals("Username or password incorrect, please try again.", loginApp.returnLoginStatus(false), "Login status message should indicate failure.");
    }


    @Test
    public void testLoginWithIncorrectCredentials() {
        loginApp.registerUser("test_", "TestPass1!", "+27721234567", "Test", "User"); // Ensure a user exists
        assertFalse(loginApp.loginUser("wrong_user", "TestPass1!"), "Login should fail with incorrect username.");
        assertFalse(loginApp.loginUser("test_", "WrongPass!"), "Login should fail with incorrect password.");
        assertFalse(loginApp.loginUser("wrong_user", "WrongPass!"), "Login should fail with both incorrect.");
        Assertions.assertEquals("Username or password incorrect, please try again.", loginApp.returnLoginStatus(false), "Login status message should indicate failure.");
    }

    // --- Tests for Getters and Setters of Stored Login Details ---
    @Test
    public void testSetAndGetStoredCellPhoneNumber() {
        String testPhoneNumber = "+27771234567"; // A valid 12-digit SA number
        loginApp.setStoredCellPhoneNumber(testPhoneNumber);
        assertEquals(testPhoneNumber, loginApp.getStoredCellPhoneNumber(), "Stored cell phone number should match the set value.");
    }

    @Test
    public void testGetStoredFirstNameAfterLogin() {
        // Register a user that will be successfully logged in
        // Changed username to comply with max 7 characters. Example: "a_user" (6 chars)
        String regMsg = loginApp.registerUser("a_user_", "GetterPass1!", "+27821112222", "Alice", "Smith");
        Assertions.assertEquals("User registered successfully.", regMsg, "Pre-condition: User should register successfully.");

        // Attempt login
        boolean loginSuccess = loginApp.loginUser("a_user_", "GetterPass1!");
        assertTrue(loginSuccess, "Login should be successful for 'a_user_'.");

        // Now assert the stored first name
        assertEquals("Alice", loginApp.getStoredFirstName(), "Stored first name should be 'Alice' after login.");
    }

    @Test
    public void testGetStoredLastNameAfterLogin() {
        // Changed username to comply with max 7 characters. Example: "b_user2" (7 chars)
        String regMsg = loginApp.registerUser("b_user2", "GetterPass2!", "+27823334444", "Bob", "Johnson");
        Assertions.assertEquals("User registered successfully.", regMsg, "Pre-condition: User should register successfully.");

        boolean loginSuccess = loginApp.loginUser("b_user2", "GetterPass2!");
        assertTrue(loginSuccess, "Login should be successful for 'b_user2'.");

        assertEquals("Johnson", loginApp.getStoredLastName(), "Stored last name should be 'Johnson' after login.");
    }
}



