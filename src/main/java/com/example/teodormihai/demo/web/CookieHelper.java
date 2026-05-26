package com.example.teodormihai.demo.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieHelper {

    public static final String COOKIE_NAME = "takeout_user";
    private static final int ONE_YEAR_SECONDS = 365 * 24 * 60 * 60;

    public Optional<String> readUser(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }

    public void writeUser(HttpServletResponse response, String userName) {
        Cookie cookie = new Cookie(COOKIE_NAME, userName);
        cookie.setMaxAge(ONE_YEAR_SECONDS);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
