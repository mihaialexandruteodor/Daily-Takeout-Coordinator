package com.example.teodormihai.demo.web;

import jakarta.validation.constraints.NotBlank;

public class JoinForm {

    @NotBlank(message = "Name must not be blank")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
