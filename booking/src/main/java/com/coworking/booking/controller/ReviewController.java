package com.coworking.booking.controller;

import com.coworking.booking.dto.ReviewDTO;
import com.coworking.booking.dto.ReviewRequest;
import com.coworking.booking.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<String> addReview(@RequestBody ReviewRequest request) {
        reviewService.addReview(request);
        return ResponseEntity.ok("Review added successfully.");
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByWorkspace(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(reviewService.getReviewsByWorkspace(workspaceId));
    }
}
