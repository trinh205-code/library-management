package com.slayer.backend.mapper;

import com.slayer.backend.entity.Subscription;
import com.slayer.backend.entity.SubscriptionPlan;
import com.slayer.backend.entity.User;
import com.slayer.backend.exception.SubscriptionException;
import com.slayer.backend.payload.dto.SubscriptionDTO;
import com.slayer.backend.repository.SubscriptionPlanRepository;
import com.slayer.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SubscriptionMapper {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

    public SubscriptionDTO toDTO(Subscription subscription) {

        if (subscription == null) {
            return null;
        }

        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(subscription.getId());

        if (subscription.getUser() != null) {
            dto.setUserId(subscription.getUser().getId());
            dto.setUserName(subscription.getUser().getFullName());
            dto.setUserEmail(subscription.getUser().getEmail());
        }

        if (subscription.getPlan() != null) {
            dto.setPlanId(subscription.getPlan().getId());
        }
        dto.setPlanName(subscription.getPlanName());
        dto.setPlanCode(subscription.getPlanCode());
        dto.setPrice(subscription.getPrice());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());
        dto.setIsActive(subscription.getIsActive());
        dto.setMaxBooksAllowed(subscription.getMaxBooksAllowed());
        dto.setMaxDaysPerBook(subscription.getMaxDaysAllowed());
        dto.setAutoRenew(subscription.getAutoRenew());
        dto.setCancellationReason(subscription.getCancellationReason());
        dto.setCancelledAt(subscription.getCancelleddAt());
        dto.setNotes(subscription.getNotes());
        dto.setCreatedAt(subscription.getCreatedAt());
        dto.setUpdatedAt(subscription.getUpdatedAt());

        dto.setDaysRemaining(subscription.getDaysRemaining());
        dto.setIsValid(subscription.isValid());
        dto.setIsExpired(subscription.isExpired());

        return dto;

    }

    public Subscription toEntity(SubscriptionDTO dto, SubscriptionPlan plan, User user) throws SubscriptionException {
        if (dto == null)
            return null;

        Subscription subscription = new Subscription();
        subscription.setId(dto.getId());
        subscription.setUser(user);
        subscription.setPlan(plan);

        subscription.setNotes(dto.getNotes());

        return subscription;
    }

    public List<SubscriptionDTO> toDTOList(List<Subscription> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}
