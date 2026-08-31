package com.slayer.backend.mapper;

import com.slayer.backend.entity.SubscriptionPlan;
import com.slayer.backend.payload.dto.SubscriptionPlanDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class SubscriptionPlanMapper {

    public SubscriptionPlanDTO toDTO(SubscriptionPlan plan) {
        if(plan==null){
            return null;
        }

        SubscriptionPlanDTO dto = SubscriptionPlanDTO.builder()
                .id(plan.getId())
                .planCode(plan.getPlanCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .durationDays(plan.getDurationDays())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .maxBooksAllowed(plan.getMaxBooksAllowed())
                .maxDaysPerBook(plan.getMaxDaysPerBook())
                .displayOrder(plan.getDisplayOrder())
                .isActive(plan.getIsActive())
                .isFeatured(plan.getIsFeatured())
                .badgeText(plan.getBadgeText())
                .adminNotes(plan.getAdminNotes())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .createdBy(plan.getCreatedBy())
                .updatedBy(plan.getUpdatedBy())
                .build();

        return dto;
    }

    public SubscriptionPlan toEntity(SubscriptionPlanDTO plan) {
        if(plan==null){
            return null;
        }
        SubscriptionPlan entity = SubscriptionPlan.builder()
                .id(plan.getId())
                .planCode(plan.getPlanCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .durationDays(plan.getDurationDays())
                .currency(plan.getCurrency())
                .price(plan.getPrice())
                .maxBooksAllowed(plan.getMaxBooksAllowed())
                .maxDaysPerBook(plan.getMaxDaysPerBook())
                .displayOrder(plan.getDisplayOrder() != null ? plan.getDisplayOrder() : 0)
                .isActive(plan.getIsActive() != null ? plan.getIsActive() : true)
                .isFeatured(plan.getIsFeatured() != null ? plan.getIsFeatured() : false)
                .badgeText(plan.getBadgeText())
                .adminNotes(plan.getAdminNotes())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .createdBy(plan.getCreatedBy())
                .updatedBy(plan.getUpdatedBy())
                .build();

        return entity;
    }

    public void updateEntity(SubscriptionPlan plan, SubscriptionPlanDTO dto) {
        if(plan == null || dto == null) return;

        if(dto.getPlanCode() != null) {
            plan.setPlanCode(dto.getPlanCode());
        }

        if(dto.getName() != null) {
            plan.setName(dto.getName());
        }

        if(dto.getDescription() != null) {
            plan.setDescription(dto.getDescription());
        }

        if(dto.getDurationDays() != null) {
            plan.setDurationDays(dto.getDurationDays());
        }

        if(dto.getPrice() != null) {
            plan.setPrice(dto.getPrice());
        }

        if(dto.getCurrency() != null) {   // ✅ thêm dòng này
            plan.setCurrency(dto.getCurrency());
        }

        if(dto.getMaxBooksAllowed() != null) {
            plan.setMaxBooksAllowed(dto.getMaxBooksAllowed());
        }

        if(dto.getMaxDaysPerBook() != null) {
            plan.setMaxDaysPerBook(dto.getMaxDaysPerBook());
        }

        if(dto.getDisplayOrder() != null) {
            plan.setDisplayOrder(dto.getDisplayOrder());
        }

        if(dto.getIsActive() != null) {
            plan.setIsActive(dto.getIsActive());
        }

        if(dto.getIsFeatured() != null) {
            plan.setIsFeatured(dto.getIsFeatured());
        }

        if(dto.getBadgeText() != null) {
            plan.setBadgeText(dto.getBadgeText());
        }

        if(dto.getAdminNotes() != null) {
            plan.setAdminNotes(dto.getAdminNotes());
        }
    }
}
