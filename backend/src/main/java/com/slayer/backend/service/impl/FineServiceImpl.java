package com.slayer.backend.service.impl;

import com.slayer.backend.Enums.FineStatus;
import com.slayer.backend.Enums.FineType;
import com.slayer.backend.Enums.PaymentGateway;
import com.slayer.backend.Enums.PaymentType;
import com.slayer.backend.entity.BookLoan;
import com.slayer.backend.entity.Fine;
import com.slayer.backend.entity.User;
import com.slayer.backend.mapper.FineMapper;
import com.slayer.backend.payload.dto.FineDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.payload.request.FineRequest;
import com.slayer.backend.payload.request.PaymentInitiateRequest;
import com.slayer.backend.payload.request.WaiveFineRequest;
import com.slayer.backend.repository.BookLoanRepository;
import com.slayer.backend.repository.FineRepository;
import com.slayer.backend.service.FineServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineServiceInterface {

    private final FineRepository fineRepository;
    private final BookLoanRepository bookLoanRepository;
    private final FineMapper fineMapper;
    private final UserService userService;
    private final PaymentServiceImpl paymentService;

    @Override
    public FineDTO createFine(FineRequest request) {

        BookLoan bookLoan = bookLoanRepository.findById(request.getBookLoanId()).orElseThrow(
                ()-> new RuntimeException("bookLoanId not found")
        );

        Fine fine = Fine.builder()
                .bookLoan(bookLoan)
                .user(bookLoan.getUser())
                .type(request.getType())
                .amount(request.getAmount())
                .status(FineStatus.PENDING)
                .reason(request.getReason())
                .note(request.getNote())
                .build();

        Fine saved = fineRepository.save(fine);

        return fineMapper.toDTO(saved);
    }

    @Override
    public PaymentInitiateResponse payFine(Long fineId, String transactionId) throws Exception {

        Fine fine = fineRepository.findById(fineId).orElseThrow(
                () -> new RuntimeException("fineId not found")
        );

        if (FineStatus.PAID.equals(fine.getStatus())) {
            throw new RuntimeException("fine already paid");
        }

        if (FineStatus.WAIVED.equals(fine.getStatus())) {
            throw new RuntimeException("fine waived");
        }

        User user = userService.getUser();

        PaymentInitiateRequest paymentRequest = PaymentInitiateRequest.builder()
                .userId(user.getId())
                .fineId(fine.getId())
                .paymentType(PaymentType.FINE)
                .paymentGateway(PaymentGateway.VNPAY)
                .amount(fine.getAmount())
                .description("Library fine payment")
                .build();
        return paymentService.initiatePayment(paymentRequest, user);
    }

    @Override
    public void markFineAsPaid(Long fineId, Long amount, String transactionId) {
        Fine fine = fineRepository.findById(fineId).orElseThrow(
                () -> new RuntimeException("fineId not found with id: " + fineId)
        );

        fine.applyPayment(amount);
        fine.setTransactionId(transactionId);
        fine.setStatus(FineStatus.PAID);
        fine.setUpdatedAt(LocalDateTime.now());

        fineRepository.save(fine);
    }

    @Override
    @Transactional
    public FineDTO waiveFine(WaiveFineRequest request) throws Exception {
        Fine fine = fineRepository.findById(request.getFineId()).orElseThrow(
                () -> new RuntimeException("Fine not found with id: " + request.getFineId())
        );

        if(fine.getStatus().equals(FineStatus.WAIVED)) {
            throw new RuntimeException("Fine is already waived");
        }

        if(fine.getStatus().equals(FineStatus.PAID)) {
            throw new RuntimeException("Fine is already paid");
        }

        User userAdmin = userService.getUser();

        // 1. Gọi hàm nghiệp vụ trong Entity để cập nhật trạng thái (Giả sử request có getReason())
        fine.waive(userAdmin, request.getReason());

        // 2. (Tùy chọn) Cập nhật thêm người xử lý giao dịch này
//        fine.setProcessedBy(userAdmin);

        Fine savedFine = fineRepository.save(fine);

        return fineMapper.toDTO(savedFine);
    }

    @Override
    public List<FineDTO> getMyFines(FineStatus status, FineType type) throws Exception {
        User currentUser = userService.getUser();

        List<Fine> fines = fineRepository.findByUserAndOptionalFilters(currentUser, status, type);

        return fines.stream()
                .map(fineMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<FineDTO> getAllFines(FineStatus status, FineType type, Long userId, int page, int size) {

        int pageNo = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNo, size);

        Page<Fine> finePage = fineRepository.findAllWithFilters(userId, status, type, pageable);

        List<FineDTO> content = finePage.getContent().stream()
                .map(fineMapper::toDTO)
                .collect(Collectors.toList());


        return PageResponse.<FineDTO>builder()
                .content(content)
                .pageNumber(finePage.getNumber() + 1)
                .pageSize(finePage.getSize())
                .totalPages(finePage.getTotalPages())
                .totalElements(finePage.getTotalElements())
                .last(finePage.isLast())
                .first(finePage.isFirst())
                .empty(finePage.isEmpty())
                .build();
    }


}
