package org.userservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${application.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.username:nexo@gmail.com}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification");

            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            String content = "Thank you for registering with our Crypto Lending Platform.\n\n" +
                    "Please verify your email by clicking the link below:\n\n" +
                    verificationUrl + "\n\n" +
                    "If you didn't request this verification, please ignore this email.\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "Regards,\nNexo Crypto Lending Platform Team";

            message.setText(content);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}