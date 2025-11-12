package com.vlady.srs.config;

import com.vlady.srs.application.usecase.*;
import com.vlady.srs.domain.repository.WordRepository;
import com.vlady.srs.infrastructure.repo.FileWordRepository;

public class AppConfig {
    private static AppConfig INSTANCE;
    private final WordRepository wordRepository = new FileWordRepository();
    private final AddWordUseCase addWordUseCase = new AddWordUseCase(wordRepository);
    private final GetDueWordsUseCase getDueWordsUseCase = new GetDueWordsUseCase(wordRepository);
    private final DeleteWordUseCase deleteWordUseCase = new DeleteWordUseCase(wordRepository);


    private AppConfig() {
    }

    public static AppConfig getINSTANCE() {
        if (INSTANCE == null) INSTANCE = new AppConfig();
        return INSTANCE;
    }
    private final GetAllWordsUseCase getAll = new GetAllWordsUseCase(wordRepository);
    public GetAllWordsUseCase getAllWords() { return getAll; }

    private final ReviewWordUseCase reviewWordUseCase = new ReviewWordUseCase(wordRepository);

    public ReviewWordUseCase reviewWord() { return reviewWordUseCase; }

    public AddWordUseCase addWord() {
        return addWordUseCase;
    }

    public GetDueWordsUseCase getDueWords() {
        return getDueWordsUseCase;
    }

    public DeleteWordUseCase deleteWord() { return deleteWordUseCase; }
}
