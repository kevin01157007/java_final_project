package src;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import src.AIAnalyze;
import javax.mail.internet.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
public class EmailClientGUI extends JFrame {
    private JTextField usernameField = new JTextField();
    private  ArrayList<Message> emailAnalyzeList = new ArrayList<>();
    private JPasswordField passwordField = new JPasswordField();
    private DefaultListModel<String> emailListModel = new DefaultListModel<>();
    private JList<String> emailList = new JList<>(emailListModel);
    private JFXPanel emailContent = new JFXPanel();
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
        //設定主介面左邊區塊
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(BACKGROUND_COLOR);
        //設定信件清單
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.addListSelectionListener(this::emailListSelectionChanged);
        emailList.setFont(EMAIL_LIST_FONT);
        JScrollPane listScrollPane = new JScrollPane(emailList);
        listScrollPane.setBackground(BACKGROUND_COLOR);
        leftPanel.add(new JScrollPane(emailList));
        //設定標題搜尋欄
        JTextField searchSubjectField = new JTextField("搜尋寄件者");
        searchSubjectField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchSubjectField.getText().equals("搜尋寄件者")) {
                    searchSubjectField.setText("");
                    searchSubjectField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchSubjectField.getText().isEmpty()) {
                    searchSubjectField.setText("搜尋寄件者");
                    searchSubjectField.setForeground(Color.GRAY);
                }
            }
        });
        searchSubjectField.setFont(EMAIL_CONTENT_FONT);
        searchSubjectField.addActionListener(e -> {
            try {
                Message[] searchedMessage = EmailSessionManager.getInstance().searchUser(messages, searchSubjectField.getText());
                if (searchedMessage.length == 0) {
                    JOptionPane.showMessageDialog(null, "查無寄件者", "Search No Sender", JOptionPane.INFORMATION_MESSAGE);
                }
                messages = searchedMessage;
                System.out.println(Arrays.toString(messages));
                emailListModel.clear();
                for (int i = messages.length-1; i >= 0; i--) {
                    emailListModel.addElement(messages[i].getSubject() + " - From: " + InternetAddress.toString(messages[i].getFrom()));
                }
            }catch (MessagingException ex) {ex.printStackTrace();}
        });
        leftPanel.add(searchSubjectField, BorderLayout.NORTH);

        //設定信件內容
        Platform.runLater(() -> {
            WebView webView = new WebView();
            emailContent.setScene(new Scene(webView));
        });
        emailContent.setBackground(BACKGROUND_COLOR);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(emailContent);

        getContentPane().setBackground(BACKGROUND_COLOR);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        JButton AIAnalyzeEmail = new JButton("群組分析");
        JButton addEmailButton = new JButton("加入群組");
        JButton removeEmailButton = new JButton("移除群組");
        JButton showEmailButton = new JButton("顯示郵件群組");
        JButton AIAnalyzeButton = new JButton("AIAnalyze");
        JButton AIreplyButton = new JButton("AIReply");
        JButton replyButton = new JButton("Reply");
        JButton forwardButton = new JButton("Forward");
        JButton deleteButton = new JButton("刪除郵件");

        AIAnalyzeEmail.setFont(BUTTON_FONT);
        addEmailButton.setFont(BUTTON_FONT);
        removeEmailButton.setFont(BUTTON_FONT);
        showEmailButton.setFont(BUTTON_FONT);
        AIAnalyzeButton.setFont(BUTTON_FONT);
        AIreplyButton.setFont(BUTTON_FONT);
        replyButton.setFont(BUTTON_FONT);
        forwardButton.setFont(BUTTON_FONT);
        deleteButton.setFont(BUTTON_FONT);
        AIAnalyzeEmail.setBackground(BUTTON_COLOR);
        addEmailButton.setBackground(BUTTON_COLOR);
        removeEmailButton.setBackground(BUTTON_COLOR);
        showEmailButton.setBackground(BUTTON_COLOR);
        AIAnalyzeButton.setBackground(BUTTON_COLOR);
        AIreplyButton.setBackground(BUTTON_COLOR);
        replyButton.setBackground(BUTTON_COLOR);
        forwardButton.setBackground(BUTTON_COLOR);
        deleteButton.setBackground(BUTTON_COLOR);
        AIAnalyzeEmail.addActionListener(e -> prepareEmailAction("AllAnalyze"));
        addEmailButton.addActionListener(e -> prepareEmailAction("addEmail"));
        removeEmailButton.addActionListener(e -> prepareEmailAction("removeEmail"));
        showEmailButton.addActionListener(e -> prepareEmailAction("showEmail"));
        AIAnalyzeButton.addActionListener(e -> prepareEmailAction("AIAnalyze"));
        AIreplyButton.addActionListener(e -> prepareEmailAction("AIReply"));
        replyButton.addActionListener(e -> prepareEmailAction("Reply"));
        forwardButton.addActionListener(e -> prepareEmailAction("Forward"));
        deleteButton.addActionListener(e -> prepareEmailAction("Delete"));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(ACTION_PANEL_COLOR);
        actionPanel.add(AIAnalyzeEmail);
        actionPanel.add(addEmailButton);
        actionPanel.add(removeEmailButton);
        actionPanel.add(showEmailButton);
        actionPanel.add(AIAnalyzeButton);
        actionPanel.add(AIreplyButton);
        actionPanel.add(replyButton);
        actionPanel.add(forwardButton);
        actionPanel.add(deleteButton);

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
                JOptionPane.showMessageDialog(this, "Failed to initialize email session: " +
                                e.getMessage() + "\n請重新登入", "Login Error", JOptionPane.ERROR_MESSAGE);
                showLoginDialog();
            }
        } else {
//            System.out.println("Login cancelled.");
            JOptionPane.showMessageDialog(null, "Login cancelled." ,"information",JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

    private void showHtmlContent(String html) {
        Platform.runLater(() -> {
            WebView webView = (WebView) emailContent.getScene().getRoot();
            webView.getEngine().loadContent(html);
        });
    }

    enum refreshMode {getMessageFromFolder, getMessageFromOriginal}

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
            for (int i = 0; i < messages.length; i++) {
                emailListModel.addElement(messages[i].getSubject() + " - From: " + InternetAddress.toString(messages[i].getFrom()));
            }

        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch emails: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshInbox(refreshMode refreshMode) {
        if (refreshMode == refreshMode.getMessageFromFolder) {refreshInbox();}
        else if (refreshMode == refreshMode.getMessageFromOriginal) {
            emailListModel.clear();
            try {
                for (int i = 0; i < messages.length; i++) {
                    emailListModel.addElement(messages[i].getSubject() + " - From: " + InternetAddress.toString(messages[i].getFrom()));
                }
            } catch (MessagingException e) {
                JOptionPane.showMessageDialog(this, "Failed to fetch emails: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void emailListSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && emailList.getSelectedIndex() != -1) {
            try {
                Message selectedMessage = messages[emailList.getSelectedIndex()];
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String formattedDate = dateFormat.format(selectedMessage.getSentDate());
                String content = "Subject: " + selectedMessage.getSubject() + "<br><br>";
                content += "From: " + InternetAddress.toString(selectedMessage.getFrom()) + "<br><br>";
                content += "Date: " + formattedDate + "<br><br>";
                content += getTextFromMessage(selectedMessage);
                showHtmlContent(content);
            } catch (MessagingException | IOException ex) {
                showHtmlContent("Error reading email content: " + ex.getMessage());
            }
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

    private String convertPlainTextToHtml(String text) {
        String html = text.replace("\n", "<br>");
        return "<html><body>" + html + "</body></html>";
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        return getTextFromPart(message);
    }
    
    private String getTextFromPart(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return convertPlainTextToHtml((String) part.getContent());
        } else if (part.isMimeType("text/html")) {
            return (String) part.getContent(); 
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
            switch (actionType) {
                case "addEmail" -> {
                    Message selectMessage = messages[emailList.getSelectedIndex()];
                    if (!emailAnalyzeList.contains(selectMessage)) {
                        emailAnalyzeList.add(selectMessage);
                        JOptionPane.showMessageDialog(this, "郵件已加入分析列表。");
                    } else {
                        JOptionPane.showMessageDialog(this, "這封郵件已被加過了!");
                    }
                    return;
                }
                case "removeEmail" -> {
                    Message selectMessage = messages[emailList.getSelectedIndex()];
                    if (emailAnalyzeList.contains(selectMessage)) {
                        emailAnalyzeList.remove(selectMessage);
                        JOptionPane.showMessageDialog(this, "郵件已從分析列表中移除。");
                    } else {
                        JOptionPane.showMessageDialog(this, "此郵件不在群組裡。");
                    }
                    return;
                }
                case "showEmail" -> {
                    if (emailAnalyzeList.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "分析列表為空。");
                    } else {
                        String tmp = "";
                        for (Message email : emailAnalyzeList) {
                            tmp += email.getSubject() + " - From: " + InternetAddress.toString(email.getFrom()) + "\n";
                        }
                        JOptionPane.showMessageDialog(this, tmp);
                    }
                    return;
                }
                case "Delete" -> {
                    EmailSessionManager.getInstance().deleteEmail(selectedMessage);
                    List<Message> list = new ArrayList<>(Arrays.asList(messages));
                    list.remove(selectedMessage);
                    messages = list.toArray(new Message[list.size()]);
                    refreshInbox(refreshMode.getMessageFromOriginal);
                    Platform.runLater(() -> emailContent.setScene(new Scene(new WebView()))
                    );
                    return;
                }
<<<<<<< Updated upstream
                case "AIAnalyze" -> {
                    try {
                        String messageContent = getTextFromMessage(selectedMessage);
                        String responseBody = AIAnalyze.OpenAIAnalyze(messageContent,1);
                        showanaly("AIAnalyze", responseBody);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case "AllAnalyze" -> {
                    try {
                        String messageContent = "";
                        int i=1;
                        for (Message email : emailAnalyzeList) {

                            String messageWithoutNewlines = InternetAddress.toString(email.getFrom()).replace("\"", " ");
                            messageContent += i+".從: " + messageWithoutNewlines+":";
                            messageContent += getTextFromMessage(email);
                            i++;
=======
                case "AIAnalyze", "AllAnalyze" -> {
                    new Thread(() -> {
                        try {
                            String messageContent = getTextFromMessage(selectedMessage);
                            if (actionType.equals("AllAnalyze")) {
                                messageContent = "";
                                int i = 1;
                                for (Message email : emailAnalyzeList) {
                                    String messageWithoutNewlines = InternetAddress.toString(email.getFrom()).replace("\"", " ");
                                    messageContent += i + ".從: " + messageWithoutNewlines + ":";
                                    messageContent += getTextFromMessage(email);
                                    i++;
                                }
                            }
                            String responseBody = AIAnalyze.OpenAIAnalyze(messageContent, actionType.equals("AIAnalyze") ? 1 : 2);
                            SwingUtilities.invokeLater(() -> showanaly(actionType, responseBody));
                        } catch (Exception e) {
                            e.printStackTrace();
>>>>>>> Stashed changes
                        }
                    }).start();
                    return;
                }

            }
            String to;
            if (actionType.equals("Reply")||actionType.equals("AIReply")) {
                to = InternetAddress.toString(selectedMessage.getFrom());
            } else {
                to = "";
            }
            String subjectPrefix;
            if (actionType.equals("Reply")||actionType.equals("AIReply")) {
                subjectPrefix = "Re: ";
            } else {
                subjectPrefix = "Fwd: ";
            }
            String subject = subjectPrefix + selectedMessage.getSubject();
            String body = getTextFromMessage(selectedMessage);
            if(actionType.equals("AIReply")){
                new Thread(() -> {
                    try {
                        String responseBody = OpenAIChat.sendOpenAIRequest(body);
                        showComposeDialog(to, subject, responseBody);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

            }else {showComposeDialog(to, subject, body);
            }
        } catch (MessagingException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error preparing email action.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void showanaly(String title,String message) {// 創建 JDialog
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null); // 使對話框居中顯
        // 創建 JTextArea 並設置自動換行
        JTextArea textArea = new JTextArea(message);
        textArea.setLineWrap(true); // 自動換行
        textArea.setWrapStyleWord(true); // 只在單詞邊界換行
        textArea.setEditable(false); // 設置為不可編輯
        // 創建 JScrollPane 並將 JTextArea 添加到其中
        JScrollPane scrollPane = new JScrollPane(textArea);
        // 添加 JScrollPane 到對話框
        dialog.add(scrollPane);

        dialog.setVisible(true); // 顯示對話框
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

        bottomPanel.add(attachedFilesLabel);
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
        Timer timer = new Timer(1000, e -> checkForNewEmails());
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmailClientGUI());
    }
}