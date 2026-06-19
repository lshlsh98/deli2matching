package com.example.deli2matching.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Component
public class EmailSender {

    @Autowired
    private JavaMailSender sender;

    public void sendMail(String emailTitle, String receiver, String emailContent) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            // 메일 전송 시간 설정
            helper.setSentDate(new Date());
            // 보내는 사람 정보
            helper.setFrom(new InternetAddress("youngminLee0428@gmail.com", "C2C 관리자"));
            // 받는 사람 정보
            helper.setTo(receiver);
            // 제목 설정
            helper.setSubject(emailTitle);
            // 내용 설정
            helper.setText(emailContent, true); // true: 내용에 html 모드 활성화 시킬지 여부
            //이메일 전송
            sender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }//
}
