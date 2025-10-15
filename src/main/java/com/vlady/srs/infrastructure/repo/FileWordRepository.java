package com.vlady.srs.infrastructure.repo;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vlady.srs.domain.Word;
import com.vlady.srs.domain.repository.WordRepository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FileWordRepository implements WordRepository {

    private final Path filePath;
    private final Gson gson;
    private final Map<String, Word> store = new LinkedHashMap<>();

    public FileWordRepository() {
        this(Paths.get(System.getProperty("user.home"), ".srs", "words.json"));
    }

    public FileWordRepository(Path filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                        (json, type, ctx) -> LocalDate.parse(json.getAsString()))
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
                        (date, type, ctx) -> new JsonPrimitive(date.toString()))
                .create();
        loadFromDisk();
    }

    @Override
    public synchronized void save(Word word) {
        store.put(word.getId(), word);
        persist();
    }

    @Override
    public synchronized Optional<Word> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized List<Word> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized List<Word> findDue(LocalDate date) {
        return store.values().stream()
                .filter(w -> !w.getNextReviewDate().isAfter(date))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void delete(String id) {
        store.remove(id);
        persist();
    }

    private void loadFromDisk() {
        try {
            Files.createDirectories(filePath.getParent());
            if (Files.exists(filePath)) {
                String json = Files.readString(filePath);
                Type listType = new TypeToken<List<Word>>() {

                }.getType();
                List<Word> words = gson.fromJson(json, listType);
                if (words != null) {
                    for (Word w : words) {
                        store.put(w.getId(), w);
                    }
                }
            } else {
                persist();
            }
        } catch (IOException e) {
            System.err.println("Failed to load words: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            Files.createDirectories(filePath.getParent());
            List<Word> words = new ArrayList<>(store.values());
            String json = gson.toJson(words);
            Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save words: " + e.getMessage());
        }
    }
}
