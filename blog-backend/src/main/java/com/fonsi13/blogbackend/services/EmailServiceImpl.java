package com.fonsi13.blogbackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Blog}")
    private String appName;

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Recuperación de contraseña - " + appName);
            helper.setText(buildEmailContent(resetLink), true);

            mailSender.send(message);
            log.info("Email de recuperación enviado a: {}", to);

        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar el email de recuperación");
        }
    }

    private String buildEmailContent(String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0;">%s</h1>
                </div>
                <div style="background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #ddd; border-top: none;">
                    <h2 style="color: #333;">Recuperación de Contraseña</h2>
                    <p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.</p>
                    <p>Si no realizaste esta solicitud, puedes ignorar este correo de forma segura.</p>
                    <p>Para restablecer tu contraseña, haz clic en el siguiente botón:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            Restablecer Contraseña
                        </a>
                    </div>
                    <p style="color: #666; font-size: 14px;">Este enlace expirará en <strong>1 hora</strong> por motivos de seguridad.</p>
                    <p style="color: #666; font-size: 14px;">Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:</p>
                    <p style="background-color: #eee; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 12px;">%s</p>
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Este es un correo automático, por favor no respondas a este mensaje.<br>
                        &copy; 2024 %s. Todos los derechos reservados.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(appName, resetLink, resetLink, appName);
    }
}
