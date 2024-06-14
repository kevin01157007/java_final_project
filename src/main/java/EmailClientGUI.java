import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.*;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Arrays;
import javax.mail.*;
import java.awt.*;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SearchTerm ;
import java.util.Calendar;
import java.util.Date;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.application.Platform;
import javax.mail.internet.*;
import java.text.SimpleDateFormat;
import com.formdev.flatlaf.FlatLightLaf;

public class EmailClientGUI extends JFrame {
    private JTextField usernameField = new JTextField();
    JTextArea bodyArea = new JTextArea(10, 20);
    private  ArrayList<Message> emailAnalyzeList = new ArrayList<>();
    private JPasswordField passwordField = new JPasswordField();
    private DefaultListModel<String> emailListModel = new DefaultListModel<>();
    private DefaultListModel<String> groupListModel = new DefaultListModel<>();
    private JList<String> emailList = new JList<>(emailListModel);
    private JList<String> groupList = new JList<>(groupListModel);
    private JFXPanel emailContent = new JFXPanel();
    private Message[] messages;
    private int lastEmailCount = 0;
    private JButton AIreplyButton = new JButton("AI回覆");
    private JPanel leftPanel = new JPanel();
    private JPanel groupPanel = new JPanel(new BorderLayout());
    private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    enum ButtonAction {REPLY, FORWARD, DELETE, AI_REPLY, ADD_GROUP, DELETE_GROUP, AI_ANALYZE_GROUP, AI_ANALYZE, DELETE_MAIL_FROM_GROUP, REPLY_ALL}

