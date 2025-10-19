package com.myproject.video.video_platform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile fields that the authenticated user may update.")
public class UpdateUserRequest {
    @Schema(description = "User identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private String userId;

    @Size(max = 100)
    @Schema(description = "Public title shown on storefront", example = "Lifestyle Photographer")
    private String title;

    @Size(max = 1000)
    @Schema(description = "Short biography", example = "Helping creatives build their first paid lifestyle shoots.")
    private String bio;

    @Size(max = 500)
    @Schema(description = "Mission tagline", example = "Teach lifestyle photographers how to lead confident shoots")
    private String taglineMission;

    @Size(max = 255)
    @Schema(description = "Personal website", example = "https://ameliahughes.studio")
    private String website;

    @Size(max = 100)
    @Schema(description = "City", example = "Berlin")
    private String city;

    @Size(max = 100)
    @Schema(description = "Country", example = "Germany")
    private String country;

    /**
     * Full replacement set:
     * • null or empty = remove all links
     * • items w/ null id = new links
     * • items w/ existing id = update that link
     * • any existing link not referenced by id here = delete
     */
    @Valid
    @Schema(description = "Full replacement list of social links")
    private List<SocialMediaLinkUpdateRequest> socialLinks;
}
