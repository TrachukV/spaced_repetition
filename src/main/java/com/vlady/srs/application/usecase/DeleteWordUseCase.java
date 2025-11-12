package com.vlady.srs.application.usecase;

import com.vlady.srs.domain.repository.WordRepository;

public class DeleteWordUseCase {
    private final WordRepository repo;

    public DeleteWordUseCase(WordRepository repo) {
        this.repo = repo;
    }


    public void execute(String id) {
        repo.delete(id);
    }

}
