package com.example.teodormihai.demo.service;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String name) {
        super("Name already taken today: " + name);
    }
}
