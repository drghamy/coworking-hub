package com.coworking.booking.service.impl;

import com.coworking.booking.dto.ReviewDTO;
import com.coworking.booking.dto.ReviewRequest;
import com.coworking.booking.model.Review;
import com.coworking.booking.model.Workspace;
import com.coworking.booking.repository.ReviewRepository;
import com.coworking.booking.repository.WorkspaceRepository;
import com.coworking.booking.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    public void addReview(ReviewRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Review review = Review.builder()
                .userId(request.getUserId())
                .workspace(workspace)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByWorkspace(Long workspaceId) {
        return reviewRepository.findByWorkspaceId(workspaceId).stream()
                .map(review -> ReviewDTO.builder()
                        .id(review.getId())
                        .userId(review.getUserId())
                        .workspaceId(review.getWorkspace().getId())
                        .workspaceName(review.getWorkspace().getName())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .build())
                .toList();
    }
}
