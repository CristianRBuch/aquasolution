package com.aquasolution.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarRecuperacionContrasena(String destinatario, String token) {
        String link = baseUrl + "/recuperar/reset?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(fromEmail);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Aqua Solution — Recuperación de contraseña");
        mensaje.setText(
                "Hola,\n\n" +
                        "Recibimos una solicitud para restablecer tu contraseña en el sistema de Aqua Solution.\n\n" +
                        "Haz clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
                        link + "\n\n" +
                        "Este enlace expirará en 30 minutos.\n\n" +
                        "Si no solicitaste este cambio, puedes ignorar este correo.\n\n" +
                        "Aqua Solution — Tu mejor opción en agua"
        );
        mailSender.send(mensaje);
    }
}