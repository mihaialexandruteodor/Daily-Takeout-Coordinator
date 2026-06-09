package com.example.teodormihai.demo.web;

import com.example.teodormihai.demo.dto.DailyViewDto;
import com.example.teodormihai.demo.dto.ProposalRequest;
import com.example.teodormihai.demo.dto.StatusRequest;
import com.example.teodormihai.demo.dto.TimeProposalRequest;
import com.example.teodormihai.demo.dto.VoteRequest;
import com.example.teodormihai.demo.service.DuplicateNameException;
import com.example.teodormihai.demo.service.TakeoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TakeoutApiController {

    private final TakeoutService takeoutService;
    private final CookieHelper cookieHelper;

    public TakeoutApiController(TakeoutService takeoutService, CookieHelper cookieHelper) {
        this.takeoutService = takeoutService;
        this.cookieHelper = cookieHelper;
    }

    // ── Inline join (called from the name modal on /today) ────────────────────

    /**
     * Called by the modal on /today when the user has no cookie.
     * Registers the participant (with IP dedup) and sets the cookie.
     * Returns the full DailyViewDto so the page can render immediately.
     */
    @PostMapping("/join-inline")
    public ResponseEntity<?> joinInline(@RequestBody Map<String, String> body,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name must not be blank"));
        }
        try {
            String ip = TakeoutController.getClientIp(request);
            var participant = takeoutService.registerParticipant(name, LocalDate.now(), ip);
            cookieHelper.writeUser(response, participant.getUserName());
            return ResponseEntity.ok(takeoutService.getDailyView(participant.getUserName(), LocalDate.now()));
        } catch (DuplicateNameException e) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", "This name is already taken today. Please choose another."));
        }
    }

    // ── Existing endpoints (unchanged) ────────────────────────────────────────

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

    // ── Time proposal endpoints ───────────────────────────────────────────────

    @PostMapping("/time-proposals")
    public ResponseEntity<?> addTimeProposal(@Valid @RequestBody TimeProposalRequest body,
                                              HttpServletRequest request) {
        String userName = requireUser(request);
        var session = takeoutService.getOrCreateSession(LocalDate.now());
        try {
            takeoutService.addTimeProposal(body.proposedTime(), userName, session.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.status(201).body(takeoutService.getDailyView(userName, LocalDate.now()));
    }

    @PostMapping("/time-votes")
    public ResponseEntity<DailyViewDto> castTimeVote(@RequestBody Map<String, Long> body,
                                                      HttpServletRequest request) {
        String userName = requireUser(request);
        Long timeProposalId = body.get("timeProposalId");
        takeoutService.castTimeVote(userName, timeProposalId);
        return ResponseEntity.ok(takeoutService.getDailyView(userName, LocalDate.now()));
    }

    @DeleteMapping("/time-votes/by-proposal/{timeProposalId}")
    public ResponseEntity<DailyViewDto> retractTimeVote(@PathVariable Long timeProposalId,
                                                         HttpServletRequest request) {
        String userName = requireUser(request);
        takeoutService.retractTimeVote(timeProposalId, userName);
        return ResponseEntity.ok(takeoutService.getDailyView(userName, LocalDate.now()));
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String requireUser(HttpServletRequest request) {
        return cookieHelper.readUser(request)
                .orElseThrow(() -> new IllegalStateException("Not authenticated"));
    }
}
