package com.demisco.springsecurityclient.listener;

import com.demisco.springsecurityclient.event.RegistrationCompleteEvent;
import com.demisco.springsecurityclient.model.User;
import com.demisco.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private UserService userService;

    public RegistrationCompleteEventListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token, user);
        String url = event.getApplicationUrl() + "/verifyRegistration?token=" + token;
        log.info("Check the link to verify your account: {}",url);
        //todo create verification token
        //todo send mail to user
    }
}
