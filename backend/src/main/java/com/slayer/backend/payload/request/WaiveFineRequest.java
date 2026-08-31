package com.slayer.backend.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaiveFineRequest {

    @NotNull(message = "Fine Id is required")
    private Long fineId;

    @NotNull(message = "Waiver reason is required")
    private String reason;

}