    public EmailClientGUI() {
        setTitle("GPT analyze email");
        setSize(900, 600);
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

        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if(e.getNewState() == MAXIMIZED_BOTH) {
                    splitPane.setResizeWeight(0.3);
                    splitPane2.setResizeWeight(1);
                }
            }
        });
    }

    private void initUI() {
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        //設定主介面左邊區塊
        leftPanel.setLayout(new BorderLayout());

        //設定信件清單
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.addListSelectionListener(this::emailListSelectionChanged);
        leftPanel.add(new JScrollPane(emailList));

        //設定標題搜尋欄
        JTextField searchField = new JTextField("搜尋郵件");
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("搜尋郵件")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("搜尋郵件");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        searchField.addActionListener(e -> {
            try {
                Message[] searchedMessage = EmailSessionManager.getInstance().searchUser(messages, searchField.getText());
                if (searchedMessage.length == 0) {
                    JOptionPane.showMessageDialog(null, "查無郵件", "查無郵件", JOptionPane.INFORMATION_MESSAGE);
                }
                messages = searchedMessage;
                addEmailToList();
            }catch (MessagingException ex) {ex.printStackTrace();}
        });
        leftPanel.add(searchField, BorderLayout.NORTH);

        //設定信件內容
        Platform.runLater(() -> {
            WebView webView = new WebView();
            emailContent.setScene(new Scene(webView));
        });

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(emailContent);
        splitPane.setDividerLocation(300);
        splitPane.setMinimumSize(new Dimension(1,1));

        //按鈕
        ImageIcon replyAllIcon = new ImageIcon(getClass().getResource("/icons/reply_all.png"));
        ImageIcon replyIcon = new ImageIcon(getClass().getResource("/icons/reply.png"));
        ImageIcon forwardIcon = new ImageIcon(getClass().getResource("/icons/forward.png"));
        ImageIcon deleteIcon = new ImageIcon(getClass().getResource("/icons/delete.png"));
        ImageIcon composeIcon = new ImageIcon(getClass().getResource("/icons/compose.png"));
        ImageIcon refreshIcon = new ImageIcon(getClass().getResource("/icons/refresh.png"));
        ImageIcon aiAnalyzeIcon = new ImageIcon(getClass().getResource("/icons/ai_analyze.png"));
        ImageIcon deleteGroupIcon = new ImageIcon(getClass().getResource("/icons/delete_group.png"));
        ImageIcon addGroupIcon = new ImageIcon(getClass().getResource("/icons/add_group.png"));
        JButton replyAllButton = new JButton(replyAllIcon);
        JButton aiAnalyzeButton = new JButton(aiAnalyzeIcon);
        JButton replyButton = new JButton(replyIcon);
        JButton forwardButton = new JButton(forwardIcon);
        JButton deleteButton = new JButton(deleteIcon);
        JButton composeButton = new JButton(composeIcon);
        JButton refreshInboxButton = new JButton(refreshIcon);
        JButton addGroupButton = new JButton(addGroupIcon);
        JButton deleteGroupButton = new JButton(deleteGroupIcon);
        JButton aiAnalyzeGroupButton = new JButton(aiAnalyzeIcon);
        JButton deleteEmailFromGroupButton = new JButton(deleteIcon);
        replyAllButton.setToolTipText("回覆群組");
        replyButton.setToolTipText("回覆");
        forwardButton.setToolTipText("轉寄");
        deleteButton.setToolTipText("刪除");
        composeButton.setToolTipText("撰寫");
        refreshInboxButton.setToolTipText("刷新");
        aiAnalyzeButton.setToolTipText("AI分析");
        aiAnalyzeGroupButton.setToolTipText("AI群組分析");
        addGroupButton.setToolTipText("加入群組");
        deleteGroupButton.setToolTipText("從群組刪除");
        deleteEmailFromGroupButton.setToolTipText("刪除群組");
        aiAnalyzeGroupButton.addActionListener(e -> prepareEmailAction(ButtonAction.AI_ANALYZE_GROUP));
        addGroupButton.addActionListener(e -> prepareEmailAction(ButtonAction.ADD_GROUP));
        deleteGroupButton.addActionListener(e -> prepareEmailAction(ButtonAction.DELETE_GROUP));
        replyAllButton.addActionListener(e -> prepareEmailAction(ButtonAction.REPLY_ALL));
        aiAnalyzeButton.addActionListener(e -> prepareEmailAction(ButtonAction.AI_ANALYZE));
        AIreplyButton.addActionListener(e -> prepareEmailAction(ButtonAction.AI_REPLY));
        replyButton.addActionListener(e -> prepareEmailAction(ButtonAction.REPLY));
        forwardButton.addActionListener(e -> prepareEmailAction(ButtonAction.FORWARD));
        deleteButton.addActionListener(e -> prepareEmailAction(ButtonAction.DELETE));
        deleteEmailFromGroupButton.addActionListener(e -> prepareEmailAction(ButtonAction.DELETE_MAIL_FROM_GROUP));
        composeButton.addActionListener(e -> showComposeDialog("", "", "", false));
        refreshInboxButton.addActionListener(e -> refreshInbox());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(composeButton);
        actionPanel.add(refreshInboxButton);
        actionPanel.add(deleteButton);
        actionPanel.add(forwardButton);
        actionPanel.add(replyButton);
        actionPanel.add(aiAnalyzeButton);
        add(actionPanel, BorderLayout.NORTH);

        //群組
        groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JPanel groupButtonPanel = new JPanel();
        groupButtonPanel.setLayout(new BoxLayout(groupButtonPanel, BoxLayout.Y_AXIS));
        JPanel groupButtonFlowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        groupButtonPanel.add(addGroupButton);
        groupButtonPanel.add(deleteGroupButton);
        groupButtonPanel.add(replyAllButton);
        groupButtonPanel.add(deleteEmailFromGroupButton);
        groupButtonPanel.add(aiAnalyzeGroupButton);
        groupButtonFlowPanel.add(groupButtonPanel);
        groupPanel.add(groupButtonFlowPanel, BorderLayout.NORTH);
        groupPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);
        splitPane2.setLeftComponent(splitPane);
        splitPane2.setRightComponent(groupPanel);
        splitPane2.setDividerLocation(this.getWidth()-50);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setMinimumSize(new Dimension(1,1));

        //將sp加進去畫面
        getContentPane().add(splitPane2, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::showLoginDialog);
    }

    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        JLabel emailLabel = new JLabel("電子信箱:");
        panel.add(emailLabel);
        panel.add(usernameField);
        JLabel passwordLabel = new JLabel("應用程式密碼:");
        panel.add(passwordLabel);
        panel.add(passwordField);
        try {
            File file = new File("credentials.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("Email:")) {
                    usernameField.setText(line.substring(line.indexOf(":") + 1).trim());
                } else if (line.startsWith("App password:")) {
                    passwordField.setText(line.substring(line.indexOf(":") + 1).trim());
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Credentials file not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        int result = JOptionPane.showConfirmDialog(null, panel, "登入",
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
            addEmailToList();

        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch emails: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEmailToList() {
        emailListModel.clear();
        try {
            for (int i = 0; i < messages.length; i++) {
                String name = (((InternetAddress)(messages[i].getFrom()[0])).getPersonal());
                if (name == null) {
                    String adr = ((InternetAddress)(messages[i].getFrom()[0])).getAddress();
                    name = adr.split("@")[0];
                }
                emailListModel.addElement(name+" - "+messages[i].getSubject()+" - "+((InternetAddress)(messages[i].getFrom()[0])).getAddress());
            }
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch emails: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshInbox(refreshMode refreshMode) {
        if (refreshMode == refreshMode.getMessageFromFolder) {refreshInbox();} //從server抓
        else if (refreshMode == refreshMode.getMessageFromOriginal) { //從message抓
            addEmailToList();
        }
    }


    private void emailListSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && emailList.getSelectedIndex() != -1) {
            new Thread(() -> {
                try {
                    Message selectedMessage = messages[emailList.getSelectedIndex()];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String formattedDate = dateFormat.format(selectedMessage.getSentDate());
                    String content = "Subject: " + selectedMessage.getSubject() + "<br><br>";
                    content += "From: " + InternetAddress.toString(selectedMessage.getFrom()) + "<br><br>";
                    content += "Date: " + formattedDate + "<br><br>";
                    content += getTextFromMessage(selectedMessage);
                    String finalContent = content;
                    SwingUtilities.invokeLater(() -> showHtmlContent(finalContent));
                } catch (MessagingException | IOException ex) {
                    SwingUtilities.invokeLater(() -> showHtmlContent("Error reading email content: " + ex.getMessage()));
                }
            }).start();
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
    
    private String dealBr(String text) {
        return text.replace("\r\n", "<br>").replace("\n", "<br>");
    }
    
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        return getTextFromPart(message);
    }
    
    private String getTextFromPart(Part part) throws MessagingException, IOException {
        boolean hasAttachments = false;
        StringBuilder htmlResult = new StringBuilder();
        StringBuilder plainTextResult = new StringBuilder();
    
        if (part.isMimeType("text/plain")) {
            return dealBr((String) part.getContent());
        } else if (part.isMimeType("text/html")) {
            return (String) part.getContent();
        } else if (part.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) part.getContent();
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/html")) {
                    htmlResult.append((String) bodyPart.getContent());
                } else if (bodyPart.isMimeType("text/plain")) {
                    plainTextResult.append(dealBr((String) bodyPart.getContent()));
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    String recursiveResult = getTextFromPart(bodyPart);
                    if (recursiveResult.toLowerCase().contains("<!doctype html")) {
                        htmlResult.append(recursiveResult);
                    } else {
                        plainTextResult.append(recursiveResult);
                    }
                }
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    hasAttachments = true;
                    executorService.submit(() -> {
                    try {
                        downloadAttachments(bodyPart, "../downloads");
                    } catch (IOException | MessagingException e) {
                        e.printStackTrace();
                    }
                });
                }
                
            }
        }
    
        String result = !htmlResult.toString().isEmpty() ? htmlResult.toString() : plainTextResult.toString();
        if (hasAttachments) {
            result += "<br><br><b>此郵件包含附件。</b>";
        }
        return result;
    }

    private void prepareEmailAction(ButtonAction action) {
        if (emailList.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "未選擇信件!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Message selectedMessage = messages[emailList.getSelectedIndex()];
            switch (action) {
                case ADD_GROUP -> {
                    try {
                        if (!emailAnalyzeList.contains(selectedMessage)) {
                            emailAnalyzeList.add(selectedMessage);
                            String name = (((InternetAddress) (selectedMessage.getFrom()[0])).getPersonal());
                            if (name == null) {
                                String adr = ((InternetAddress)(selectedMessage.getFrom()[0])).getAddress();
                                name = adr.split("@")[0];
                            }
                            groupListModel.addElement(name + "  -  " + selectedMessage.getSubject());
                            JOptionPane.showMessageDialog(this, "郵件已加入分析列表。");
                        } else {
                            JOptionPane.showMessageDialog(this, "這封郵件已被加過了!");
                        }
                    }catch (Exception e) {};
                }
                case DELETE_GROUP -> {
                    int[] selectedIndices = groupList.getSelectedIndices();
                    if (selectedIndices.length == 0) {
                        JOptionPane.showMessageDialog(this, "未選擇信件!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;}
                    // 倒序刪除選中的項目，避免索引問題
                    for (int i = selectedIndices.length - 1; i >= 0; i--) {
                        // 從ArrayList中刪除
                        emailAnalyzeList.remove(selectedIndices[i]);
                        // 從DefaultListModel中刪除
                        groupListModel.removeElementAt(selectedIndices[i]);
                    }
                }
                case DELETE -> {
                    EmailSessionManager.getInstance().deleteEmail(selectedMessage);
                    List<Message> list = new ArrayList<>(Arrays.asList(messages));
                    list.remove(selectedMessage);
                    messages = list.toArray(new Message[list.size()]);
                    refreshInbox(refreshMode.getMessageFromOriginal);
                    Platform.runLater(() -> emailContent.setScene(new Scene(new WebView()))
                    );
                }
                case AI_ANALYZE -> {
                    JDialog dialog = new JDialog();
                    dialog.setTitle("AI分析");
                    dialog.setSize(400, 300);
                    dialog.setLocationRelativeTo(null);
                    JTextArea textArea = new JTextArea();
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    dialog.add(scrollPane);
                    dialog.setVisible(true);

                    new Thread(() -> {
                        try {
                            String messageContent = getTextFromMessage(selectedMessage);
                            AIAnalyze.OpenAIAnalyze(messageContent, 1, response -> {
                                SwingUtilities.invokeLater(() -> textArea.append(response));
                            });
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(() -> textArea.append("Error: " + e.getMessage() + "\n"));
                        }
                    }).start();
                }
                case AI_ANALYZE_GROUP -> {
                    if (emailAnalyzeList.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "信件群組是空的!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JDialog dialog = new JDialog();
                    dialog.setTitle("AI分析");
                    dialog.setSize(400, 300);
                    dialog.setLocationRelativeTo(null);
                    JTextArea textArea = new JTextArea();
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    dialog.add(scrollPane);
                    dialog.setVisible(true);
                    new Thread(() -> {
                        try {
                            String messageContent = "";
                            int i = 1;
                            for (Message email : emailAnalyzeList) {
                                String messageWithoutNewlines = InternetAddress.toString(email.getFrom()).replace("\"", " ");
                                messageContent += i + ".從: " + messageWithoutNewlines + ":";
                                messageContent += getTextFromMessage(email);
                                i++;
                            }
                            AIAnalyze.OpenAIAnalyze(messageContent, 2, response -> {
                                SwingUtilities.invokeLater(() -> textArea.append(response));
                            });
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(() -> textArea.append("Error: " + e.getMessage() + "\n"));
                        }
                    }).start();
                }
                case REPLY -> {
                    String to, subject;
                    to = InternetAddress.toString(selectedMessage.getFrom());
                    subject = "Re: "+selectedMessage.getSubject();
                    try {
                        showComposeDialog(to, subject, "", true);
                    } catch (Exception e) {}
                }
                case AI_REPLY -> {
                    new Thread(() -> {
                        try {
                            String repliedMsg = "<br><br>---Replied Message---<br><br>"+getTextFromMessage(selectedMessage);
                            bodyArea.setText(OpenAIChat.sendOpenAIRequest(getTextFromMessage(selectedMessage)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                case FORWARD -> {
                    try {
                        String forwardMsg = "<br><br>---Forwarded Message---<br><br>"+ getTextFromMessage(selectedMessage);
                        String fwdSubject = "Fwd: " + selectedMessage.getSubject();
                        showComposeDialog("", fwdSubject, forwardMsg, false);
                    } catch (Exception e) {}
                }
                case DELETE_MAIL_FROM_GROUP -> {
                    if (emailAnalyzeList.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "信件群組是空的!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    emailAnalyzeList.clear();
                    groupListModel.removeAllElements();
                }
                case REPLY_ALL -> {
                    if (emailAnalyzeList.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "信件群組是空的!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String to = InternetAddress.toString(emailAnalyzeList.get(0).getFrom());
                    for (int i = 1; i < emailAnalyzeList.size(); i++) {
                        to += ", "+InternetAddress.toString(emailAnalyzeList.get(i).getFrom());
                    }
                    showComposeDialog(to, "Re: ", "", false);
                }
            }
        } catch (MessagingException ex) {
            JOptionPane.showMessageDialog(this, "Error preparing email action.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showComposeDialog(String to, String subject, String body, boolean isReply) {
        JDialog composeDialog = new JDialog(this, "撰寫郵件", true);
        composeDialog.setLayout(new BorderLayout(5, 5));
        composeDialog.setSize(400, 300);
        Box fieldsPanel = Box.createVerticalBox();

        JTextField toField = new JTextField(to);
        JTextField subjectField = new JTextField(subject);
        bodyArea.setText(body);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);

        JLabel toLabel = new JLabel("收件者:");
        fieldsPanel.add(toLabel);
        fieldsPanel.add(toField);
        JLabel subjectLabel = new JLabel("主旨:");
        fieldsPanel.add(subjectLabel);
        fieldsPanel.add(subjectField);

        JPanel bottomPanel = new JPanel();

        JButton attachButton = new JButton("附件");

        JButton sendButton = new JButton("傳送");

        JLabel attachedFilesLabel = new JLabel("沒有附加檔案");
        List<File> attachedFiles = new ArrayList<>();
        attachButton.addActionListener(e -> {
            File[] files = AttachmentChooser.chooseAttachments();
            attachedFiles.addAll(Arrays.asList(files));
            attachedFilesLabel.setText(attachedFiles.size() + " 個檔案");
        });

        sendButton.addActionListener(e -> {
            if(bodyArea.getText().toLowerCase().contains("<!doctype html")){
                EmailSender.sendEmailWithAttachment(toField.getText(), subjectField.getText(), bodyArea.getText(),
                        attachedFiles.toArray(new File[0]));
            }
            else{
                EmailSender.sendEmailWithAttachment(toField.getText(), subjectField.getText(), dealBr(bodyArea.getText()),
                        attachedFiles.toArray(new File[0]));
            }
            composeDialog.dispose();
        });

        bottomPanel.add(attachedFilesLabel);
        bottomPanel.add(attachButton);
        if (isReply) bottomPanel.add(AIreplyButton);
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
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new EmailClientGUI());
    }
}