package com.example.sample.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileDTO implements Serializable {
    private String name;
    private String url;
}
