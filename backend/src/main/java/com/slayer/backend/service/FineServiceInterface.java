package com.slayer.backend.service;

import com.slayer.backend.Enums.FineStatus;
import com.slayer.backend.Enums.FineType;
import com.slayer.backend.payload.dto.FineDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.reponse.PaymentInitiateResponse;
import com.slayer.backend.payload.request.FineRequest;
import com.slayer.backend.payload.request.WaiveFineRequest;

import java.util.List;

public interface FineServiceInterface {
    FineDTO createFine(FineRequest request);

    PaymentInitiateResponse payFine(Long fineId, String transactionId) throws Exception;

    void markFineAsPaid(Long fineId, Long amount, String transactionId);

    FineDTO waiveFine(WaiveFineRequest request) throws Exception;

    List<FineDTO> getMyFines(FineStatus status, FineType type) throws Exception;

    PageResponse<FineDTO> getAllFines(FineStatus status, FineType type, Long userId, int page, int size);

}
