package com.service.api.controller;

import com.service.api.form.UserForm;
import com.service.api.response.ApiResponse;
import com.service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
class UserControllerTest {
    @Autowired
    private AuthenticationController authenticationController;
    @Autowired
    private UserController userController;

    private Long USER_ID;

    @BeforeAll
    void setUp() {
        log.info("@BeforeAll: Init an user");

        ApiResponse response = userController.createUser(UserForm.builder()
                .email("someone@email.com")
                .username("someone")
                .password("password")
                .build());

        // save user id
        USER_ID = (Long) Objects.requireNonNull(response.getBody()).getData();
    }

    @Test
    void getUserList() {
        log.info("Integrating API get user list");

        ApiResponse response = userController.getUserList("someone", 1, 20);
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getUser() throws ResourceNotFoundException {
        log.info("Integrating API get user");

        ApiResponse response = userController.getUser(USER_ID);
        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateUser() throws ResourceNotFoundException {
        log.info("Integrating API update user");

        ApiResponse apiResponse = userController.updateUser(UserForm.builder().id(USER_ID).email("other@email.com").username("other").build());
        Assertions.assertEquals(200, apiResponse.getStatusCode().value());
    }

    @Test
    void changPassword() throws ResourceNotFoundException {
        log.info("Integrating API change password");

        ApiResponse apiResponse = userController.changPassword(USER_ID, "password");
        Assertions.assertEquals(200, apiResponse.getStatusCode().value());
    }

    @Test
    void deleteUser() throws ResourceNotFoundException {
        log.info("Integrating API delete user");

        ApiResponse apiResponse = userController.deleteUser(USER_ID);
        Assertions.assertEquals(200, apiResponse.getStatusCode().value());
    }
}
