package com.vlady.srs;

import com.vlady.srs.config.AppConfig;
import com.vlady.srs.application.usecase.AddWordUseCase;
import com.vlady.srs.application.usecase.GetDueWordsUseCase;
import com.vlady.srs.domain.Word;

import java.time.LocalDate;
import java.util.List;

public class App {
    public static void main(String[] args) {
        var cfg = AppConfig.getINSTANCE();
        AddWordUseCase add = cfg.addWord();
        GetDueWordsUseCase due = cfg.getDueWords();

        Word w1 = add.execute("apple", "äpple");
        Word w2 = add.execute("house", "hus");
        Word w3 = add.execute("river", "älv");


        LocalDate today = LocalDate.now();
        List<Word> dueToday = due.execute(today);
        System.out.println("Due TODAY:");
        dueToday.forEach(System.out::println);


        w1.review(true);
        System.out.println("\nAfter reviewing 'apple' as CORRECT:");
        System.out.println(w1);

        LocalDate tomorrow = today.plusDays(1);
        List<Word> dueTomorrow = due.execute(tomorrow);
        System.out.println("\nDue TOMORROW:");
        dueTomorrow.forEach(System.out::println);
    }
}
