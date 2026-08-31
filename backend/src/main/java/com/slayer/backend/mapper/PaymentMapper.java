package com.slayer.backend.mapper;

import com.slayer.backend.entity.Payment;
import com.slayer.backend.payload.dto.PaymentDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    @Mapping(target = "bookLoanId", ignore = true)
    @Mapping(target = "successUrl", ignore = true)
    @Mapping(target = "cancelUrl", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    PaymentDTO toDTO(Payment payment);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "fine", ignore = true)
    Payment toEntity(PaymentDTO dto);
}
