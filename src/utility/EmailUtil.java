package utility;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import config.PropertiesConfig;

public class EmailUtil {

	public static void sendEmail(String to, String subject, String body) throws MessagingException {
//		1 get properties config for send mail from PropertiesConfig class
		Properties properties = PropertiesConfig.getInstance().getProperties();
		String username = properties.getProperty("mail.username");
		String password = properties.getProperty("mail.password");
		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
//			2 prepare sended mail include : set from mail, setReceipient,setSubject, setText for mail
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setText(body);
//			3. send mail
			Transport.send(message);

		} catch (MessagingException e) {
//			throw a MessingException
			throw e;
		}
	}
}