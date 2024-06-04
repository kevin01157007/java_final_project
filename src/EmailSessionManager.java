package src;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;
import java.util.*;

public class EmailSessionManager {
    private Session emailSession;
    private Store store;
    private Folder emailFolder;
    private static EmailSessionManager instance;

    private static String currentUsername = "";
    private static String currentPassword = "";

    private EmailSessionManager(String username, String password) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.ssl.enable", "true");
        this.emailSession = Session.getInstance(properties, null);
        this.store = emailSession.getStore("imaps");
        this.store.connect(username, password);

        currentUsername = username;
        currentPassword = password;
    }

    public static EmailSessionManager getInstance(String username, String password) throws MessagingException {
        if (instance == null) {
            instance = new EmailSessionManager(username, password);
        }
        return instance;
    }

    public static EmailSessionManager getInstance() throws IllegalStateException {
        if (instance == null) {
            throw new IllegalStateException("EmailSessionManager is not initialized. Please login first.");

        }
        return instance;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static String getPassword() {
        return currentPassword;
    }

    public int getTotalEmailCount() throws MessagingException {
        if (emailFolder == null || !emailFolder.isOpen()) {
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);
        }
        return emailFolder.getMessageCount();
    }

    public Message[] searchEmail(SearchTerm searchTerm) throws MessagingException {
        if (emailFolder == null || !emailFolder.isOpen()) {
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);
        }
        Message[] messages = emailFolder.search(searchTerm);
        List<Message> messageList = Arrays.asList(messages);
        Collections.reverse(messageList);
        return messageList.toArray(new Message[0]);
    }

    public Message[] searchUser(Message[] messages, String keyword) throws MessagingException {

        ArrayList<Message> searchedMessages = new ArrayList<Message>();
        int idx = 0;
        for (int i = 0; i < messages.length; i++) {
            InternetAddress internetAddress = (InternetAddress) messages[i].getFrom()[0];
            String personal = internetAddress.getPersonal();
            if (personal != null && personal.toLowerCase().contains(keyword.toLowerCase()))
                searchedMessages.add(messages[i]);
        }
        for (int i = 0; i < messages.length; i++) {
            String subject = messages[i].getSubject();
            if (subject != null && subject.toLowerCase().contains(keyword.toLowerCase()))
                searchedMessages.add(messages[i]);
        }
        return searchedMessages.toArray(new Message[0]);
    }

    public void deleteEmail(Message message) throws MessagingException {
        if (emailFolder == null || !emailFolder.isOpen()) {
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);
        } else if (emailFolder.getMode() != Folder.READ_WRITE) {
            emailFolder.close(false);
            emailFolder.open(Folder.READ_WRITE);
        }

        message.setFlag(Flags.Flag.DELETED, true);
    }

//    public void deleteMailFromGroup(Message[] messages, ArrayList<Message> deletedMessages) throws MessagingException {
//        for (int j = 0; j < deletedMessages.size(); j++) {
//            for (int i = 0; i < messages.length; i++) {
//                if (messages[i].getSubject().equals(deletedMessages.get(j).getSubject())){
//                    deleteEmail(messages[i]);
//                    //System.out.println("Deleted message from " + deletedMessages.get(j).getSubject());
//                }
//            }
//        }
//    }
//public void deleteMailFromGroup(Message[] messages, ArrayList<Message> deletedMessages) throws MessagingException {
//    for (int j = 0; j < deletedMessages.size(); j++) {
//        for (int i = 0; i < messages.length; i++) {
//            if (messages[i].getSubject().equals(deletedMessages.get(j).getSubject())){
//                deleteEmail(messages[i]);
//                //System.out.println("Deleted message from " + deletedMessages.get(j).getSubject());
//            }
//        }
//    }
//}

    public void close() throws MessagingException {
        if (emailFolder != null) {
            emailFolder.close(false);
            emailFolder = null;
        }
        if (store != null) {
            store.close();
            store = null;
        }
        instance = null;
        currentUsername = "";
        currentPassword = "";
    }
}
