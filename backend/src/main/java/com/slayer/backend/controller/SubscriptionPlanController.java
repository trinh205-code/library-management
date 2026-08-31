package com.slayer.backend.controller;

import com.slayer.backend.payload.dto.SubscriptionPlanDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.service.impl.SubscriptionPlanServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/subscription-plans")
public class SubscriptionPlanController {
    private final SubscriptionPlanServiceImpl subscriptionPlan;

    @GetMapping()
    public ResponseEntity<?> getAllSubscriptionPlans() {
        List<SubscriptionPlanDTO> plans = subscriptionPlan.getAllSubscriptionPlan();

        return ResponseEntity.ok(plans);
    }

    @PostMapping("/admin/create")
    public ResponseEntity<SubscriptionPlanDTO> createSubscriptionPlan(@Valid @RequestBody SubscriptionPlanDTO subscriptionPlanDTO) throws Exception {
         SubscriptionPlanDTO planDTO = subscriptionPlan.createSubscriptionPlan(subscriptionPlanDTO);

        return ResponseEntity.ok(planDTO);
    }

    @DeleteMapping("/admin/{subscriptionPlanId}")
    public ResponseEntity<?> deleteSubscriptionPlan(@PathVariable() Long subscriptionPlanId) throws Exception {
        subscriptionPlan.deleteSubscriptionPlan(subscriptionPlanId);
        ApiResponse response = new ApiResponse("SubscriptionPlan Delete Successfully", true);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/{subscriptionPlanId}")
    public ResponseEntity<?> updateSubscriptionPlan(@PathVariable Long subscriptionPlanId, @RequestBody SubscriptionPlanDTO subscriptionPlanDTO) throws Exception {

        subscriptionPlan.updateSubscriptionPlan(subscriptionPlanId, subscriptionPlanDTO);
        ApiResponse response = new ApiResponse("SubscriptionPlan Update Successfully", true);
        return ResponseEntity.ok(subscriptionPlanDTO);
    }



}
