package com.vlady.srs.ui.fx;


import com.vlady.srs.application.usecase.AddWordUseCase;
import com.vlady.srs.application.usecase.GetDueWordsUseCase;
import com.vlady.srs.config.AppConfig;
import com.vlady.srs.domain.Word;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.time.LocalDate;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        AppConfig appConfig = AppConfig.getINSTANCE();
        AddWordUseCase add = appConfig.addWord();
        GetDueWordsUseCase getDue = appConfig.getDueWords();
        add.execute("apple", "äpple");
        add.execute("house", "hus");
        add.execute("river", "älv");

        ListView<Word> listView = new ListView<Word>();
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


        Button buttonCorrect = new Button("Correct");
        Button buttonWrong = new Button("Wrong");

        buttonCorrect.setOnAction(e -> {
            Word word = listView.getSelectionModel().getSelectedItem();
            if (word == null) return;
            word.review(true);
            refresh.run();
        });
        buttonWrong.setOnAction(e -> {
            var w = listView.getSelectionModel().getSelectedItem();
            if (w == null) return;
            w.review(false);
            refresh.run();
        });
        var buttons = new javafx.scene.layout.HBox(10, buttonCorrect, buttonWrong);
        var root = new javafx.scene.layout.VBox(10, listView, buttons);
        root.setPadding(new javafx.geometry.Insets(12));

        stage.setTitle("SRS — Due today");
        stage.setScene(new javafx.scene.Scene(root, 520, 380));
        stage.show();
    }

}
