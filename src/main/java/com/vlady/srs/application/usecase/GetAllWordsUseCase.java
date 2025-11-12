package com.vlady.srs.application.usecase;

import com.vlady.srs.domain.Word;
import com.vlady.srs.domain.repository.WordRepository;

import java.util.List;

public class GetAllWordsUseCase {
    private final WordRepository repo;

    public GetAllWordsUseCase(WordRepository repo) { this.repo = repo; }

    public List<Word> execute() { return repo.findAll(); }

}
