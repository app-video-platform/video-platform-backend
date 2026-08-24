package com.myproject.video.video_platform.repository.products.membership;

import com.myproject.video.video_platform.entity.products.membership.MembershipContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipContentRepository extends JpaRepository<MembershipContent, UUID> {
    List<MembershipContent> findAllByMembershipId(UUID membershipId);
    Optional<MembershipContent> findByIdAndMembershipId(UUID id, UUID membershipId);
    void deleteAllByMembershipId(UUID membershipId);
}
