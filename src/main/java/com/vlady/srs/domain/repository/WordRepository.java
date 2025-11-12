package com.vlady.srs.domain.repository;

import com.vlady.srs.domain.Word;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface WordRepository {

    void save(Word word);

    Optional<Word> findById(String id);

    List<Word> findAll();

    List<Word> findDue(LocalDate date);

    void delete(String id);
}
