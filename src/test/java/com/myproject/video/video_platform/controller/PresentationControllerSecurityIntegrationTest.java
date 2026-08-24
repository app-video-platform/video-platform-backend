package com.myproject.video.video_platform.controller;

import com.myproject.video.video_platform.common.enums.ProductLandingHeroLayout;
import com.myproject.video.video_platform.common.enums.ProductLandingSection;
import com.myproject.video.video_platform.common.enums.StorefrontAppearance;
import com.myproject.video.video_platform.common.enums.StorefrontTypography;
import com.myproject.video.video_platform.dto.products.landing.ProductLandingPageDtos;
import com.myproject.video.video_platform.dto.storefront.StorefrontDtos;
import com.myproject.video.video_platform.service.ProductLandingPageService;
import com.myproject.video.video_platform.service.StorefrontService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PresentationControllerSecurityIntegrationTest {
    @Autowired MockMvc mockMvc;
    @MockBean StorefrontService storefrontService;
    @MockBean ProductLandingPageService landingPageService;

    @Test
    void anonymousUsersCanReadPublicStorefrontAndPublishedLandingConfig() throws Exception {
        UUID id = UUID.randomUUID();
        StorefrontDtos.Theme theme = new StorefrontDtos.Theme(StorefrontAppearance.DARK, "#ffbd41", StorefrontTypography.MODERN);
        when(storefrontService.getPublicStorefront(id)).thenReturn(new StorefrontDtos.PublicStorefront(id.toString(),
                new StorefrontDtos.PublicCreator(id.toString(), "Creator", null, null, null, null, null, null, null, List.of()),
                null, List.of(), theme));
        when(landingPageService.getPublic(id)).thenReturn(new ProductLandingPageDtos.Config(null, id.toString(), "",
                ProductLandingHeroLayout.MEDIA_RIGHT, List.of(), List.of(ProductLandingSection.ABOUT,
                ProductLandingSection.CONTENTS, ProductLandingSection.CREATOR), null));

        mockMvc.perform(get("/api/storefronts/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.creator.email").doesNotExist());
        mockMvc.perform(get("/api/products/{id}/landing-page", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void creatorCanReadOwnStorefrontConfig() throws Exception {
        when(storefrontService.getCreatorConfig()).thenReturn(new StorefrontDtos.Config(null, null, List.of(),
                new StorefrontDtos.Theme(StorefrontAppearance.DARK, "#ffbd41", StorefrontTypography.MODERN), null));
        mockMvc.perform(get("/api/creator/storefront")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCannotReadCreatorStorefrontConfigButCanManageLandingConfig() throws Exception {
        UUID id = UUID.randomUUID();
        when(landingPageService.getForManager(id)).thenReturn(new ProductLandingPageDtos.Config(null, id.toString(), "",
                ProductLandingHeroLayout.MEDIA_RIGHT, List.of(), List.of(ProductLandingSection.ABOUT,
                ProductLandingSection.CONTENTS, ProductLandingSection.CREATOR), null));
        mockMvc.perform(get("/api/creator/storefront")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/creator/products/{id}/landing-page", id)).andExpect(status().isOk());
    }
}
