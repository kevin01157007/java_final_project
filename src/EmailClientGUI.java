import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import java.awt.*;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.ComparisonTerm;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.mail.search.SearchTerm ;
import java.util.Calendar;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.application.Platform;
import javax.mail.internet.*;
public class EmailClientGUI extends JFrame {
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private DefaultListModel<String> emailListModel = new DefaultListModel<>();
    private JList<String> emailList = new JList<>(emailListModel);
    private JTextArea emailContent = new JTextArea();
    private Message[] messages;
    private int lastEmailCount = 0;
    public EmailClientGUI() {
        setTitle("Java Email Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (EmailSessionManager.getInstance() != null) {
                        EmailSessionManager.getInstance().close(); // Close the email session
                    }
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static final Color BACKGROUND_COLOR = new Color(230, 240, 250);
    private static final Color ACTION_PANEL_COLOR = new Color(200, 220, 240);
    private static final Color BUTTON_COLOR = new Color(180, 220, 240);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font EMAIL_LIST_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font EMAIL_CONTENT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private void initUI() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);

        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.addListSelectionListener(this::emailListSelectionChanged);
        emailList.setFont(EMAIL_LIST_FONT);
        JScrollPane listScrollPane = new JScrollPane(emailList);
        listScrollPane.setBackground(BACKGROUND_COLOR);

        emailContent.setEditable(false);
        emailContent.setFont(EMAIL_CONTENT_FONT);
        JScrollPane contentScrollPane = new JScrollPane(emailContent);
        contentScrollPane.setBackground(BACKGROUND_COLOR);

        splitPane.setLeftComponent(listScrollPane);
        splitPane.setRightComponent(contentScrollPane);

        getContentPane().setBackground(BACKGROUND_COLOR);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        JButton replyButton = new JButton("Reply");
        JButton forwardButton = new JButton("Forward");
        replyButton.setFont(BUTTON_FONT);
        forwardButton.setFont(BUTTON_FONT);
        replyButton.setBackground(BUTTON_COLOR);
        forwardButton.setBackground(BUTTON_COLOR);

        replyButton.addActionListener(e -> prepareEmailAction("Reply"));
        forwardButton.addActionListener(e -> prepareEmailAction("Forward"));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(ACTION_PANEL_COLOR);
        actionPanel.add(replyButton);
        actionPanel.add(forwardButton);

