package org.example.chatapp.auth;
// src/Message.java (Part 3)
import com.google.gson.Gson; // For JSON serialization
import javax.swing.JOptionPane; // For displaying messages
import java.util.Random; // For generating Message ID

public class Message {
    private final String messageID;
    private final String recipientCellNumber;
    private final String messageText;
    private String messageHash;
    private boolean isSent;
    // You can add isReceived and isRead flags here if needed for future parts

    // Static counter for total messages sent.
    public static int totalMessagesSent = 0;

    /**
     * Constructor for the Message class.
     * Auto-generates a unique 10-digit message ID.
     * @param recipientCellNumber The cell number of the recipient.
     * @param messageText The actual message payload.
     */
    public Message(String recipientCellNumber, String messageText) {
        this.messageID = generateMessageID(); // Generate unique 10-digit ID
        this.recipientCellNumber = recipientCellNumber;
        this.messageText = messageText;
        this.isSent = false; // Default to not sent
    }

    // Getters
    public String getMessageID() {
        return messageID;
    }

    public String getRecipientCellNumber() {
        return recipientCellNumber;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public boolean isSent() {
        return isSent;
    }

    // Setters
    public void setSent(boolean sent) {
        isSent = sent;
    }

    /**
     * Generates a unique, random 10-digit message ID.
     * @return A 10-digit string representing the message ID.
     */
    private String generateMessageID() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10)); // Append a random digit (0-9)
        }
        return sb.toString();
    }

    /**
     * Checks if the message ID is no more than ten characters.
     * @return True if the message ID length is valid, false otherwise.
     */
    public boolean checkMessageID() {
        return messageID.length() <= 10;
    }

    /**
     * Creates and returns the Message Hash.
     * The hash contains the first two numbers of the message ID, a colon {:},
     * the number of the message ID (totalMessagesSent, as per provided example),
     * and the first and last words in the message (displayed in all caps).
     * Example: 00:0:HITHANKS
     * @return The generated message hash.
     */
    public final String createMessageHash() {
        String firstTwoID = messageID.substring(0, 2);
        String[] words = messageText.split("\\s+"); // Split by one or more spaces
        @SuppressWarnings("ReassignedVariable") String firstWord = "";
        @SuppressWarnings("ReassignedVariable") String lastWord = "";

        if (words.length > 0) {
            firstWord = words[0];
            if (words.length > 1) {
                lastWord = words[words.length - 1];
            } else {
                lastWord = firstWord; // If only one word, first and last are the same
            }
        }

        // --- FIX START ---
        // Strip non-alphanumeric characters from words before combining and uppercasing
        // This handles punctuation like "?" or "!" at the end of words
        firstWord = firstWord.replaceAll("[^a-zA-Z0-9]", "");
        lastWord = lastWord.replaceAll("[^a-zA-Z0-9]", "");
        // --- FIX END ---

        String combinedWords = (firstWord + lastWord).toUpperCase();

        this.messageHash = String.format("%s:%d:%s", firstTwoID, totalMessagesSent, combinedWords);
        return this.messageHash;
    }

    /**
     * Increments the static counter for the total number of messages sent.
     */
    public static void incrementTotalMessagesSent() {
        totalMessagesSent++;
    }

    /**
     * Returns the total number of messages sent.
     * @return The current count of total messages sent.
     */
    public static int getTotalMessagesSent() {
        return totalMessagesSent;
    }

    /**
     * Allows the user to choose to send, store, or disregard the message.
     * Uses JOptionPane for interaction.
     * @return An integer representing the user's choice: 1 for Send, 2 for Store, 3 for Disregard.
     */
    public int sendMessageOptions() {
        String[] options = {"Send Message", "Store Message to send later", "Disregard Message"};
        int choice = JOptionPane.showOptionDialog(null,
                "What would you like to do with this message?",
                "Message Options",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        return choice + 1; // Adjust to 1-based indexing
    }

    /**
     * Returns a string containing full message details for display,
     * including MessageID, Message Hash, Recipient, and Message.
     * @return Formatted string of message details.
     */
    public String getMessageDetailsForDisplay() {
        return "Message ID: " + messageID + "\n" +
                "Message Hash: " + (messageHash != null ? messageHash : "Not Generated") + "\n" +
                "Recipient: " + recipientCellNumber + "\n" +
                "Message: \"" + messageText + "\"";
    }

    /**
     * Converts the Message object to a JSON string.
     * Assisted by Google Gemini (for Gson usage).
     * @return A JSON string representation of the message.
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

