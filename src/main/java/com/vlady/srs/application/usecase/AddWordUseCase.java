package com.vlady.srs.application.usecase;

import com.vlady.srs.domain.Word;
import com.vlady.srs.domain.repository.WordRepository;

import java.util.UUID;

public class AddWordUseCase {
    private final WordRepository repo;


    public AddWordUseCase(WordRepository repo) {
        this.repo = repo;
    }
    public Word execute(String front, String back) {
        Word w = new Word(UUID.randomUUID().toString(), front, back);
        repo.save(w);
        return w;
    }
}
