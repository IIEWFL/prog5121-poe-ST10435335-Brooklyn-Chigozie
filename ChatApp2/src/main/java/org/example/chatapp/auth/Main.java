package org.example.chatapp.auth;
// src/Main.java (Part 3)
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JOptionPane;
import com.google.gson.Gson; // For JSON parsing
import com.google.gson.reflect.TypeToken; // For deserializing List<Message>
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type; // For TypeToken


//https://www.w3schools.com/java/default.asp
//https://www.w3schools.com/java/java_getstarted.asp
//https://www.w3schools.com/java/java_output.asp
//https://www.w3schools.com/java/java_booleans.asp
//https://www.w3schools.com/java/java_arrays.asp
//https://www.w3schools.com/java/java_methods.asp
//Google Gemini AI Language Model, version 2025
public class Main {

    // --- Arrays to populate as per Part 3 requirements ---
    private static final List<Message> sentMessages = new ArrayList<>(); // Contains all messages sent.
    private static final List<Message> disregardedMessages = new ArrayList<>(); // Contains all messages that were disregarded.
    private static final List<Message> storedMessages = new ArrayList<>(); // Contains the stored messages (from JSON).
    private static final List<String> messageHashes = new ArrayList<>(); // Contains all message hashes.
    private static final List<String> messageIDs = new ArrayList<>(); // Contains all message IDs.


    private static final Login loginApp = new Login(); // Re-use the Login instance from Part 1

    private static final String STORED_MESSAGES_FILE = "stored_messages.json"; // File for JSON storage

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // --- Part 1: Registration ---
        System.out.println("--- Chat App Registration ---");
        boolean registrationSuccessful = false;
        while (!registrationSuccessful) {
            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter your last name: ");
            String lastName = scanner.nextLine();

            System.out.print("Enter desired username (underscore and max 5 chars): ");
            String username = scanner.nextLine();

            System.out.print("Enter desired password (>=8 chars, capital, number, special char): ");
            String password = scanner.nextLine();

            System.out.print("Enter South African cell phone number (+27XXXXXXXXX or +27XXXXXXXXXX): ");
            String cellPhoneNumber = scanner.nextLine();

            String registrationMessage = loginApp.registerUser(username, password, cellPhoneNumber, firstName, lastName);
            System.out.println(registrationMessage);

            if (registrationMessage.equals("User registered successfully.")) {
                registrationSuccessful = true;
            } else {
                System.out.println("Registration failed. Please try again.");
            }
        }

        // --- Part 1: Login ---
        System.out.println("\n--- Chat App Login ---");
        boolean loggedIn = false;
        int loginAttempts = 0;
        while (!loggedIn && loginAttempts < 3) {
            System.out.print("Enter your username: ");
            String loginUsername = scanner.nextLine();

            System.out.print("Enter your password: ");
            String loginPassword = scanner.nextLine();

            loggedIn = loginApp.loginUser(loginUsername, loginPassword);
            String loginStatusMessage = loginApp.returnLoginStatus(loggedIn);
            System.out.println(loginStatusMessage);

            if (!loggedIn) {
                loginAttempts++;
                if (loginAttempts < 3) {
                    System.out.println("Login attempt " + loginAttempts + " failed. Please try again.");
                }
            }
        }

        if (loggedIn) {
            System.out.println("\nWelcome to QuickChat.");
            loadStoredMessages(); // Load any previously stored messages at startup
            runChatMenu(scanner); // Proceed to chat menu
        } else {
            System.out.println("Too many failed login attempts. Exiting application.");
        }

