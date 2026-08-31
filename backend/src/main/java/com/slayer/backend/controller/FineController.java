package com.slayer.backend.controller;

import com.slayer.backend.Enums.FineStatus;
import com.slayer.backend.Enums.FineType;
import com.slayer.backend.payload.dto.FineDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.payload.request.FineRequest;
import com.slayer.backend.payload.request.WaiveFineRequest;
import com.slayer.backend.service.impl.FineServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fines")
public class FineController {

    private final FineServiceImpl fineService;



    @PostMapping
    public ResponseEntity<FineDTO> createFine(@Valid @RequestBody FineRequest request) {
        FineDTO response = fineService.createFine(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<FineDTO>> getMyFines(
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) FineType type
    ) throws Exception {
        return ResponseEntity.ok(fineService.getMyFines(status, type));
    }

    @GetMapping
    public ResponseEntity<PageResponse<FineDTO>> getAllFines(
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) FineType type,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(fineService.getAllFines(status, type, userId, page, size));
    }

    @PostMapping("/waive")
    public ResponseEntity<?> waiveFine(@Valid @RequestBody WaiveFineRequest request) {
        try {
            FineDTO response = fineService.waiveFine(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }




    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payFine(
            @PathVariable Long id,
            @RequestParam(required = false) String transactionId
    ) {
        try {
            PaymentInitiateResponse response = fineService.payFine(id, transactionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * VNPAY RETURN URL / IPN (Rất quan trọng)
     * Đây là API mà VNPay sẽ redirect về (hoặc gọi ngầm) sau khi user thanh toán thành công.
     * Tùy thuộc vào code cấu hình VNPay của bạn, tham số nhận vào sẽ là các @RequestParam (vnp_TxnRef, vnp_Amount, vnp_ResponseCode...)
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(
            @RequestParam("vnp_TxnRef") Long fineId, // Ví dụ bạn dùng TxnRef làm fineId
            @RequestParam("vnp_Amount") Long amount,
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TransactionNo") String transactionNo
    ) {
        // "00" là mã thành công của VNPay
        if ("00".equals(responseCode)) {
            // Số tiền VNPay trả về thường được nhân 100, cần chia lại
            Long actualAmount = amount / 100;

            // Gọi hàm Service cập nhật trạng thái
            fineService.markFineAsPaid(fineId, actualAmount, transactionNo);

            return ResponseEntity.ok("Thanh toán thành công!");
            // Thực tế FE thường yêu cầu redirect về 1 trang Success thay vì chỉ trả text
        } else {
            return ResponseEntity.badRequest().body("Thanh toán thất bại hoặc bị hủy");
        }
    }

}
