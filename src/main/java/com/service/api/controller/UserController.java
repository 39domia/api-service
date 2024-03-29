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
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/users")
@Slf4j(topic = "USER_CONTROLLER")
@Tag(name = "User Controller")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(description = "Get all of user information")
    @GetMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse getUserList(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        log.info("Request api GET api/v1/users");

        PageResponse response;
        if (StringUtils.hasLength(search)) {
            response = userService.searchByName(search, pageNo, pageSize);
        } else {
            response = userService.findAll(pageNo, pageSize);
        }
        return new ApiResponse(OK, "users", response);
    }

    @Operation(description = "Get user information")
    @GetMapping(path = "/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse getUser(@PathVariable("id") @Min(1) Long id) throws ResourceNotFoundException {
        log.info("Request api GET api/v1/users/{}", id);
        AppUser user = userService.getUserById(id);
        return new ApiResponse(OK, "user", user);
    }

    @Operation(description = "Create new user")
    // @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'ROLE_MANAGER')")
    @PostMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse createUser(@Valid @RequestBody UserForm form) {
        log.info("Request api POST api/v1/users");

        try {
            long userId = userService.saveOrUpdate(form);
            return new ApiResponse(CREATED, Translator.toLocale("user-add-success"), userId);
        } catch (Exception e) {
            log.error("Can not create user");
            return new ErrorResponse(BAD_REQUEST, Translator.toLocale("user-add-fail"));
        }
    }

    @Operation(description = "Update user information")
    @PutMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse updateUser(@Valid @RequestBody UserForm form) throws ResourceNotFoundException {
        log.info("Request api PUT api/v1/users");

        userService.saveOrUpdate(form);
        return new ApiResponse(ACCEPTED, Translator.toLocale("user-update-success"));
    }

    @Operation(description = "Change password")
    @PatchMapping(path = "/change-password/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse changPassword(@PathVariable("id") @Min(1) Long id, @RequestParam("password") String password) throws ResourceNotFoundException {
        log.info("Request api PATCH api/v1/users/changePassword/{}", id);

        userService.changePassword(id, password);
        return new ApiResponse(ACCEPTED, Translator.toLocale("user-change-password-success"));
    }

    // @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @Operation(description = "Delete user permanently")
    @DeleteMapping(path = "/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse deleteUser(@PathVariable(value = "id") @Min(1) Long id) throws ResourceNotFoundException {
        log.info("Request api DELETE api/v1/users/{}", id);

        userService.delete(id);
        return new ApiResponse(RESET_CONTENT, Translator.toLocale("user-delete-success"));
    }
}
