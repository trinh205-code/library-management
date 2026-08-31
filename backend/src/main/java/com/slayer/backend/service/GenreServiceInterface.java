package com.slayer.backend.service;

import com.slayer.backend.exception.GenreException;
import com.slayer.backend.payload.dto.GenreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenreServiceInterface {

    GenreDTO createGenre(GenreDTO genreDTO);
    List<GenreDTO> getAllGenres();

    GenreDTO getGenreById(Long genreId) throws GenreException;
    GenreDTO updateGenre(Long genreId, GenreDTO genreDTO) throws GenreException;
    void deleteGenre(Long genreId) throws GenreException;

    void handleDeleteGenre(Long genreId) throws GenreException;

    List<GenreDTO> getAllActiveGenresWithSubGenres();

    List<GenreDTO> getTopLevelGenres();

    Page<GenreDTO> searchGenres(String searchTerm, Pageable pageable);

    long getTotalActiveGenres();

    long getBookCountByGenre(Long genreId);

}
