import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EmailReceiver {

    public static Message[] receiveEmail() throws MessagingException {
        EmailSessionManager manager = EmailSessionManager.getInstance();
        Message[] messages = manager.receiveEmail();
        List<Message> messageList = Arrays.asList(messages);
        Collections.reverse(messageList);
        System.out.println(messageList);
        return messageList.toArray(new Message[0]);
    }
}