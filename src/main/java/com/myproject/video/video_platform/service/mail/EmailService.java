package com.myproject.video.video_platform.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles sending emails.
 */
@Service
@Slf4j
public class EmailService {

    //TODO: implement emailing
    public void sendVerificationEmail(String recipientEmail, String verificationLink) {
        log.info("send verification email with {}", verificationLink);
    }
}
