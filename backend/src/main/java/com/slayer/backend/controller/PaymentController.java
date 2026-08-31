package com.slayer.backend.controller;

import com.slayer.backend.entity.User;
import com.slayer.backend.entity.Payment;
import com.slayer.backend.Enums.PaymentType;
import com.slayer.backend.payload.dto.PaymentDTO;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.payload.request.PaymentInitiateRequest;
import com.slayer.backend.service.gateway.VnPayService;
import com.slayer.backend.service.impl.PaymentServiceImpl;
import com.slayer.backend.service.impl.UserService;
import com.slayer.backend.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;
    private final PaymentServiceImpl paymentService;
    private final UserService userService;
    private final PaymentRepository paymentRepository;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(@RequestBody PaymentInitiateRequest request) throws Exception {
        User currenUser = userService.getUser();
        PaymentInitiateResponse response = paymentService.initiatePayment(request, currenUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {

        if (!vnPayService.isValidPayment(request)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/my-fines?payment=failed&reason=invalid_signature"))
                    .build();
        }

        // 2. Nếu chữ ký chuẩn, bắt đầu lấy thông tin
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionId = request.getParameter("vnp_TxnRef");
        
        String redirectUrl = "http://localhost:5173/my-fines";
        Payment payment = paymentRepository.findByTransactionId(transactionId).orElse(null);
        if (payment != null && payment.getPaymentType() == PaymentType.MEMBERSHIP) {
            redirectUrl = "http://localhost:5173/subscriptions";
        }

        if ("00".equals(responseCode)) {
           paymentService.processPaymentSuccess(transactionId);
           return ResponseEntity.status(HttpStatus.FOUND)
                   .location(URI.create(redirectUrl + "?payment=success"))
                   .build();
        } else {
            // Khách hủy hoặc thẻ lỗi
//             paymentService.updatePaymentStatus(transactionId, PaymentStatus.FAILED);
            return ResponseEntity.status(HttpStatus.FOUND)
                   .location(URI.create(redirectUrl + "?payment=failed&reason=" + responseCode))
                   .build();
        }
    }

    @GetMapping()
    public ResponseEntity<?> getAllPayments(
            @RequestParam(required = false) com.slayer.backend.Enums.PaymentStatus status,
            @RequestParam(required = false) com.slayer.backend.Enums.PaymentType type,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.getAllPayments(status, type, userName, startDate, endDate, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/my-payments")
    public ResponseEntity<?> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) throws Exception {

        User currentUser = userService.getUser();
        Sort sort = sortDir.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.getMyPayments(currentUser.getId(), pageable);
        return ResponseEntity.ok(payments);
    }
}