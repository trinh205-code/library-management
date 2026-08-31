package com.slayer.backend.service.impl;

import com.slayer.backend.Enums.PaymentGateway;
import com.slayer.backend.Enums.PaymentType;
import com.slayer.backend.entity.Subscription;
import com.slayer.backend.entity.SubscriptionPlan;
import com.slayer.backend.entity.User;
import com.slayer.backend.exception.SubscriptionException;
import com.slayer.backend.mapper.SubscriptionMapper;
import com.slayer.backend.payload.dto.SubscriptionDTO;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.payload.request.PaymentInitiateRequest;
import com.slayer.backend.repository.SubscriptionPlanRepository;
import com.slayer.backend.repository.SubscriptionRepository;
import com.slayer.backend.service.SubscriptionInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionInterface {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserService userService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentServiceImpl paymentService;

    @Override
    public PaymentInitiateResponse subscribe(SubscriptionDTO subscriptionDTO) throws Exception {

        User user = userService.getUser();
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionDTO.getPlanId()).orElseThrow(
                ()-> new Exception("Plan not found")
        );

        Subscription subscription = subscriptionMapper.toEntity(subscriptionDTO, plan, user);
        subscription.initializeFromPlan();
        subscription.setIsActive(false);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .userId(user.getId())
                .subscriptionId(subscription.getId())
                .paymentType(PaymentType.MEMBERSHIP)
                .paymentGateway(PaymentGateway.VNPAY)
                .amount(subscription.getPrice())
                .description("Library Subscription - " + plan.getName())
                .build();

        return paymentService.initiatePayment(request, user);
    }

    @Override
    public SubscriptionDTO getUsersActiveSubscription(Long userId) throws Exception {
        User user = userService.getUser();

        Long targetUserId = (userId != null) ? userId : user.getId();

        List<Subscription> subscriptions = subscriptionRepository
                .findActiveSubscriptionByUserId(targetUserId, LocalDate.now());

        if (subscriptions == null || subscriptions.isEmpty()) {
            throw new SubscriptionException("no active subscription found");
        }

        Subscription subscription = subscriptions.get(0);

        return subscriptionMapper.toDTO(subscription);
    }

    @Override
    public SubscriptionDTO cancelSubscription(Long subscriptionId, String reason) throws SubscriptionException {

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                ()-> new SubscriptionException("Subscription not found with ID: " + subscriptionId)
        );

        if(!subscription.getIsActive()) {
            throw  new SubscriptionException("Subscription is already inactive");
        }

        subscription.setIsActive(false);
        subscription.setCancelleddAt(LocalDateTime.now());
        subscription.setCancellationReason(reason != null ? reason : "Cancelled by user");

        subscriptionRepository.save(subscription);

        return subscriptionMapper.toDTO(subscription);
    }

    @Override
    public SubscriptionDTO activeSubscription(Long subscriptionId, Long paymentId) throws SubscriptionException {

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                ()-> new SubscriptionException("subscription not found by id!")
        );

        subscription.setIsActive(true);
        subscription.setStartDate(LocalDate.now());
        subscription.calculateEndDate();
        subscriptionRepository.save(subscription);

        return subscriptionMapper.toDTO(subscription);
    }

    @Override
    public List<SubscriptionDTO> getAllSubscriptions(Pageable pageable) {
        List<Subscription> subscriptions=subscriptionRepository.findAll();
        return subscriptionMapper.toDTOList(subscriptions);
    }

    @Override
    public void deactivateExpireSubscriptions() throws SubscriptionException {
        List<Subscription> expiredSubscriptions=subscriptionRepository
                .findExpireActiveSubscriptions(LocalDate.now());

        for(Subscription subscription : expiredSubscriptions){
            subscription.setIsActive(false);
            subscriptionRepository.save(subscription);
        }
    }
}
