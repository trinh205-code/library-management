package com.slayer.backend.controller;

import com.slayer.backend.entity.Genre;
import com.slayer.backend.exception.GenreException;
import com.slayer.backend.payload.dto.GenreDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.service.GenreServiceInterface;
import com.slayer.backend.service.impl.GenreServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/genres")
public class GenreController {

    private final GenreServiceImpl genreService;


    @PostMapping("/create")
    public ResponseEntity<GenreDTO> addGenre(@RequestBody GenreDTO genre) {
        GenreDTO createGenre = genreService.createGenre(genre);

        return ResponseEntity.ok(createGenre);
    }

    @GetMapping
    public ResponseEntity<?> getAllGenres() {
        List<GenreDTO> genres = genreService.getAllGenres();
        return ResponseEntity.ok(genres);
    }


    @GetMapping("/{genreId}")
    public ResponseEntity<?> getGenreById(@PathVariable("genreId") Long genreId) throws GenreException {
        GenreDTO genres = genreService.getGenreById(genreId);
        return ResponseEntity.ok(genres);
    }

    @PutMapping("/{genreId}")
    public ResponseEntity<?> updateGenreByID(@PathVariable("genreId") Long genreId ,@RequestBody GenreDTO genre) throws GenreException {
        GenreDTO genres = genreService.updateGenre(genreId, genre);
        return ResponseEntity.ok(genres);
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<?> deleteGenreByID(@PathVariable("genreId") Long genreId) throws GenreException {
       genreService.deleteGenre(genreId);
        ApiResponse response = new ApiResponse("Genre Delete Successfully", true);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{genreId}/hard")
    public ResponseEntity<?> hardDeleteGenre(@PathVariable("genreId") Long genreId) throws GenreException {
        genreService.handleDeleteGenre(genreId);
        ApiResponse response = new ApiResponse("Genre Hard delete", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-level")
    public ResponseEntity<?> getTopLevelGenres() throws GenreException {
        List<GenreDTO> genres = genreService.getTopLevelGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getTotalActiveGenres() throws GenreException {
        Long genres = genreService.getTotalActiveGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/{id}/book-count")
    public ResponseEntity<?> getBookCountByGenres(
            @PathVariable Long id
    ) throws GenreException {
        Long count = genreService.getBookCountByGenre(id);
        return ResponseEntity.ok(count);
    }



}
