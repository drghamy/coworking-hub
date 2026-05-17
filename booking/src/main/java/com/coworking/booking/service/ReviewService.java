package com.coworking.booking.service;

import com.coworking.booking.dto.ReviewDTO;
import com.coworking.booking.dto.ReviewRequest;

import java.util.List;

public interface ReviewService {
    void addReview(ReviewRequest request);

    List<ReviewDTO> getReviewsByWorkspace(Long workspaceId);
}
