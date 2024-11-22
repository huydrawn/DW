package utility;

import javax.mail.*;
import javax.mail.internet.*;

import config.PropertiesConfig;

import java.util.Properties;

public class EmailUtil {

	public static void sendEmail(String to, String subject, String body) {
		Properties properties = PropertiesConfig.getInstance().getProperties();
		String username = properties.getProperty("mail.username");
		String password = properties.getProperty("mail.password");
		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setText(body);

			Transport.send(message);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}