package com.vlady.srs.infrastructure.repo;

import com.vlady.srs.domain.Word;
import com.vlady.srs.domain.repository.WordRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryWordRepository implements WordRepository {
    private final Map<String, Word> store = new LinkedHashMap<>();

    @Override
    public void save(Word word) {
        store.put(word.getId(), word);
    }

    @Override
    public Optional<Word> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Word> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Word> findDue(LocalDate date) {
        return store.values().stream()
                .filter(w -> !w.getNextReviewDate().isAfter(date))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}
