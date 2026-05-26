package com.example.teodormihai.demo.web;

import com.example.teodormihai.demo.dto.DailyViewDto;
import com.example.teodormihai.demo.dto.ProposalRequest;
import com.example.teodormihai.demo.dto.StatusRequest;
import com.example.teodormihai.demo.dto.VoteRequest;
import com.example.teodormihai.demo.service.TakeoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class TakeoutApiController {

    private final TakeoutService takeoutService;
    private final CookieHelper cookieHelper;

    public TakeoutApiController(TakeoutService takeoutService, CookieHelper cookieHelper) {
        this.takeoutService = takeoutService;
        this.cookieHelper = cookieHelper;
    }

    @GetMapping("/view")
    public ResponseEntity<DailyViewDto> view(HttpServletRequest request) {
        return cookieHelper.readUser(request)
                .map(userName -> ResponseEntity.ok(takeoutService.getDailyView(userName, LocalDate.now())))
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/status")
    public ResponseEntity<DailyViewDto> setStatus(@Valid @RequestBody StatusRequest body,
                                                   HttpServletRequest request) {
        String userName = requireUser(request);
        var session = takeoutService.getOrCreateSession(LocalDate.now());
        takeoutService.setStatus(userName, session.getId(), body.status());
        return ResponseEntity.ok(takeoutService.getDailyView(userName, LocalDate.now()));
    }

    @PostMapping("/proposals")
    public ResponseEntity<DailyViewDto> addProposal(@Valid @RequestBody ProposalRequest body,
                                                     HttpServletRequest request) {
        String userName = requireUser(request);
        var session = takeoutService.getOrCreateSession(LocalDate.now());
        takeoutService.addProposal(body.restaurant(), session.getId());
        return ResponseEntity.status(201).body(takeoutService.getDailyView(userName, LocalDate.now()));
    }

    @PostMapping("/votes")
    public ResponseEntity<DailyViewDto> castVote(@Valid @RequestBody VoteRequest body,
                                                  HttpServletRequest request) {
        String userName = requireUser(request);
        takeoutService.castVote(userName, body.proposalId());
        return ResponseEntity.ok(takeoutService.getDailyView(userName, LocalDate.now()));
    }

    @DeleteMapping("/votes/by-proposal/{proposalId}")
    public ResponseEntity<DailyViewDto> retractVote(@PathVariable Long proposalId,
                                                     HttpServletRequest request) {
        String userName = requireUser(request);
        takeoutService.retractVote(proposalId, userName);
        return ResponseEntity.ok(takeoutService.getDailyView(userName, LocalDate.now()));
    }

    private String requireUser(HttpServletRequest request) {
        return cookieHelper.readUser(request)
                .orElseThrow(() -> new IllegalStateException("Not authenticated"));
    }
}
