package com.vlady.srs.application.usecase;

import com.vlady.srs.domain.Word;
import com.vlady.srs.domain.repository.WordRepository;

import java.time.LocalDate;
import java.util.List;

public class GetDueWordsUseCase {
    private final WordRepository repo;

    public GetDueWordsUseCase(WordRepository repo) {
        this.repo = repo;
    }

    public List<Word> execute(LocalDate date) {
        return repo.findDue(date);
    }
}
