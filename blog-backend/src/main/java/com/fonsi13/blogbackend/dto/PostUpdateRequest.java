package com.fonsi13.blogbackend.dto;

import com.fonsi13.blogbackend.models.PostStatus;
import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private List<String> images;
    private List<String> videoUrls;
    private List<String> topics;
    private PostStatus status;
}
