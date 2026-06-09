package com.example.teodormihai.demo.web;

import com.example.teodormihai.demo.service.DuplicateNameException;
import com.example.teodormihai.demo.service.TakeoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

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
            // Provide a minimal placeholder view so Thymeleaf doesn't NPE on th:text="${view.currentUser}"
            model.addAttribute("view", takeoutService.getDailyView("", LocalDate.now()));
            model.addAttribute("needsName", true);
        }
        return "index";
    }

    // /join is still available as a standalone page (e.g. direct link)
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("joinForm", new JoinForm());
        return "join";
    }

    @PostMapping("/join")
    public String joinSubmit(@Valid @ModelAttribute JoinForm joinForm,
                             BindingResult result,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        if (result.hasErrors()) {
            return "join";
        }
        try {
            String ip = getClientIp(request);
            var participant = takeoutService.registerParticipant(joinForm.getName(), LocalDate.now(), ip);
            cookieHelper.writeUser(response, participant.getUserName());
            return "redirect:/today";
        } catch (DuplicateNameException e) {
            result.rejectValue("name", "duplicate", "This name is already taken today. Please choose another.");
            return "join";
        }
    }

    static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
