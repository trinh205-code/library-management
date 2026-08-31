package com.slayer.backend.controller;

import com.slayer.backend.entity.Subscription;
import com.slayer.backend.payload.dto.SubscriptionDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.service.impl.SubscriptionServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/subscriptions")
public class SubscriptionController {
    private final SubscriptionServiceImpl subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscriptionDTO subscription) throws Exception{

        PaymentInitiateResponse dto = subscriptionService.subscribe(subscription);
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/user/active")
    public ResponseEntity<?> getUserActiveSubscriptions(@RequestParam(required = false) Long userId) throws Exception{
        int page = 10;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<SubscriptionDTO> dtoList = subscriptionService.getAllSubscriptions(pageable);

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAllSubscriptions() throws Exception{
        int page = 10;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<SubscriptionDTO> dtoList = subscriptionService.getAllSubscriptions(pageable);

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/admin/deactivate-expired")
    public ResponseEntity<?> deactivateExpiredSubscriptions() throws Exception{
        subscriptionService.deactivateExpireSubscriptions();
        ApiResponse res = new ApiResponse("task done", true);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/cancel/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam(required = false) String reason) throws Exception{

        SubscriptionDTO subscriptionDTO = subscriptionService.cancelSubscription(subscriptionId, reason);

        return ResponseEntity.ok(subscriptionDTO);
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateSubscription(
            @Valid @RequestParam Long subscriptionId, @RequestParam Long paymentId) throws Exception{

        SubscriptionDTO subscription = subscriptionService.activeSubscription(subscriptionId, paymentId);

        return ResponseEntity.ok(subscription);
    }
}
