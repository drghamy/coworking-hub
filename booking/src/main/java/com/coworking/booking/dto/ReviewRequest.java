package com.coworking.booking.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long userId;
    private Long workspaceId;
    private Integer rating;
    private String comment;
}