package com.myproject.video.video_platform.service.mail;

import com.myproject.video.video_platform.exception.email.EmailSendingException;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Handles sending emails.
 */
@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.mail.from}")
    private String mailFrom;

    public void sendVerificationEmail(String recipientEmail, String verificationLink) {
        Email from = new Email(mailFrom);
        String subject = "Please Verify Your Email";
        Email to = new Email(recipientEmail);
        Content content = new Content("text/plain",
                "Click the following link to verify: " + verificationLink);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody();

            log.info("SendGrid response status: {}", statusCode);
            log.info("SendGrid response body: {}", responseBody);
            log.info("SendGrid response headers: {}", response.getHeaders());

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Email successfully sent to {}", recipientEmail);
            } else {
                String errorMsg = String.format("Failed to send email. Status: %d, Body: %s", statusCode, responseBody);
                log.error(errorMsg);
                throw new EmailSendingException(errorMsg);
            }
        } catch (IOException ex) {
            String errorMsg = "Error sending email with SendGrid: " + ex.getMessage();
            log.error(errorMsg, ex);
            throw new EmailSendingException(errorMsg, ex);
        }
    }

}
