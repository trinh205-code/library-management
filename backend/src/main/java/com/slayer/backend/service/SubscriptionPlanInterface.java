package com.slayer.backend.service;

import com.slayer.backend.entity.SubscriptionPlan;
import com.slayer.backend.payload.dto.SubscriptionPlanDTO;

import java.util.List;

public interface SubscriptionPlanInterface {
   SubscriptionPlanDTO createSubscriptionPlan(SubscriptionPlanDTO PlanDTO) throws Exception;

   SubscriptionPlanDTO updateSubscriptionPlan(Long planId,SubscriptionPlanDTO PlanDTO) throws Exception;

   void deleteSubscriptionPlan(Long planId) throws Exception;

   List<SubscriptionPlanDTO> getAllSubscriptionPlan();

   SubscriptionPlan getBySubscriptionPlanCode(String planCode) throws Exception;
}
