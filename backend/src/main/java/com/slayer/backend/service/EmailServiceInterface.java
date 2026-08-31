package com.slayer.backend.service;

public interface EmailServiceInterface {

    void sendMail(String to, String subject, String content);
}
