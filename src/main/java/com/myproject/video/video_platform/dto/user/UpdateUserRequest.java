package com.myproject.video.video_platform.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String userId;

    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    private String bio;

    @Size(max = 500)
    private String taglineMission;

    @Size(max = 255)
    private String website;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    /**
     * Full replacement set:
     * • null or empty = remove all links
     * • items w/ null id = new links
     * • items w/ existing id = update that link
     * • any existing link not referenced by id here = delete
     */
    @Valid
    private List<SocialMediaLinkUpdateRequest> socialLinks;
}
