package com.vlady.srs.domain;

import java.time.LocalDate;

public class Word {
    private final String id;
    private final String front;
    private final String back;
    private int intervalDays;
    private  int ease;
    private LocalDate nextReviewDate;

    public Word(String id, String front, String back) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.intervalDays = 1;
        this.ease = 200;
        this.nextReviewDate = LocalDate.now();
    }


    public String getId() {
        return id;
    }

    public String getFront() {
        return front;
    }
    public String getBack() {
        return back;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public int getEase() {
        return ease;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public void review(boolean correct) {
        if (correct) {
            intervalDays = Math.max(1, (int)Math.round(intervalDays * (ease / 100.0)));
            ease = Math.min(300, ease + 10);
        } else {
            intervalDays = 1;
            ease = Math.max(130, ease - 20);
        }
        nextReviewDate = LocalDate.now().plusDays(intervalDays);
    }

    public String toString() {
        return "%s → %s (I=%d, E=%d, next=%s)".formatted(front, back, intervalDays, ease, nextReviewDate);
    }

}

