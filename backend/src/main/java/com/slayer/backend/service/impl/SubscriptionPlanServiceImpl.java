package com.slayer.backend.service.impl;

import com.slayer.backend.entity.SubscriptionPlan;
import com.slayer.backend.entity.User;
import com.slayer.backend.mapper.SubscriptionPlanMapper;
import com.slayer.backend.payload.dto.SubscriptionPlanDTO;
import com.slayer.backend.repository.SubscriptionPlanRepository;
import com.slayer.backend.repository.UserRepository;
import com.slayer.backend.service.SubscriptionPlanInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanInterface {
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPlanMapper planMapper;
    private final UserService userService;

    @Override
    public SubscriptionPlanDTO createSubscriptionPlan(SubscriptionPlanDTO PlanDTO) throws Exception {
        if(planRepository.existsByPlanCode(PlanDTO.getPlanCode())) {
            throw new IllegalArgumentException("Plan code already exists");
        }
        SubscriptionPlan plan = planMapper.toEntity(PlanDTO);

        User currentUser = userService.getUser();
        plan.setCreatedBy(currentUser.getFullName());
        plan.setCreatedBy(currentUser.getFullName());
        SubscriptionPlan savePlan = planRepository.save(plan);
        return planMapper.toDTO(savePlan);
    }

    @Override
    public SubscriptionPlanDTO updateSubscriptionPlan(Long planId, SubscriptionPlanDTO PlanDTO) throws Exception {
        SubscriptionPlan existingPlan = planRepository.findById(planId).orElseThrow(
                () -> new Exception("Plan not found")
        );
        planMapper.updateEntity(existingPlan, PlanDTO);
        User currentUser = userService.getUser();
        existingPlan.setUpdatedBy(currentUser.getFullName());

        SubscriptionPlan updatedPlan = planRepository.save(existingPlan);

        return planMapper.toDTO(updatedPlan);
    }

    @Override
    public void deleteSubscriptionPlan(Long planId) throws Exception {
        SubscriptionPlan plan = planRepository.findById(planId).orElseThrow(
                () -> new Exception("plan not found")
        );
        planRepository.delete(plan);
    }

    @Override
    public List<SubscriptionPlanDTO> getAllSubscriptionPlan() {
        List<SubscriptionPlan> plans = planRepository.findAll();

        return plans.stream().map(planMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlan getBySubscriptionPlanCode(String planCode) throws Exception {
        SubscriptionPlan plan = planRepository.findByPlanCode(planCode);
        if(plan == null) {
            throw new Exception("Plan not found");
        }
        return plan;
    }

}
