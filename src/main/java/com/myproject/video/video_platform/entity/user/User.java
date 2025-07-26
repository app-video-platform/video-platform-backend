package com.myproject.video.video_platform.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myproject.video.video_platform.entity.products.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a user in the system.
 * Stores user credentials, roles, and verification status.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private UUID userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean enabled = false;

    private String title;

    private String bio;

    private String taglineMission;

    private String website;

    private String city;

    private String country;

    private boolean onboardingcompleted;

    @Column(name = "auth_provider", nullable = false)
    private String authProvider = "LOCAL";

    private Instant createdAt = Instant.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Product> products;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<SocialMediaLink> socialLinks = new ArrayList<>();

    /**
     * Convenience constructor for minimal fields.
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.enabled = false;
        this.createdAt = Instant.now();
    }

    public void addSocialLink(SocialMediaLink link) {
        socialLinks.add(link);
        link.setUser(this);
    }

    public void removeSocialLink(SocialMediaLink link) {
        socialLinks.remove(link);
        link.setUser(null);
    }
}