        scanner.close();
    }

    /**
     * Displays the main chat application menu and handles user choices.
     * The menu is displayed numerically using `JOptionPane`.
     * @param scanner The Scanner object for console input.
     */
    private static void runChatMenu(Scanner scanner) {
        boolean quit = false;
        while (!quit) {
            String[] menuOptions = {"Send Messages", "Show recently sent messages", "Display Reports", "Quit"};
            int choice = JOptionPane.showOptionDialog(null,
                    """
                            --- QuickChat Menu ---

                            1) Send Messages
                            2) Show recently sent messages
                            3) Display Reports
                            4) Quit""",
                    "QuickChat Menu",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    menuOptions,
                    menuOptions[0]); // Default selected option

            int menuChoice = choice + 1; // JOptionPane returns 0-indexed, convert to 1-indexed for menu

            switch (menuChoice) {
                case 1: // Send Messages
                    sendMessages(scanner);
                    break;
                case 2: // Show recently sent messages (now calls full report)
                    displayAllSentMessages();
                    break;
                case 3: // Display Reports
                    displayReportMenu(scanner);
                    break;
                case 4: // Quit
                    quit = true;
                    System.out.println("Exiting QuickChat. Goodbye!");
                    break;
                case -1: // User closed the dialog (equivalent to quit)
                    quit = true;
                    System.out.println("Exiting QuickChat. Goodbye!");
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid choice. Please select 1, 2, 3, or 4.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Guides the user through sending messages, including input validation and message options.
     * Uses a loop to allow the user to enter the assigned number of messages.
     * @param scanner The Scanner object for console input.
     */
    private static void sendMessages(Scanner scanner) {
        int numMessagesToEnter;
        while (true) {
            System.out.print("How many messages do you wish to enter? ");
            try {
                numMessagesToEnter = Integer.parseInt(scanner.nextLine());
                if (numMessagesToEnter <= 0) {
                    System.out.println("Please enter a positive number of messages.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        for (int i = 0; i < numMessagesToEnter; i++) {
            System.out.println("\n--- Composing Message " + (i + 1) + " ---");

            String recipient;
            boolean validRecipient = false;
            do {
                System.out.print("Enter recipient cell number (+27XXXXXXXXX or +27XXXXXXXXXX): ");
                recipient = scanner.nextLine();
                if (loginApp.checkCellPhoneNumber(recipient)) {
                    validRecipient = true;
                    System.out.println("Cell phone number successfully captured.");
                } else {
                    System.out.println("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
                }
            } while (!validRecipient);

            String messageText;
            boolean validMessageLength = false;
            do {
                System.out.print("Enter your message (max 250 characters): ");
                messageText = scanner.nextLine();
                if (messageText.length() <= 250) {
                    validMessageLength = true;
                    System.out.println("Message ready to send.");
                } else {
                    int exceededBy = messageText.length() - 250;
                    System.out.println("Message exceeds 250 characters by " + exceededBy + ", please reduce size.");
                }
            } while (!validMessageLength);

            Message currentMessage = new Message(recipient, messageText);
            System.out.println("Message ID generated: " + currentMessage.getMessageID());

            // Add ID to messageIDs array
            messageIDs.add(currentMessage.getMessageID());

            int choice = currentMessage.sendMessageOptions();

            switch (choice) {
                case 1: // Send Message
                    currentMessage.setSent(true);
                    Message.incrementTotalMessagesSent();
                    currentMessage.createMessageHash();
                    sentMessages.add(currentMessage);
                    messageHashes.add(currentMessage.getMessageHash());
                    System.out.println("Message successfully sent.");
                    JOptionPane.showMessageDialog(null, currentMessage.getMessageDetailsForDisplay(),
                            "Message Sent Details", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case 2: // Store Message (JSON)
                    currentMessage.createMessageHash(); // Generate hash for stored messages too
                    storedMessages.add(currentMessage);
                    messageHashes.add(currentMessage.getMessageHash());
                    saveStoredMessages(); // Save all stored messages to JSON file
                    System.out.println("Message successfully stored.");
                    JOptionPane.showMessageDialog(null, "Message stored for later sending (JSON):\n" + currentMessage.toJson(),
                            "Message Stored", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case 3: // Disregard Message
                    disregardedMessages.add(currentMessage);
                    // No hash or total messages count for disregarded as per requirements.
                    System.out.println("Press 0 to delete message."); // Interpreting this as "message disregarded"
                    break;
                default:
                    System.out.println("Invalid option. Message disregarded by default.");
                    disregardedMessages.add(currentMessage);
                    break;
            }
        }
        System.out.println("\nTotal messages sent during this session: " + Message.getTotalMessagesSent());
    }

    /**
     * Loads messages from the stored_messages.json file into the storedMessages array.
     */
    private static void loadStoredMessages() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(STORED_MESSAGES_FILE)) {
            // Define the type for Gson to deserialize a List of Message objects
            Type messageListType = new TypeToken<ArrayList<Message>>(){}.getType();
            List<Message> loadedMessages = gson.fromJson(reader, messageListType);
            if (loadedMessages != null) {
                storedMessages.clear(); // Clear existing in-memory list before loading
                storedMessages.addAll(loadedMessages);
                // Also add their IDs and hashes to the respective tracking lists if not already present
                for (Message msg : loadedMessages) {
                    if (!messageIDs.contains(msg.getMessageID())) {
                        messageIDs.add(msg.getMessageID());
                    }
                    // Ensure hash is created and added if it wasn't during loading (e.g., if loaded from old file)
                    if (msg.getMessageHash() == null) {
                        msg.createMessageHash(); // Re-create hash if missing
                    }
                    if (msg.getMessageHash() != null && !messageHashes.contains(msg.getMessageHash())) {
                        messageHashes.add(msg.getMessageHash());
                    }
                }
                System.out.println("Stored messages loaded successfully from " + STORED_MESSAGES_FILE);
            }
        } catch (IOException e) {
            System.out.println("No existing stored messages file found, or error reading file: " + e.getMessage());
            // This is common on first run, so don't treat as a critical error.
        }
    }

    /**
     * Saves all messages in the storedMessages array to the stored_messages.json file.
     */
    private static void saveStoredMessages() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(STORED_MESSAGES_FILE)) {
            gson.toJson(storedMessages, writer);
            System.out.println("Messages saved to " + STORED_MESSAGES_FILE);
        } catch (IOException e) {
            System.err.println("Error saving messages: " + e.getMessage());
        }
    }

    /**
     * Displays a menu for various reporting options for messages.
     * @param scanner The Scanner object for console input.
     */
    private static void displayReportMenu(Scanner scanner) {
        boolean backToMainMenu = false;
        while (!backToMainMenu) {
            String[] reportOptions = {
                    "Display All Sent Messages",
                    "Display Longest Sent Message",
                    "Search by Message ID",
                    "Search by Recipient",
                    "Delete Message by Hash",
                    "Back to Main Menu"
            };
            int choice = JOptionPane.showOptionDialog(null,
                    """
                            --- Message Reports ---

                            1) Display All Sent Messages
                            2) Display Longest Sent Message
                            3) Search by Message ID
                            4) Search by Recipient
                            5) Delete Message by Hash
                            6) Back to Main Menu""",
                    "Message Reports",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    reportOptions,
                    reportOptions[0]);

            int reportChoice = choice + 1;

            switch (reportChoice) {
                case 1:
                    displayAllSentMessages();
                    break;
                case 2:
                    displayLongestSentMessage();
                    break;
                case 3:
                    searchMessageByID(scanner);
                    break;
                case 4:
                    searchMessagesByRecipient(scanner);
                    break;
                case 5:
                    deleteMessageByHash(scanner);
                    break;
                case 6:
                    backToMainMenu = true;
                    System.out.println("Returning to main menu.");
                    break;
                case -1: // User closed the dialog
                    backToMainMenu = true;
                    System.out.println("Returning to main menu.");
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid choice. Please select a valid option.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Displays a report that lists the full details of all sent messages.
     * Includes Message Hash, Recipient, Message.
     * Also displays sender (implicit: sender is the logged-in user).
     */
    private static void displayAllSentMessages() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages have been sent yet.", "Sent Messages Report", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder report = new StringBuilder("--- Full Report of All Sent Messages ---\n\n");
        report.append("Sender: ").append(loginApp.getStoredFirstName()).append(" ").append(loginApp.getStoredLastName()).append("\n\n");

        for (int i = 0; i < sentMessages.size(); i++) {
            Message msg = sentMessages.get(i);
            report.append("Message #").append(i + 1).append(":\n");
            report.append(msg.getMessageDetailsForDisplay()).append("\n\n");
        }
        JOptionPane.showMessageDialog(null, report.toString(), "All Sent Messages Report", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Displays the longest sent message.
     */
    private static void displayLongestSentMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages have been sent to determine the longest message.", "Longest Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Message longestMessage = null;
        int maxLength = -1;

        for (Message msg : sentMessages) {
            if (msg.getMessageText().length() > maxLength) {
                maxLength = msg.getMessageText().length();
                longestMessage = msg;
            }
        }

        JOptionPane.showMessageDialog(null,
                "Longest Sent Message:\n" +
                        "Length: " + maxLength + " characters\n" +
                        "Message: \"" + longestMessage.getMessageText() + "\"",
                "Longest Message Report", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Searches for a message by its ID and displays the corresponding recipient and message.
     * @param ignoredScanner The Scanner object for console input (not used directly for input here due to JOptionPane).
     */
    private static void searchMessageByID(Scanner ignoredScanner) {
        String searchID = JOptionPane.showInputDialog(null, "Enter Message ID to search:");
        if (searchID == null || searchID.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Search cancelled or empty ID entered.", "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Message foundMessage = null;
        // Search in sent messages
        for (Message msg : sentMessages) {
            if (msg.getMessageID().equals(searchID.trim())) {
                foundMessage = msg;
                break;
            }
        }
        // Search in stored messages
        if (foundMessage == null) {
            for (Message msg : storedMessages) {
                if (msg.getMessageID().equals(searchID.trim())) {
                    foundMessage = msg;
                    break;
                }
            }
        }
        // Search in disregarded messages (optional, based on requirement "search for all messages sent or stored")
        if (foundMessage == null) {
            for (Message msg : disregardedMessages) {
                if (msg.getMessageID().equals(searchID.trim())) {
                    foundMessage = msg;
                    break;
                }
            }
        }


        if (foundMessage != null) {
            JOptionPane.showMessageDialog(null,
                    "Message Found (ID: " + searchID + "):\n" +
                            "Recipient: " + foundMessage.getRecipientCellNumber() + "\n" +
                            "Message: \"" + foundMessage.getMessageText() + "\"",
                    "Message Search Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No message found with ID: " + searchID, "Message Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Searches for all messages sent to a particular recipient.
     * @param ignoredScanner The Scanner object for console input (not used directly for input here due to JOptionPane).
     */
    private static void searchMessagesByRecipient(Scanner ignoredScanner) {
        String searchRecipient = JOptionPane.showInputDialog(null, "Enter Recipient Cell Number to search:");
        if (searchRecipient == null || searchRecipient.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Search cancelled or empty recipient entered.", "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Message> matchingMessages = new ArrayList<>();
        // Search across all message lists
        for (Message msg : sentMessages) {
            if (msg.getRecipientCellNumber().equals(searchRecipient.trim())) {
                matchingMessages.add(msg);
            }
        }
        for (Message msg : storedMessages) {
            if (msg.getRecipientCellNumber().equals(searchRecipient.trim())) {
                matchingMessages.add(msg);
            }
        }
        for (Message msg : disregardedMessages) {
            if (msg.getRecipientCellNumber().equals(searchRecipient.trim())) {
                matchingMessages.add(msg);
            }
        }


        if (!matchingMessages.isEmpty()) {
            StringBuilder result = new StringBuilder("Messages for Recipient: " + searchRecipient + "\n\n");
            for (Message msg : matchingMessages) {
                result.append("- \"").append(msg.getMessageText()).append("\" (ID: ").append(msg.getMessageID()).append(", Status: ");
                if (msg.isSent()) result.append("Sent");
                else if (storedMessages.contains(msg)) result.append("Stored"); // This check is approximate, a flag on Message would be better
                else if (disregardedMessages.contains(msg)) result.append("Disregarded");
                result.append(")\n");
            }
            JOptionPane.showMessageDialog(null, result.toString(), "Messages by Recipient", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No messages found for recipient: " + searchRecipient, "Messages by Recipient", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Deletes a message using its message hash.
     * @param ignoredScanner The Scanner object for console input (not used directly for input here due to JOptionPane).
     */
    private static void deleteMessageByHash(Scanner ignoredScanner) {
        String hashToDelete = JOptionPane.showInputDialog(null, "Enter Message Hash to delete:");
        if (hashToDelete == null || hashToDelete.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Deletion cancelled or empty hash entered.", "Delete Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean foundAndRemoved = false;
        String deletedMessageText = "";

        // Attempt to remove from sentMessages
        var sentIterator = sentMessages.iterator();
        while (sentIterator.hasNext()) {
            Message msg = sentIterator.next();
            if (msg.getMessageHash() != null && msg.getMessageHash().equals(hashToDelete.trim())) {
                deletedMessageText = msg.getMessageText();
                sentIterator.remove();
                foundAndRemoved = true;
                break;
            }
        }

        // If not found in sent, attempt to remove from storedMessages
        if (!foundAndRemoved) {
            var storedIterator = storedMessages.iterator();
            while (storedIterator.hasNext()) {
                Message msg = storedIterator.next();
                if (msg.getMessageHash() != null && msg.getMessageHash().equals(hashToDelete.trim())) {
                    deletedMessageText = msg.getMessageText();
                    storedIterator.remove();
                    foundAndRemoved = true;
                    saveStoredMessages(); // Save file after removal from storedMessages
                    break;
                }
            }
        }

        // If not found in sent or stored, attempt to remove from disregardedMessages
        if (!foundAndRemoved) {
            var disregardedIterator = disregardedMessages.iterator();
            while (disregardedIterator.hasNext()) {
                Message msg = disregardedIterator.next();
                if (msg.getMessageHash() != null && msg.getMessageHash().equals(hashToDelete.trim())) {
                    deletedMessageText = msg.getMessageText();
                    disregardedIterator.remove();
                    foundAndRemoved = true;
                    break;
                }
            }
        }

        // Also remove from global ID and Hash lists if it was there
        if (foundAndRemoved) {
            // Removing from messageIDs and messageHashes needs careful handling
            // as multiple messages might have the same hash if not unique enough.
            // For simplicity, we remove the first occurrence.
            messageHashes.remove(hashToDelete.trim());
            // If message IDs are truly unique, we can iterate and remove.
            // For production, you'd want to store the ID of the deleted message to remove it here.
            // For now, if a message was deleted, its hash and ID (if present) are removed.
        }


        if (foundAndRemoved) {
            JOptionPane.showMessageDialog(null, "Message \"" + deletedMessageText + "\" successfully deleted.", "Delete Message", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No message found with hash: " + hashToDelete, "Delete Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}