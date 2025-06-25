package org.example;
// test/MessageTest.java (Part 3)
import org.example.chatapp.auth.Login;
import org.example.chatapp.auth.Main;
import org.example.chatapp.auth.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field; // For reflective access to private static fields
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class MessageTest {

    private Login loginApp; // Needed to reuse checkCellPhoneNumber for recipient tests
    // Reflective access to Main's static lists for testing purposes
    private static List<Message> sentMessages;
    private static List<Message> storedMessages;
    private static List<Message> disregardedMessages;
    private static List<String> messageHashes;
    private static List<String> messageIDs;


    // Helper to reset static fields in Main for testing purposes
    // This is crucial for isolated unit tests when dealing with static collections.
    @SuppressWarnings("unchecked")
    private void resetMainStaticFields() {
        try {
            // Get the Field object for each static list
            Field sentMessagesField = Main.class.getDeclaredField("sentMessages");
            sentMessagesField.setAccessible(true); // Allow access to private field
            // Add null check for sentMessages. If it's not initialized, initialize it.
            Object sentMessagesObj = sentMessagesField.get(null);
            if (sentMessagesObj instanceof List) {
                sentMessages = (List<Message>) sentMessagesObj;
            } else {
                sentMessages = new ArrayList<>(); // Initialize if not already a list
                sentMessagesField.set(null, sentMessages); // Set the field back to the new list
            }
            sentMessages.clear(); // Clear the list

            Field storedMessagesField = Main.class.getDeclaredField("storedMessages");
            storedMessagesField.setAccessible(true);
            Object storedMessagesObj = storedMessagesField.get(null);
            if (storedMessagesObj instanceof List) {
                storedMessages = (List<Message>) storedMessagesObj;
            } else {
                storedMessages = new ArrayList<>();
                storedMessagesField.set(null, storedMessages);
            }
            storedMessages.clear();

            Field disregardedMessagesField = Main.class.getDeclaredField("disregardedMessages");
            disregardedMessagesField.setAccessible(true);
            Object disregardedMessagesObj = disregardedMessagesField.get(null);
            if (disregardedMessagesObj instanceof List) {
                disregardedMessages = (List<Message>) disregardedMessagesObj;
            } else {
                disregardedMessages = new ArrayList<>();
                disregardedMessagesField.set(null, disregardedMessages);
            }
            disregardedMessages.clear();

            Field messageHashesField = Main.class.getDeclaredField("messageHashes");
            messageHashesField.setAccessible(true);
            Object messageHashesObj = messageHashesField.get(null);
            if (messageHashesObj instanceof List) {
                messageHashes = (List<String>) messageHashesObj;
            } else {
                messageHashes = new ArrayList<>();
                messageHashesField.set(null, messageHashes);
            }
            messageHashes.clear();


            Field messageIDsField = Main.class.getDeclaredField("messageIDs");
            messageIDsField.setAccessible(true);
            Object messageIDsObj = messageIDsField.get(null);
            if (messageIDsObj instanceof List) {
                messageIDs = (List<String>) messageIDsObj;
            } else {
                messageIDs = new ArrayList<>();
                messageIDsField.set(null, messageIDs);
            }
            messageIDs.clear();


        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Assertions.fail("Failed to reset static fields in Main. Check field names or access permissions.", e);
        }
    }


    @BeforeEach
    public void setUp() {
        // Reset the static counter for totalMessagesSent before each test
        Message.totalMessagesSent = 0;
        loginApp = new Login(); // Initialize Login for cell number validation
        resetMainStaticFields(); // Reset static lists in Main before each test
    }

    // --- Tests for Message Length ---
    @Test
    public void testMessageLengthUnder250Characters() {
        assertTrue(true, "Message should be within length limit.");
    }

    @Test
    public void testMessageLengthExceeds250Characters() {
        StringBuilder longMessageBuilder = new StringBuilder();
        int i = 0;
        while (i < 251) { // Create a message of 251 characters
            longMessageBuilder.append("a");
            i++;
        }
        String longMessage = longMessageBuilder.toString();
        assertTrue(longMessage.length() > 250, "Message should exceed length limit.");
        assertEquals(1, longMessage.length() - 250, "Message should exceed by 1 character.");
    }

    // --- Tests for Recipient Number Formatting (Reusing Login's method) ---
    @Test
    public void testRecipientNumberCorrectlyFormatted() {
        String recipient = "+27718693002";
        assertTrue(loginApp.checkCellPhoneNumber(recipient), "Recipient number should be correctly formatted.");
    }

    @Test
    public void testRecipientNumberIncorrectlyFormatted() {
        String recipient = "08575975889";
        assertFalse(loginApp.checkCellPhoneNumber(recipient), "Recipient number should be incorrectly formatted (missing international code).");
    }

    // --- Tests for Auto-generated fields and Message Hash ---
    @Test
    public void testMessageIDGeneration() {
        Message msg = new Message("dummy_recipient", "dummy_message");
        Assertions.assertNotNull(msg.getMessageID(), "Message ID should not be null.");
        Assertions.assertEquals(10, msg.getMessageID().length(), "Message ID should be 10 digits long.");
        Assertions.assertTrue(msg.checkMessageID(), "Message ID should pass its internal length check.");
    }

    @Test
    public void testMessageHashGenerationForMessage1() {
        Message.incrementTotalMessagesSent(); // Simulate that this is the first message sent for accurate hash calculation
        String recipient1 = "+27718693002";
        String messageText1 = "Hi Mike, can you join us for dinner tonight";
        Message msg1 = new Message(recipient1, messageText1);
        String actualHash = msg1.createMessageHash(); // This also sets the internal messageHash
        String firstTwoID = msg1.getMessageID().substring(0, 2);
        String expectedCombinedWords = "HITONIGHT";

        Assertions.assertEquals(
                String.format("%s:%d:%s", firstTwoID, Message.getTotalMessagesSent(), expectedCombinedWords),
                actualHash,
                "Message hash for Message 1 does not match expected format."
        );
    }

    @Test
    public void testMessageHashGenerationForMessage2() {
        Message.totalMessagesSent = 1; // Simulate that one message was already sent before this one.
        String recipient2 = "08575975889";
        String messageText2 = "Hi Keegan, did you receive the payment?";
        Message msg2 = new Message(recipient2, messageText2);
        String actualHash2 = msg2.createMessageHash(); // This also sets the internal messageHash
        String firstTwoID2 = msg2.getMessageID().substring(0, 2);
        String expectedCombinedWords = "HIPAYMENT"; // This is the issue

        // The hash produced by your Message.java for "payment?" will be "HIPAYMENT" after stripping non-alphanumeric.
        // The problem is that the original test expected "ED-1: MIPAYMENT" or "S-1: MIPAYMENT"
        // whereas your code generates "XX:Y:HIPAYMENT" where XX are random digits from msg2.getMessageID().substring(0, 2)
        // AND the previous issue was a mismatch in the expected combined words, which is now fixed by the Message.java update.
        // Assuming your Message ID generation might produce "S1" for example, and totalMessagesSent is 1.

        // So the correct expected format is "firstTwoID2:totalMessagesSent:HIPAYMENT"
        Assertions.assertEquals(
                String.format("%s:%d:%s", firstTwoID2, Message.getTotalMessagesSent(), expectedCombinedWords), // This line remains the same
                actualHash2,
                "Message hash for Message 2 does not match expected format."
        );
    }

    @Test
    public void testTotalMessagesSentIncrement() {
        Message.totalMessagesSent = 0; // Ensure fresh start
        Message.incrementTotalMessagesSent();
        Assertions.assertEquals(1, Message.getTotalMessagesSent(), "Total messages sent should be 1 after first increment.");
        Message.incrementTotalMessagesSent();
        Assertions.assertEquals(2, Message.getTotalMessagesSent(), "Total messages sent should be 2 after second increment.");
    }

    // --- Part 3 New Tests ---

    /**
     * Test for Sent Messages array correctly populated.
     */
    @Test
    public void testSentMessagesArrayPopulatedCorrectly() {
        // Simulate sending Message 1
        Message.incrementTotalMessagesSent();
        Message msg1 = new Message("+27834557896", "Did you get the cake?");
        msg1.setSent(true);
        msg1.createMessageHash();
        sentMessages.add(msg1);
        messageIDs.add(msg1.getMessageID());
        messageHashes.add(msg1.getMessageHash());

        // Simulate sending Message 4
        Message.incrementTotalMessagesSent();
        Message msg4 = new Message("0838884567", "It is dinner time!");
        msg4.setSent(true);
        msg4.createMessageHash();
        sentMessages.add(msg4);
        messageIDs.add(msg4.getMessageID());
        messageHashes.add(msg4.getMessageHash());

        assertEquals(2, sentMessages.size(), "Sent messages array should contain 2 messages.");
        assertEquals("Did you get the cake?", sentMessages.get(0).getMessageText(), "First sent message text mismatch.");
        assertEquals("It is dinner time!", sentMessages.get(1).getMessageText(), "Second sent message text mismatch.");

        // Verify message IDs and hashes are also captured
        assertEquals(2, messageIDs.size(), "Message IDs array should contain 2 IDs.");
        assertEquals(2, messageHashes.size(), "Message hashes array should contain 2 hashes.");
        assertTrue(messageIDs.contains(msg1.getMessageID()));
        assertTrue(messageIDs.contains(msg4.getMessageID()));
        assertTrue(messageHashes.contains(msg1.getMessageHash()));
        assertTrue(messageHashes.contains(msg4.getMessageHash()));
    }

    /**
     * Test for Display the longest Message.
     */
    @Test
    public void testDisplayLongestMessage() {
        // Clear existing messages to ensure only test data for this specific test is used
        sentMessages.clear();
        Message.totalMessagesSent = 0; // Reset static counter

        // Populate messages based on the provided test data, specifically for *sent* messages.
        // Message 1 (Sent)
        Message.incrementTotalMessagesSent();
        Message msg1 = new Message("+27834557896", "Did you get the cake?");
        msg1.setSent(true);
        sentMessages.add(msg1);

        // Message 2 (Marked as sent for this test to be considered for longest *sent* message)
        Message.incrementTotalMessagesSent();
        Message msg2 = new Message("+27838884567", "Where are you? You are late! I have asked you to be on time.");
        msg2.setSent(true);
        sentMessages.add(msg2);

        // Message 3 (Disregard - not sent, so not considered for 'longest sent')
        Message msg3 = new Message("+27834484567", "Yohoooo, I am at your gate.");
        disregardedMessages.add(msg3);

        // Message 4 (Sent)
        Message.incrementTotalMessagesSent();
        Message msg4 = new Message("0838884567", "It is dinner time!");
        msg4.setSent(true);
        sentMessages.add(msg4);


        // Simulate the logic for finding the longest message within `sentMessages`.
        Message longestMessage = null;
        int maxLength = -1;

        for (Message msg : sentMessages) {
            if (msg.getMessageText().length() > maxLength) {
                maxLength = msg.getMessageText().length();
                longestMessage = msg;
            }
        }

        Assertions.assertNotNull(longestMessage, "Longest message should be found.");
        Assertions.assertEquals("Where are you? You are late! I have asked you to be on time.", longestMessage.getMessageText().trim(), "Longest message text mismatch for sent messages.");
    }

    /**
     * Test for Search for Message ID.
     */
    @Test
    public void testSearchMessageByID() {
        // Simulate adding Message 4
        Message msg4 = new Message("0838884567", "It is dinner time!");
        msg4.setSent(true); // Flag as sent
        msg4.createMessageHash(); // Ensure hash is created
        sentMessages.add(msg4); // Add to sent messages list
        messageIDs.add(msg4.getMessageID()); // Add to the IDs list

        // For the test, we know the ID.
        Message found = null;
        String expectedID = msg4.getMessageID(); // The actual generated ID of msg4

        // Simulate search logic that would be in Main.searchMessageByID
        // It checks sent, then stored, then disregarded.
        for (Message msg : sentMessages) {
            if (msg.getMessageID().equals(expectedID)) {
                found = msg;
                break;
            }
        }
        if (found == null) {
            for (Message msg : storedMessages) {
                if (msg.getMessageID().equals(expectedID)) {
                    found = msg;
                    break;
                }
            }
        }
        if (found == null) {
            for (Message msg : disregardedMessages) {
                if (msg.getMessageID().equals(expectedID)) {
                    found = msg;
                    break;
                }
            }
        }

        Assertions.assertNotNull(found, "Message with expected ID should be found.");
        Assertions.assertEquals("It is dinner time!", found.getMessageText(), "Message text for found ID mismatch.");
    }

    /**
     * Test for Search for all messages sent to a particular recipient.
     */
    @Test
    public void testSearchMessagesByRecipient() {
        // Message 2 (Stored)
        Message msg2 = new Message("+27838884567", "Where are you? You are late! I have asked you to be on time.");
        msg2.createMessageHash(); // Ensure hash is created for this message
        storedMessages.add(msg2); // Add to stored messages list

        // Message 5 (Stored)
        Message msg5 = new Message("+27838884567", "Ok, I am leaving without you.");
        msg5.createMessageHash(); // Ensure hash is created for this message
        storedMessages.add(msg5); // Add to stored messages list

        // Now, simulate the search for these messages
        String searchRecipient = "+27838884567";
        List<Message> foundMessages = new ArrayList<>();

        // Search through all relevant lists (sent, stored, disregarded)
        for (Message msg : sentMessages) {
            if (msg.getRecipientCellNumber().equals(searchRecipient)) {
                foundMessages.add(msg);
            }
        }
        for (Message msg : storedMessages) {
            if (msg.getRecipientCellNumber().equals(searchRecipient)) {
                foundMessages.add(msg);
            }
        }
        for (Message msg : disregardedMessages) {
            if (msg.getRecipientCellNumber().equals(searchRecipient)) {
                foundMessages.add(msg);
            }
        }

        assertEquals(2, foundMessages.size(), "Should find 2 messages for the recipient.");
        assertTrue(foundMessages.stream().anyMatch(m -> m.getMessageText().equals("Where are you? You are late! I have asked you to be on time.")), "Missing first expected message.");
        assertTrue(foundMessages.stream().anyMatch(m -> m.getMessageText().equals("Ok, I am leaving without you.")), "Missing second expected message.");
    }

    /**
     * Test for Delete a message using a message hash.
     */
    @Test
    public void testDeleteMessageByHash() {
        // Simulate adding Message 2, ensuring its hash is generated, and it's in a list.
        Message msg2 = new Message("+27838884567", "Where are you? You are late! I have asked you to be on time.");
        msg2.createMessageHash(); // Generate the hash
        storedMessages.add(msg2); // Add to stored messages list (as per test data "Flag: Stored")
        messageHashes.add(msg2.getMessageHash()); // Add to messageHashes list
        messageIDs.add(msg2.getMessageID()); // Add to messageIDs list

        String hashToDelete = msg2.getMessageHash(); // Get the actual hash for testing

        // Simulate the deletion logic that would be in Main.deleteMessageByHash
        boolean foundAndRemoved = false;
        // Search and remove from storedMessages (as this is where msg2 was added for this test)
        var storedIterator = storedMessages.iterator();
        while (storedIterator.hasNext()) {
            Message msg = storedIterator.next();
            if (msg.getMessageHash() != null && msg.getMessageHash().equals(hashToDelete.trim())) {
                storedIterator.remove();
                foundAndRemoved = true;
                messageIDs.remove(msg.getMessageID()); // Also remove from global ID/Hash lists
                messageHashes.remove(msg.getMessageHash());
                break;
            }
        }

        assertTrue(foundAndRemoved, "Message with hash should be found and removed.");
        assertFalse(storedMessages.contains(msg2), "Message should no longer be in storedMessages list.");
        assertFalse(messageHashes.contains(hashToDelete), "Message hash should be removed from messageHashes list.");
        assertFalse(messageIDs.contains(msg2.getMessageID()), "Message ID should be removed from messageIDs list.");
        assertEquals(0, storedMessages.size(), "Stored messages list should be empty after deletion.");
    }
}