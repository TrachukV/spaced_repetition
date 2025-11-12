package com.vlady.srs.application.usecase;

import com.vlady.srs.domain.Word;
import com.vlady.srs.domain.repository.WordRepository;

public class ReviewWordUseCase {
    private final WordRepository repo;

    public ReviewWordUseCase(WordRepository repo) {
        this.repo = repo;
    }
    public Word execute(Word word, boolean correct) {
        word.review(correct);
        repo.save(word);
        return word;
    }
}
