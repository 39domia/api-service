package com.service.unit;

import com.google.gson.Gson;
import com.service.api.controller.UserController;
import com.service.api.form.UserForm;
import com.service.api.response.ApiResponse;
import com.service.api.response.PageResponse;
import com.service.dto.UserDTO;
import com.service.model.AppUser;
import com.service.service.TokenService;
import com.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Slf4j
class UserControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private TokenService tokenService;

    private HttpHeaders headers;

    @BeforeAll
    void setUp() {
        headers = new HttpHeaders();
        headers.add("Api-Version", "1.0");
        headers.add("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzeXNhZG1pbiIsInJvbGVzIjpbIlNZU1RFTV9BRE1JTiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgxODEvYXBpL3YxL2F1dGgvbG9naW4iLCJleHAiOjM2MTY3NzA0NjUxMH0.ZonZHyP40b_9zfkBvULjJimXgt8_ZRoKv5hzRjsLa5U");
        headers.add("Content-Type", "application/json");
    }

    @Test
    void getUserList() throws Exception {
        log.info("Testing API get user list");

        List<UserDTO> userDTOs = List.of(new UserDTO("user1", "password"), new UserDTO("user2", "password"));
        PageResponse response = PageResponse.builder()
                .pageNo(1)
                .pageSize(20)
                .total(2)
                .items(userDTOs)
                .build();

        Mockito.when(userService.findAll(1, 20)).thenReturn(response);

        ApiResponse.Payload apiResponse = new ApiResponse.Payload(200, "users", response);
        mockMvc.perform(get("/users").headers(headers))
                .andExpect(status().isOk())
                .andExpect(content().json(new Gson().toJson(apiResponse)));
    }

    @Test
    void getUser() throws Exception {
        log.info("Testing API get user");

        AppUser user = AppUser.builder()
                .id(1l)
                .email("someone@email.com")
                .username("someone")
                .password("password")
                .build();
        Mockito.when(userService.getUserById(1)).thenReturn(user);
        Mockito.when(userService.getByEmail(user.getEmail())).thenReturn(user);

        ApiResponse.Payload apiResponse = new ApiResponse.Payload(200, "user", user);
        mockMvc.perform(get("/users/1").headers(headers))
                .andExpect(status().isOk())
                .andExpect(content().json(new Gson().toJson(apiResponse)));
    }

    @Test
    void createUser() throws Exception {
        log.info("Testing API create user");

        UserForm form = UserForm.builder()
                .email("someone@email.com")
                .username("someone")
                .password("password")
                .build();

        Mockito.when(userService.saveOrUpdate(form)).thenReturn(1l);

        mockMvc.perform(post("/users").headers(headers).content(new Gson().toJson(form)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser() throws Exception {
        log.info("Testing API update user");

        UserForm form = UserForm.builder()
                .id(1l)
                .email("anyone@email.com")
                .username("anyone")
                .password("password")
                .build();

        Mockito.when(userService.saveOrUpdate(form)).thenReturn(1l);

        mockMvc.perform(put("/users")
                        .headers(headers)
                        .content(new Gson().toJson(form)))
                .andExpect(status().isAccepted());
    }

    @Test
    void changPassword() throws Exception {
        log.info("Testing API change password");

        Mockito.when(userService.changePassword(1l, "newPassword")).thenReturn(1l);

        mockMvc.perform(patch("/users/change-password/{id}", 1l)
                        .headers(headers)
                        .param("password", "newPassword"))
                .andExpect(status().isAccepted());
    }

    @Test
    void deleteUser() throws Exception {
        log.info("Testing API delete user");

        Mockito.when(userService.delete(1l)).thenReturn(true);

        mockMvc.perform(delete("/users/{id}", 1l)
                        .headers(headers))
                .andExpect(status().isResetContent());
    }
}
