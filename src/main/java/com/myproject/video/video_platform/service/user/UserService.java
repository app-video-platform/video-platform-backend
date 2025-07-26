package com.myproject.video.video_platform.service.user;

import com.myproject.video.video_platform.dto.user.SocialMediaLinkResponse;
import com.myproject.video.video_platform.dto.user.SocialMediaLinkUpdateRequest;
import com.myproject.video.video_platform.dto.user.UpdateUserRequest;
import com.myproject.video.video_platform.dto.user.UserDto;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.SocialMediaLink;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Log log = LogFactory.getLog(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserDto getUserInfo(Authentication authentication) {
        // Extract the user’s principal (email) from the Authentication object.
        String email = (String) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        return getUserDto(user);
    }

    private UserDto getUserDto(User user) {
        List<SocialMediaLinkResponse> linkResponses = user.getSocialLinks()
                .stream()
                .map(l -> new SocialMediaLinkResponse(
                        l.getId(), l.getPlatform(), l.getUrl(), l.getCreatedAt()))
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(user.getUserId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getRoleName).toList())
                .title(user.getTitle())
                .bio(user.getBio())
                .taglineMission(user.getTaglineMission())
                .website(user.getWebsite())
                .city(user.getCity())
                .country(user.getCountry())
                .onboardingCompleted(user.isOnboardingCompleted())
                .socialLinks(linkResponses)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public Optional<User> findByUserId(UUID uuid) {
        return userRepository.findById(uuid);
    }

    @Transactional
    public UserDto updateUserInfo(UpdateUserRequest req) {
        User user = userRepository.findById(UUID.fromString(req.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getUserId()));

        log.info("Updating user: " + req.getUserId());

        if (req.getTitle() != null)          user.setTitle(req.getTitle());
        if (req.getBio() != null)            user.setBio(req.getBio());
        if (req.getTaglineMission() != null) user.setTaglineMission(req.getTaglineMission());
        if (req.getWebsite() != null)        user.setWebsite(req.getWebsite());
        if (req.getCity() != null)           user.setCity(req.getCity());
        if (req.getCountry() != null)        user.setCountry(req.getCountry());

        // 2️⃣ Reconcile socialLinks
        List<SocialMediaLink> existing = new ArrayList<>(user.getSocialLinks());
        List<SocialMediaLinkUpdateRequest> incoming =
                req.getSocialLinks() != null ? req.getSocialLinks() : Collections.emptyList();

        // Build set of incoming IDs to keep
        Set<UUID> keepIds = incoming.stream()
                .map(SocialMediaLinkUpdateRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove any existing link not in keepIds
        for (SocialMediaLink link : existing) {
            if (link.getId() != null && !keepIds.contains(link.getId())) {
                user.removeSocialLink(link);
            }
        }

        // Add or update incoming
        for (SocialMediaLinkUpdateRequest in : incoming) {
            if (in.getId() != null) {
                // update existing
                SocialMediaLink link = existing.stream()
                        .filter(l -> l.getId().equals(in.getId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "SocialMediaLink ID not found: " + in.getId()));
                link.setPlatform(in.getPlatform());
                link.setUrl(in.getUrl());
            } else {
                // new link
                SocialMediaLink newLink = SocialMediaLink.builder()
                        .platform(in.getPlatform())
                        .url(in.getUrl())
                        .build();
                user.addSocialLink(newLink);
            }
        }

        User saved = userRepository.save(user);

        log.info("Saved user: " + saved.getUserId());

        // map to UserProfileResponse
        return getUserDto(saved);

    }
}
