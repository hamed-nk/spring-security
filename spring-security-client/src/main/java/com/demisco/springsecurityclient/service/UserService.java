package com.demisco.springsecurityclient.service;

import com.demisco.springsecurityclient.model.User;
import com.demisco.springsecurityclient.model.VerificationToken;
import com.demisco.springsecurityclient.request.UserRequest;

import java.util.Optional;

public interface UserService {
    User register(UserRequest request);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkValidOldPassword(User user, String oldPassword);

}
