package com.myproject.video.video_platform.service.admin;

import com.myproject.video.video_platform.dto.admin.AdminAuditLogDto;
import com.myproject.video.video_platform.entity.admin.AdminAuditLog;
import com.myproject.video.video_platform.repository.admin.AdminAuditLogRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminAuditService {

    private final AdminAuditLogRepository repository;
    private final CurrentUserService currentUserService;

    public AdminAuditService(AdminAuditLogRepository repository,
                             CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void recordCurrentAdminAction(String action,
                                         String targetType,
                                         String targetId,
                                         String beforeSummary,
                                         String afterSummary) {
        AdminAuditLog log = new AdminAuditLog();
        log.setActorUserId(currentUserService.getCurrentUserId());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeSummary(beforeSummary);
        log.setAfterSummary(afterSummary);
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLogDto> search(String actorId,
                                         String targetType,
                                         String targetId,
                                         String action,
                                         Pageable pageable) {
        return repository.findAll(spec(actorId, targetType, targetId, action), pageable)
                .map(this::toDto);
    }

    private Specification<AdminAuditLog> spec(String actorId,
                                              String targetType,
                                              String targetId,
                                              String action) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (actorId != null && !actorId.isBlank()) {
                predicates.add(cb.equal(root.get("actorUserId"), UUID.fromString(actorId)));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("targetType")), targetType.toLowerCase()));
            }
            if (targetId != null && !targetId.isBlank()) {
                predicates.add(cb.equal(root.get("targetId"), targetId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("action")), action.toLowerCase()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private AdminAuditLogDto toDto(AdminAuditLog log) {
        return AdminAuditLogDto.builder()
                .id(log.getId())
                .actorUserId(log.getActorUserId().toString())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .beforeSummary(log.getBeforeSummary())
                .afterSummary(log.getAfterSummary())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
