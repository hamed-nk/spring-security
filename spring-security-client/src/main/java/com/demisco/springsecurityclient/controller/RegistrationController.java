package com.demisco.springsecurityclient.controller;

import com.demisco.springsecurityclient.event.RegistrationCompleteEvent;
import com.demisco.springsecurityclient.model.User;
import com.demisco.springsecurityclient.model.VerificationToken;
import com.demisco.springsecurityclient.request.PasswordRequest;
import com.demisco.springsecurityclient.request.UserRequest;
import com.demisco.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {
    private UserService userService;
    private ApplicationEventPublisher publisher;

    public RegistrationController(UserService userService, ApplicationEventPublisher publisher) {
        this.userService = userService;
        this.publisher = publisher;
    }

    @PostMapping("/register")
    public String register(@RequestBody UserRequest request, final HttpServletRequest servletRequest) {
        User user = userService.register(request);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(servletRequest)
        ));
        return "Success";
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if (result.equalsIgnoreCase("valid")) {
            return "User Verifies Success";
        }
        return "Bad User";

    }

    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) {
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "verification link sent";
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url = applicationUrl + "/verifyRegistration?token=" + verificationToken.getToken();
        log.info("Click the link to verify your account: {}", url);
    }

    private String applicationUrl(HttpServletRequest servletRequest) {
        return "http://" +
                servletRequest.getServerName() +
                ":" +
                servletRequest.getServerPort() +
                servletRequest.getContextPath();
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordRequest request, HttpServletRequest servletRequest) {
        User user = userService.findByEmail(request.getEmail());
        String url = "";
        if (user != null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, applicationUrl(servletRequest), token);
        }
        return url;
    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl + "/savePassword?token=" + token;
        log.info("Click the link to Reset your Password: {}", url);
        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordRequest request) {
        String result = userService.validatePasswordResetToken(token);
        if (!result.equalsIgnoreCase("valid")) {
            return "invalid token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if (user.isPresent()) {
            userService.changePassword(user.get(), request.getNewPassword());
            return "password reset success";
        } else {
            return "invalid token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if(!userService.checkValidOldPassword(user, request.getOldPassword())){
            return "Invalid Old Password";
        }

        userService.changePassword(user, request.getNewPassword());
        return "Changed Password";
    }
}
