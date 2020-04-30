package sample;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Properties;

public class JavaMailUtil {
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "465";
    public static final String USER = LoginController.EMAIL;
    public static final String PASSWD = LoginController.PASSWD;

    static boolean sendEmail(String recepient, String subject, String content, String filePath) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", PORT);
        props.put("mail.smtp.socketFactory.fallback", "false");
        Session mailSession = Session.getDefaultInstance(props, null);
        mailSession.setDebug(true);
        Message mailMessage = new MimeMessage(mailSession);
        try {
            mailMessage.setFrom(new InternetAddress(USER));
            mailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
            mailMessage.setSubject(subject);
            Multipart fileContent = new MimeMultipart();
            MimeBodyPart textContent = new MimeBodyPart();
            textContent.setText(content);
            MimeBodyPart fileBodyPart = new MimeBodyPart();
            if (filePath != null) {
                fileBodyPart.attachFile(filePath);
                fileContent.addBodyPart(fileBodyPart);
            } else {
                System.out.println("nothing selected ! warning");
            }
            fileContent.addBodyPart(textContent);
            mailMessage.setContent(fileContent);
            Transport transport = mailSession.getTransport("smtp");
            transport.connect(HOST, USER, PASSWD);
            transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
            return true;
        } catch (MessagingException | IOException e) {
            System.out.println(e.getCause());
            return false;
        }
    }
}