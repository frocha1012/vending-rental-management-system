package pt.ipvc.vending.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an account-approved notification to the new client.
     *
     * @return true if the email was sent successfully, false on any mail error.
     */
    public boolean sendAccountApprovedEmail(String to, String nome, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Conta aprovada - Vending Rental");
        message.setText(buildApprovedBody(nome, username));

        try {
            mailSender.send(message);
            log.info("Account-approved email sent to {}", to);
            return true;
        } catch (MailException ex) {
            log.warn("Failed to send account-approved email to {}: {}", to, ex.getMessage());
            return false;
        }
    }

    /**
     * Sends a password-reset link to the client's email address.
     *
     * @return true if sent successfully, false on any mail error.
     */
    public boolean sendPasswordResetEmail(String to, String nome, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Redefinição de password - Vending Rental");
        message.setText(buildPasswordResetBody(nome, token));

        try {
            mailSender.send(message);
            log.info("Password-reset email sent to {}", to);
            return true;
        } catch (MailException ex) {
            log.warn("Failed to send password-reset email to {}: {}", to, ex.getMessage());
            return false;
        }
    }

    private String buildPasswordResetBody(String nome, String token) {
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        return """
                Olá, %s,

                Recebemos um pedido de redefinição de password para a sua conta no portal Vending Rental.

                Clique no link abaixo para definir uma nova password:
                  %s

                Este link é válido durante 30 minutos.

                Se não solicitou esta alteração, ignore este email — a sua password não será modificada.

                Cumprimentos,
                Equipa Vending Rental
                """.formatted(nome, resetLink);
    }

    private String buildApprovedBody(String nome, String username) {
        return """
                Olá, %s,

                A sua conta de cliente no portal Vending Rental foi aprovada com sucesso!

                Os seus dados de acesso são:
                  Username: %s

                Pode iniciar sessão no portal do cliente em:
                  http://localhost:8080/login

                Se não solicitou esta conta, por favor contacte-nos imediatamente.

                Cumprimentos,
                Equipa Vending Rental
                """.formatted(nome, username);
    }
}