        add(actionPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.setBackground(ACTION_PANEL_COLOR);
        JButton composeButton = new JButton("Compose");
        JButton refreshInboxButton = new JButton("Refresh Inbox");
        composeButton.setBackground(BUTTON_COLOR);
        refreshInboxButton.setBackground(BUTTON_COLOR);
        composeButton.setFont(BUTTON_FONT);
        refreshInboxButton.setFont(BUTTON_FONT);

        composeButton.addActionListener(e -> showComposeDialog("", "", ""));
        refreshInboxButton.addActionListener(e -> refreshInbox());
        bottomPanel.add(composeButton);
        bottomPanel.add(refreshInboxButton);
        add(bottomPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::showLoginDialog);
    }

    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(BUTTON_FONT);
        panel.add(emailLabel);
        usernameField.setFont(EMAIL_LIST_FONT);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("App Password:");
        passwordLabel.setFont(BUTTON_FONT);
        panel.add(passwordLabel);
        passwordField.setFont(EMAIL_LIST_FONT);
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            try {
                EmailSessionManager.getInstance(username, password);
                refreshInbox();
                lastEmailCount = EmailSessionManager.getInstance().getTotalEmailCount();
                startEmailCheckTimer();
            } catch (MessagingException e) {
                JOptionPane.showMessageDialog(this, "Failed to initialize email session: " + e.getMessage(),
                        "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("Login cancelled.");
        }
    }
    private void showHtmlContent(String html) {
        JDialog htmlDialog = new JDialog(this, "HTML Content", true);
        htmlDialog.setSize(600, 400);
        htmlDialog.setLocationRelativeTo(this);

        JFXPanel jfxPanel = new JFXPanel();
        htmlDialog.add(jfxPanel, BorderLayout.CENTER);
        
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webView.getEngine().loadContent(html);
    
            Scene scene = new Scene(webView);
            jfxPanel.setScene(scene);
        });
    
        htmlDialog.setVisible(true);
    }
    
    private void refreshInbox() {
        try {
            // Get the current date
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -7); // Subtract 7 days
            Date oneWeekAgo = cal.getTime();

            // Create a search term for messages received after one week ago
            SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GT, oneWeekAgo);

            messages = EmailSessionManager.getInstance().searchEmail(searchTerm);
            emailListModel.clear();
            for (Message message : messages) {
                emailListModel.addElement(message.getSubject() + " - From: " + InternetAddress.toString(message.getFrom()));
            }
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch emails: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void emailListSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && emailList.getSelectedIndex() != -1) {
            try {
                Message selectedMessage = messages[emailList.getSelectedIndex()];
                emailContent.setText("");
                emailContent.append("Subject: " + selectedMessage.getSubject() + "\n\n");
                emailContent.append("From: " + InternetAddress.toString(selectedMessage.getFrom()) + "\n\n");
                emailContent.append(getTextFromMessage(selectedMessage));
            } catch (MessagingException | IOException ex) {
                emailContent.setText("Error reading email content: " + ex.getMessage());
            }
        }
    }
    public List<String> downloadAttachments(Part part, String saveDirectory) throws IOException, MessagingException {
        List<String> downloadedAttachments = new ArrayList<>();
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
        if (!downloadedAttachments.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "附件已被下載到: " + saveDirectory,
                                              "下載成功", JOptionPane.INFORMATION_MESSAGE);
            });
        }
        return downloadedAttachments;
    }
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        return getTextFromPart(message);
    }
    
    private String getTextFromPart(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        } else if (part.isMimeType("text/html")) {
            String html = (String) part.getContent();
            showHtmlContent(html);
            return "HTML Content";
        } else if (part.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) part.getContent();
            String result = "";
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                downloadAttachments(bodyPart,"../java_final_project/downloads");
                String partText = getTextFromPart(bodyPart);
                if (partText != null && !partText.isEmpty()) {
                    result += partText + "\n";
                }
            }
            return result.trim();
        }
        return "";
    }

    private void prepareEmailAction(String actionType) {
        if (emailList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "No email selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Message selectedMessage = messages[emailList.getSelectedIndex()];
            String to = actionType.equals("Reply") ? InternetAddress.toString(selectedMessage.getFrom()) : "";
            String subjectPrefix = actionType.equals("Reply") ? "Re: " : "Fwd: ";
            String subject = subjectPrefix + selectedMessage.getSubject();
            String body = getTextFromMessage(selectedMessage);

            showComposeDialog(to, subject, body);
        } catch (MessagingException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error preparing email action.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showComposeDialog(String to, String subject, String body) {
        JDialog composeDialog = new JDialog(this, "Compose Email", true);
        composeDialog.setLayout(new BorderLayout(5, 5));
        composeDialog.getContentPane().setBackground(BACKGROUND_COLOR);

        Box fieldsPanel = Box.createVerticalBox();
        fieldsPanel.setBackground(BACKGROUND_COLOR);

        JTextField toField = new JTextField(to);
        toField.setFont(EMAIL_CONTENT_FONT);
        JTextField subjectField = new JTextField(subject);
        subjectField.setFont(EMAIL_CONTENT_FONT);
        JTextArea bodyArea = new JTextArea(10, 20);
        bodyArea.setText(body);
        bodyArea.setFont(EMAIL_CONTENT_FONT);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(BUTTON_FONT);
        fieldsPanel.add(toLabel);
        fieldsPanel.add(toField);
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setFont(BUTTON_FONT);
        fieldsPanel.add(subjectLabel);
        fieldsPanel.add(subjectField);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(ACTION_PANEL_COLOR);

        JButton attachButton = new JButton("Attach Files");
        attachButton.setFont(BUTTON_FONT);
        attachButton.setBackground(BUTTON_COLOR);

        JButton sendButton = new JButton("Send");
        sendButton.setFont(BUTTON_FONT);
        sendButton.setBackground(BUTTON_COLOR);

        JLabel attachedFilesLabel = new JLabel("No files attached");
        attachedFilesLabel.setFont(BUTTON_FONT);

        List<File> attachedFiles = new ArrayList<>();
        attachButton.addActionListener(e -> {
            File[] files = AttachmentChooser.chooseAttachments();
            attachedFiles.addAll(Arrays.asList(files));
            attachedFilesLabel.setText(attachedFiles.size() + " files attached");
        });

        sendButton.addActionListener(e -> {
            EmailSender.sendEmailWithAttachment(toField.getText(), subjectField.getText(), bodyArea.getText(),
                    attachedFiles.toArray(new File[0]));
            composeDialog.dispose();
        });

        bottomPanel.add(attachButton);
        bottomPanel.add(sendButton);

        composeDialog.add(fieldsPanel, BorderLayout.NORTH);
        composeDialog.add(new JScrollPane(bodyArea), BorderLayout.CENTER);
        composeDialog.add(bottomPanel, BorderLayout.SOUTH);

        composeDialog.pack();
        composeDialog.setLocationRelativeTo(this);
        composeDialog.setVisible(true);
    }
    private void checkForNewEmails() {
        try {
            int currentEmailCount = EmailSessionManager.getInstance().getTotalEmailCount();
            if (currentEmailCount > lastEmailCount) {
                JOptionPane.showMessageDialog(this, "You have new emails!", "New Email", JOptionPane.INFORMATION_MESSAGE);
                refreshInbox(); // Optionally refresh inbox
                lastEmailCount = currentEmailCount; // 更新邮件总数
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    private void startEmailCheckTimer() {
        Timer timer = new Timer(1000, e -> checkForNewEmails()); // 每5分钟检查一次
        timer.start();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmailClientGUI());
    }
}