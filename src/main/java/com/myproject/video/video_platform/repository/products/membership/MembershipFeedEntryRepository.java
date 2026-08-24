package com.myproject.video.video_platform.repository.products.membership;

import com.myproject.video.video_platform.entity.products.membership.MembershipFeedEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MembershipFeedEntryRepository extends JpaRepository<MembershipFeedEntry, UUID> {
    List<MembershipFeedEntry> findAllByMembershipId(UUID membershipId);
    void deleteAllByMembershipId(UUID membershipId);
    void deleteByMembershipIdAndContentId(UUID membershipId, UUID contentId);

    @Modifying
    @Query("delete from MembershipFeedEntry e where e.includedProductId = :productId")
    int deleteAllByIncludedProductId(@Param("productId") UUID productId);

    @Query("select distinct e.membership.id from MembershipFeedEntry e where e.includedProductId = :productId")
    List<UUID> findMembershipIdsByIncludedProductId(@Param("productId") UUID productId);
}
