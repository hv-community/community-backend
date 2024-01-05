package com.hv.community.backend.service.mail;

import com.hv.community.backend.exception.MailException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

  private final Logger logger = LoggerFactory.getLogger(MailService.class);

  @Autowired
  private final JavaMailSender javaMailSender;

  public void sendEmailV1(String toEmail, String title, String text) {
    SimpleMailMessage emailForm = createEmailFormV1(toEmail, title, text);
    try {
      javaMailSender.send(emailForm);
    } catch (Exception e) {
      logger.debug("MailService.sendEmail exception occur toEmail: {}, " + "title: {}, text: {}",
          toEmail, title, text);
      throw new MailException("MAIL:SEND_MAIL_FAIL");
    }
  }

  public SimpleMailMessage createEmailFormV1(String toEmail, String title, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject(title);
    message.setText(text);
    return message;
  }
}
