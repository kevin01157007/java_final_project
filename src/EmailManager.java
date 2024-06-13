import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmailManager {
    private static EmailClientGUI parent;
    private static Message[] messages;

    public static Message[] getMessages() {return messages;}

    public static Message[] refreshInbox() {
        try {
            // Get the current date
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -7); // Subtract 7 days
            Date oneWeekAgo = cal.getTime();

            // Create a search term for messages received after one week ago
            SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GT, oneWeekAgo);

            messages = EmailSessionManager.getInstance().searchEmail(searchTerm);
            return messages;

        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(parent, "Failed to fetch emails: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public List<String> downloadAttachments(Part part, String saveDirectory) throws IOException, MessagingException {
        List<String> downloadedAttachments = new ArrayList<>();
        File directory = new File(saveDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multiPart = (Multipart) part.getContent();
            int numberOfParts = multiPart.getCount();
            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                BodyPart bodyPart = multiPart.getBodyPart(partCount);
                downloadedAttachments.addAll(downloadAttachments(bodyPart, saveDirectory));
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            String fileName = part.getFileName();
            if (fileName != null) {
                File saveFile = new File(saveDirectory, fileName);
                ((MimeBodyPart) part).saveFile(saveFile);
                downloadedAttachments.add(saveFile.getAbsolutePath());
            }
        }
        return downloadedAttachments;

    }

    private String dealBr(String text) {return text.replace("\r\n", "<br>").replace("\n", "<br>");}

    private String getTextFromMessage(Message message) throws MessagingException, IOException {return getTextFromPart(message);}
    private String getTextFromPart(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return dealBr((String) part.getContent());
        } else if (part.isMimeType("text/html")) {
            return (String) part.getContent();
        } else if (part.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) part.getContent();
            String htmlResult = "";
            String plainTextResult = "";
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/html") && htmlResult.isEmpty()) {
                    htmlResult = (String) bodyPart.getContent();
                } else if (bodyPart.isMimeType("text/plain") && plainTextResult.isEmpty()) {
                    plainTextResult = dealBr((String) bodyPart.getContent());
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    String recursiveResult = getTextFromPart(bodyPart);
                    if (recursiveResult.contains("<!DOCTYPE html") && htmlResult.isEmpty()) {
                        htmlResult = recursiveResult;
                    } else if (plainTextResult.isEmpty()) {
                        plainTextResult = recursiveResult;
                    }
                }
                executorService.submit(() -> {
                    try {
                        downloadAttachments(bodyPart, "../downloads");
                    } catch (IOException | MessagingException e) {
                        e.printStackTrace();
                    }
                });
            }
            return !htmlResult.isEmpty() ? htmlResult : plainTextResult; // 优先返回 HTML 内容
        }
        return "";
    }

    private void checkForNewEmails() {
        try {
            int currentEmailCount = EmailSessionManager.getInstance().getTotalEmailCount();
            if (currentEmailCount > lastEmailCount) {
                JOptionPane.showMessageDialog(this, "You have new emails!", "New Email", JOptionPane.INFORMATION_MESSAGE);
                EmailClientGUI.addEmailtoList refreshInbox(); // Optionally refresh inbox
                lastEmailCount = currentEmailCount; // 更新邮件总数
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void startEmailCheckTimer() {
        Timer timer = new Timer(1000, e -> checkForNewEmails());
        timer.start();
    }
}
