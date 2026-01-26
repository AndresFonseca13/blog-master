package com.fonsi13.blogbackend.services;

public interface EmailService {

    void sendPasswordResetEmail(String to, String resetLink);
}
