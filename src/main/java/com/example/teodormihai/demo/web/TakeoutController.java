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

    @GetMapping("/today")
    public String today(HttpServletRequest request, Model model) {
        return cookieHelper.readUser(request).map(userName -> {
            model.addAttribute("view", takeoutService.getDailyView(userName, LocalDate.now()));
            return "index";
        }).orElse("redirect:/join");
    }

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
            var participant = takeoutService.registerParticipant(joinForm.getName(), LocalDate.now());
            cookieHelper.writeUser(response, participant.getUserName());
            return "redirect:/today";
        } catch (DuplicateNameException e) {
            result.rejectValue("name", "duplicate", "This name is already taken today. Please choose another.");
            return "join";
        }
    }
}
