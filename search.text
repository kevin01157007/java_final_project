public void search(String username) {
    try {
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        for (Message msg : messages) {
            Address[] fromAddresses = msg.getFrom();
            boolean find = false;
            for (Address addr : fromAddresses) {
                InternetAddress internetAddress = (InternetAddress) addr;
                String personal = internetAddress.getPersonal();
                if (personal != null&&personal.equals(username)) {
                    find = true;
                    break;
                }
            }
            if (find) {
                //印出內容(msg);
            }
        }
        inbox.close(false);
    } catch (MessagingException e) {
        e.printStackTrace();
    } catch (IOException e) {
        System.out.println("IO異常處理: " + e.getMessage());
        e.printStackTrace();
    }
}

