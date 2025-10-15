package com.vlady.srs.ui.fx;


import com.vlady.srs.application.usecase.AddWordUseCase;
import com.vlady.srs.application.usecase.GetDueWordsUseCase;
import com.vlady.srs.config.AppConfig;
import com.vlady.srs.domain.Word;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;

import static javafx.scene.layout.Priority.ALWAYS;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        AppConfig appConfig = AppConfig.getINSTANCE();
        AddWordUseCase add = appConfig.addWord();
        GetDueWordsUseCase getDue = appConfig.getDueWords();

        ListView<Word> listView = new ListView<Word>();

        Button buttonCorrect = new Button("Correct");

        Button buttonWrong = new Button("Wrong");
        Button buttonAdd = new Button("Add");
        HBox reviewBox = new HBox(10, buttonCorrect, buttonWrong);


        TextField textFieldFront = new TextField();
        TextField textFieldBack = new TextField();
        textFieldBack.setPromptText("Back (e.g., SV)");
        textFieldFront.setPromptText("Front (e.g., EN)");


        listView.setCellFactory(new Callback<ListView<Word>, ListCell<Word>>() {
            @Override
            public ListCell<Word> call(ListView<Word> list) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Word w, boolean empty) {
                        super.updateItem(w, empty);
                        if (empty || w == null) {
                            setText(null);
                        } else {
                            setText(w.getId().substring(0, 8) + " | " + w.getFront() + " → " + w.getBack());
                        }
                    }
                };
            }
        });
        Runnable refresh = () -> {
            var due = getDue.execute(LocalDate.now());
            listView.setItems(FXCollections.observableArrayList(due));
        };
        refresh.run();

        buttonCorrect.setOnAction(e -> {
            Word word = listView.getSelectionModel().getSelectedItem();
            if (word == null) return;
            word.review(true);
            refresh.run();
        });
        buttonWrong.setOnAction(e -> {
            Word w = listView.getSelectionModel().getSelectedItem();
            if (w == null) return;
            w.review(false);
            refresh.run();
        });
        buttonAdd.setOnAction(e -> {
            String front = textFieldFront.getText().trim();
            String back = textFieldBack.getText().trim();
            if (front.isEmpty() || back.isEmpty()) return;
            add.execute(front, back);
            textFieldFront.clear();
            textFieldBack.clear();
            refresh.run();
        });
        HBox addBox = new HBox(10, textFieldFront, textFieldBack, buttonAdd);
        HBox.setHgrow(textFieldFront, ALWAYS);
        HBox.setHgrow(textFieldBack, ALWAYS);
        VBox bottom = new VBox(10, reviewBox, addBox);


        BorderPane root = new BorderPane();
        root.setCenter(listView);
        root.setBottom(bottom);
        root.setPadding(new javafx.geometry.Insets(12));


        listView.setPrefHeight(320);
        reviewBox.setPadding(new Insets(8, 8, 0, 0));
        stage.setScene(new javafx.scene.Scene(root, 640, 520));
        stage.setTitle("SRS — Due today");
        stage.show();
    }

}
