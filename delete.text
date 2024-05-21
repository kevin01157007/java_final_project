Folder emailFolder = store.getFolder("INBOX");
emailFolder.open(Folder.READ_WRITE);

Message[] messages = emailFolder.getMessage();
Message message;
message.setFlag(Flags.Flag.DELETED, true); //這個函式可以直接刪掉信件