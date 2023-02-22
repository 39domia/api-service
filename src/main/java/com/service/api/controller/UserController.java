package com.service.api.controller;


import com.service.api.form.UserForm;
import com.service.api.response.ApiResponse;
import com.service.api.response.ErrorResponse;
import com.service.api.response.PageResponse;
import com.service.config.Translator;
import com.service.exception.ResourceNotFoundException;
import com.service.model.AppUser;
import com.service.service.UserService;
import com.service.util.ApiConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Date;

@RestController
@RequestMapping("/users")
@Slf4j(topic = "USER_CONTROLLER")
@Tag(name = "User Controller")
@Validated
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder encoder;

    @Operation(description = "Get all of user information")
    @GetMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse getUserList(
            @RequestParam(name = "search", required = false) String _search,
            @RequestParam(name = "pageNo", defaultValue = "1") int _pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") int _pageSize) {
        log.info("Request api GET api/v1/users");

        PageResponse response;
        if (StringUtils.hasLength(_search)) {
            response = userService.searchByName(_search, _pageNo, _pageSize);
        } else {
            response = userService.findAll(_pageNo, _pageSize);
        }
        return new ApiResponse(HttpStatus.OK.value(), "users", response);
    }

    @Operation(description = "Get user information")
    @GetMapping(path = "/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse getUser(@PathVariable("id") @Min(1) Long _id) throws ResourceNotFoundException {
        log.info("Request api GET api/v1/users/{}", _id);
        AppUser user = userService.getUserById(_id);
        return new ApiResponse(HttpStatus.OK.value(), "user", user);
    }

    @Operation(description = "Create new user")
    // @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'ROLE_MANAGER')")
    @PostMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse createUser(@Valid @RequestBody UserForm form) {
        log.info("Request api POST api/v1/users");

        AppUser user = AppUser.builder()
                .email(form.getEmail())
                .username(form.getUsername())
                .password(encoder.encode(form.getPassword()))
                .createdDate(new Date())
                .build();

        try {
            userService.save(user);
            return new ApiResponse(HttpStatus.CREATED.value(), Translator.toLocale("user-add-success"), user.getId());
        } catch (Exception e) {
            log.error("Can not create user");
            return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), Translator.toLocale("user-add-fail"));
        }
    }

    @Operation(description = "Update user information")
    @PutMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse updateUser(@Valid @RequestBody UserForm form) throws ResourceNotFoundException {
        log.info("Request api PUT api/v1/users/{}", form.getId());

        AppUser user = userService.getUserById(form.getId());
        user.setUsername(form.getUsername());
        userService.save(user);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("user-update-success"));
    }

    // @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(description = "Delete user permanently")
    @DeleteMapping(path = "/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse deleteUser(@PathVariable(value = "id") @Min(1) Long _id) throws ResourceNotFoundException {
        log.info("Request api DELETE api/v1/users/{}", _id);

        userService.delete(_id);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("user-delete-success"));
    }

    @Operation(description = "Change password")
    @PatchMapping(path = "/change-password/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse changPassword(@PathVariable("id") @Min(1) Long _id, @RequestParam("password") String password) throws ResourceNotFoundException {
        log.info("Request api PATCH api/v1/users/changePassword/{}", _id);

        AppUser user = userService.getUserById(_id);
        user.setPassword(encoder.encode(password));
        userService.save(user);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("user-change-password-success"));
    }

}
