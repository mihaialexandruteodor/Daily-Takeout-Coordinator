package com.example.teodormihai.demo.web;

import com.example.teodormihai.demo.dto.DailyViewDto;
import com.example.teodormihai.demo.service.TakeoutService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class TakeoutController {

    private final TakeoutService takeoutService;
    private final CookieHelper cookieHelper;

    public TakeoutController(TakeoutService takeoutService, CookieHelper cookieHelper) {
        this.takeoutService = takeoutService;
        this.cookieHelper = cookieHelper;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/today";
    }

    /**
     * If the user has a valid cookie → show the page normally.
     * If not → still show the page but with needsName=true so the
     * inline name-entry modal appears. No redirect to /join.
     */
    @GetMapping("/today")
    public String today(HttpServletRequest request, Model model) {
        var maybeUser = cookieHelper.readUser(request);
        if (maybeUser.isPresent()) {
            model.addAttribute("view", takeoutService.getDailyView(maybeUser.get(), LocalDate.now()));
            model.addAttribute("needsName", false);
        } else {
            // No cookie — render a blank view so Thymeleaf has something to bind to,
            // and the name-entry modal will be displayed.
            model.addAttribute("view", new DailyViewDto(
                    null, LocalDate.now(), "", null, List.of(), List.of(), List.of()));
            model.addAttribute("needsName", true);
        }
        return "index";
    }

    // /join no longer exists as a page. Anyone hitting it (old links, bookmarks)
    // gets redirected to /today, where the name modal handles onboarding.
    @GetMapping("/join")
    public String joinFormRedirect() {
        return "redirect:/today";
    }

    @PostMapping("/join")
    public String joinSubmitRedirect() {
        return "redirect:/today";
    }

    static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
