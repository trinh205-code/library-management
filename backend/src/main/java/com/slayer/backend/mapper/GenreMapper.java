package com.slayer.backend.mapper;

import com.slayer.backend.entity.Genre;
import com.slayer.backend.payload.dto.GenreDTO;
import com.slayer.backend.repository.GenreRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//@Mapper(componentModel = "spring")
@Component
@AllArgsConstructor
public class GenreMapper {

    private final GenreRepository genreRepository;

    public GenreDTO toDTO(Genre savedGenre) {
        if (savedGenre == null) {
            return null;
        }

        GenreDTO dto = GenreDTO.builder()
                .id(savedGenre.getId())
                .code(savedGenre.getCode())
                .name(savedGenre.getName())
                .description(savedGenre.getDescription())
                .displayOrder(savedGenre.getDisplayOrder())
                .active(savedGenre.isActive())
                .createdAt(savedGenre.getCreatedAt())
                .updatedAt(savedGenre.getUpdatedAt())
                .build();

        if (savedGenre.getParentGenre() != null) {
            dto.setParentGenreId(savedGenre.getParentGenre().getId());
            dto.setParentGenreName(savedGenre.getParentGenre().getName());
        }

        if (savedGenre.getSubGenres() != null && !savedGenre.getSubGenres().isEmpty()) {
            dto.setSubGenre(savedGenre.getSubGenres().stream()
                    .filter(subGenre -> subGenre.isActive())
                    .map(subGenre -> toDTO(subGenre))
                    .collect(Collectors.toList()));

        }

        return dto;
    }

    public Genre toEntity(GenreDTO genreDTO) {
        if (genreDTO == null) {
            return null;
        }

        Genre genre = Genre.builder()
                .code(genreDTO.getCode())
                .name(genreDTO.getName())
                .description(genreDTO.getDescription())
                .displayOrder(genreDTO.getDisplayOrder())
                .active(true)
                .build();

        if (genreDTO.getParentGenreId() != null) {
            genreRepository.findById(genreDTO.getParentGenreId())
                    .ifPresent(genre::setParentGenre);
        }

        return genre;
    }

    public void updateEntityFromDTO(GenreDTO genreDTO, Genre genre) {
        if (genreDTO == null || genre == null) {
            return;
        }
        genre.setCode(genreDTO.getCode());
        genre.setName(genreDTO.getName());
        genre.setDescription(genreDTO.getDescription());
        genre.setDisplayOrder(genreDTO.getDisplayOrder() != null ? genreDTO.getDisplayOrder() : 0);
        if (genreDTO.getActive() != null) {
            genre.setActive(genreDTO.getActive());
        }
        if (genreDTO.getParentGenreId() != null) {
            genreRepository.findById(genreDTO.getParentGenreId())
                    .ifPresent(genre::setParentGenre);
        }
    }

    public List<GenreDTO> toDTOList(List<Genre> genreList) {
        return genreList.stream().map(
                genre -> toDTO(genre))
                .collect(Collectors.toList());
    }
}
