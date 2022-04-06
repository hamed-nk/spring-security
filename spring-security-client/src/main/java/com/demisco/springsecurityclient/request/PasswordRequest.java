package com.demisco.springsecurityclient.request;

import lombok.Data;

@Data
public class PasswordRequest {
    private String email;
    private String oldPassword;
    private String newPassword;
}
