package com.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class UserDTO implements Serializable {
    private String username;
    private String password;

}
