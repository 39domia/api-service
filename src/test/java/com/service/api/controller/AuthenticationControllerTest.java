package com.service.api.controller;

import com.service.api.response.TokenResponse;
import com.service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
class AuthenticationControllerTest {
    @Autowired
    private AuthenticationController controller;
    private MockHttpServletRequest request;
    private String refreshToken;

    @Test
    @BeforeAll
    void login() {
        log.info("@BeforeAll: Login and save refresh token");

        request = new MockHttpServletRequest();
        ResponseEntity<TokenResponse> apiResponse = controller.login(request, "sysadmin", "password");
        Assertions.assertEquals(200, apiResponse.getStatusCode().value());

        // save refresh token
        refreshToken = "Bearer " + Objects.requireNonNull(apiResponse.getBody()).getRefresh_token();
    }

    @Test
    void refreshToken() throws ResourceNotFoundException {
        request.addHeader(HttpHeaders.AUTHORIZATION, refreshToken);
        ResponseEntity<TokenResponse> apiResponse = controller.refreshToken(request);
        Assertions.assertEquals(200, apiResponse.getStatusCode().value());
    }

    @Test
    void forgotPassword() throws ResourceNotFoundException {
        // Todo: Need to config mail server before test
//        ApiResponse apiResponse = controller.forgotPassword("someone@email.com");
//        Assertions.assertEquals(202, apiResponse.getStatusCode().value());
    }

    @Test
    void resetPassword() throws ResourceNotFoundException {
        // Todo: Get reset token from email before testing
//        ApiResponse apiResponse = controller.resetPassword("reset-token", "newPassword");
//        Assertions.assertEquals(200, apiResponse.getStatusCode().value());
    }
}