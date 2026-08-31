package com.slayer.backend.service.impl;

import com.google.gson.JsonObject;
import com.slayer.backend.Enums.PaymentGateway;
import com.slayer.backend.Enums.PaymentStatus;
import com.slayer.backend.Enums.PaymentType;
import com.slayer.backend.entity.Payment;
import com.slayer.backend.entity.Subscription;
import com.slayer.backend.entity.User;
import com.slayer.backend.event.PaymentEventPublisher;
import com.slayer.backend.mapper.PaymentMapper;
import com.slayer.backend.mapper.UserMapper;
import com.slayer.backend.payload.dto.PaymentDTO;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.payload.request.PaymentInitiateRequest;
import com.slayer.backend.repository.FineRepository;
import com.slayer.backend.repository.PaymentRepository;
import com.slayer.backend.repository.SubscriptionRepository;
import com.slayer.backend.service.PaymentServiceInterface;
import com.slayer.backend.service.gateway.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentServiceInterface {
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final VnPayService vnPayService;
    private final PaymentMapper paymentMapper;
    private final UserMapper userMapper;
    private final PaymentEventPublisher paymentEventPublisher;
    private final FineRepository fineRepository;


    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request, User user) {

        long totalAmount = 0;
        Subscription subscription = null;
        // Thêm biến fine nếu bạn có liên kết Entity
        // Fine fine = null;

        if (request.getPaymentType() == PaymentType.MEMBERSHIP) {
            subscription = subscriptionRepository.findById(request.getSubscriptionId()).orElseThrow(
                    () -> new RuntimeException("Subscription not found")
            );
            totalAmount = subscription.getPrice();

        } else if (request.getPaymentType() == PaymentType.FINE) {
            if (request.getAmount() == null) {
                throw new RuntimeException("Thiếu thông tin số tiền phạt");
            }
            totalAmount = request.getAmount();
        }

        if (totalAmount <= 0) {
            throw new RuntimeException("Số tiền thanh toán không hợp lệ!");
        }


        Payment payment = Payment.builder()
                .user(user)
                .paymentType(request.getPaymentType())
                .paymentGateway(request.getPaymentGateway())
                .amount(totalAmount)
                .description(request.getDescription())
                .paymentStatus(PaymentStatus.PENDING)
                .transactionId("TXN_" + UUID.randomUUID())
                .initiatedAt(LocalDateTime.now())
                .build();


        if (request.getPaymentType() == PaymentType.MEMBERSHIP && subscription != null) {
            payment.setSubscription(subscription);
        } else if (request.getPaymentType() == PaymentType.FINE) {
            // TODO: Nếu trong Entity Payment của bạn có tạo trường @ManyToOne Fine,
            // thì bạn phải set nó ở đây để lúc VNPay trả kết quả về còn biết đang nộp cho cái Phạt nào!
            payment.setFine(fineRepository.findById(request.getFineId()).get());
        }

        payment = paymentRepository.save(payment);

        String checkoutUrl = null;
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if (request.getPaymentGateway() == PaymentGateway.VNPAY) {
            checkoutUrl = vnPayService.createPaymentUrl(payment, httpRequest);
        }

        PaymentInitiateResponse response = new PaymentInitiateResponse();
        response.setPaymentId(payment.getId());
        response.setPaymentGateway(payment.getPaymentGateway());
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount() * 1000);
        response.setDescription(payment.getDescription());
        response.setCheckoutUrl(checkoutUrl);
        response.setSuccess(true);
        response.setMessage("Payment initiated successfully");

        return response;
    }

    @Override
    public PaymentDTO verifyPayment(PaymentInitiateRequest request) {
        // Trong trường hợp verify, bạn có thể truyền transactionId thay vì cả cụm PaymentInitiateRequest.
        // Tạm thời lấy transactionId từ request (bạn cần tự điều chỉnh lại DTO/Request cho khớp logic của mình)

        String transactionId = request.getTransactionId(); // Giả sử bạn thêm field này vào PaymentInitiateRequest

        Payment payment = paymentRepository.findByTransactionId(transactionId).orElseThrow(
                ()-> new RuntimeException("Không tìm thấy giao dịch với ID: " + transactionId)
        );

        // Chỉ kiểm tra những giao dịch đang ở trạng thái PENDING
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return paymentMapper.toDTO(payment); // Nếu đã SUCCESS hoặc FAILED từ trước thì trả về luôn, không gọi VNPAY nữa
        }

        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if (payment.getPaymentGateway() == PaymentGateway.VNPAY) {
            JsonObject vnPayResponse = vnPayService.queryTransaction(payment, httpRequest);

            if (vnPayResponse != null && vnPayResponse.has("vnp_ResponseCode") && vnPayResponse.has("vnp_TransactionStatus")) {
                String responseCode = vnPayResponse.get("vnp_ResponseCode").getAsString();
                String transactionStatus = vnPayResponse.get("vnp_TransactionStatus").getAsString();
                // ... logic check "00" của bạn ...
            } else {
                // Xử lý trường hợp VNPAY không trả về đúng format mong đợi
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Lỗi phản hồi từ cổng thanh toán VNPAY");
                paymentRepository.save(payment);
            }
        }

        return paymentMapper.toDTO(payment);
    }

    public void updatePaymentStatus(String transactionId, PaymentStatus status) {
        Payment payment = paymentRepository.findByTransactionId(transactionId).orElseThrow(
                ()-> new RuntimeException("Không tìm thấy giao dịch: " + transactionId)
        );


        // Nếu trạng thái đã chốt (Thành công/Thất bại) thì bỏ qua, tránh ghi đè sai
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setPaymentStatus(status);
        if (status == PaymentStatus.SUCCESS) {
            payment.setCompletedAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);
    }


    @Override
    public Page<PaymentDTO> getAllPayments(PaymentStatus status,
                                           PaymentType type,
                                           String userName,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate,
                                           Pageable pageable) {
        
        if (startDate == null) {
            startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusDays(1);
        }

        return paymentRepository.findFilteredPayments(status, type, userName, startDate, endDate, pageable)
                .map(paymentMapper::toDTO);
    }

    @Override
    public Page<PaymentDTO> getMyPayments(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable)
                .map(paymentMapper::toDTO);
    }

    @Override
    @Transactional
    public void processPaymentSuccess(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch: " + transactionId));

        // Nếu giao dịch đã được xử lý trước đó rồi thì bỏ qua (tránh duplicate)
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        //Bắn Event ra cho các module khác (Subscription, Notification...) xử lý
        paymentEventPublisher.publishPaymentSuccessEvent(payment);
    }
}
