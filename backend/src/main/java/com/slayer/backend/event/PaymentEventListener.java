package com.slayer.backend.event;

import com.slayer.backend.entity.Fine;
import com.slayer.backend.entity.Payment;
import com.slayer.backend.repository.FineRepository;
import com.slayer.backend.service.impl.SubscriptionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final SubscriptionServiceImpl subscriptionService;
    private final FineRepository fineRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        Payment payment = event.getPayment();

        try {
            switch (payment.getPaymentType()) {
                case FINE:
                case LOST_BOOK_PENALTY:
                case DAMAGED_BOOK_PENALTY:
                    Fine fine = payment.getFine();

                    if (fine != null) {
                        // Gọi hàm applyPayment mà bạn đã định nghĩa sẵn trong Fine.java
                        fine.applyPayment(payment.getAmount());

                        // Lưu xuống Database
                        fineRepository.save(fine);

                        log.info("✅ Đã gạch nợ thành công cho phiếu phạt ID: {} (Giao dịch: {})",
                                fine.getId(), payment.getTransactionId());
                    } else {
                        log.warn("⚠️ Thanh toán phạt thành công nhưng không tìm thấy Fine ID trong hóa đơn (GD: {})",
                                payment.getTransactionId());
                    }
                    break;

                case MEMBERSHIP:
                    if (payment.getSubscription() != null) {
                        subscriptionService.activeSubscription(
                                payment.getSubscription().getId(),
                                payment.getId()
                        );
                        log.info("✅ Kích hoạt gói Subscription thành công cho GD: {}", payment.getTransactionId());
                    } else {
                        log.warn("⚠️ Thanh toán MEMBERSHIP nhưng không tìm thấy Subscription ID (GD: {})", payment.getTransactionId());
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý sự kiện thanh toán (GD: {}): {}", payment.getTransactionId(), e.getMessage(), e);
        }
    }
}
