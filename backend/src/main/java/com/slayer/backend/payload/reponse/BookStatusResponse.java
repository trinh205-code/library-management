package com.slayer.backend.payload.reponse;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookStatusResponse {

    private long totalActiveBooks;
    private long totalAvailableBooks;
}
