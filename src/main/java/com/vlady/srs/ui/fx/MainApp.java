package com.vlady.srs.ui.fx;


import com.vlady.srs.application.usecase.AddWordUseCase;
import com.vlady.srs.application.usecase.GetDueWordsUseCase;
import com.vlady.srs.config.AppConfig;
import com.vlady.srs.domain.Word;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import static javafx.scene.layout.Priority.ALWAYS;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        AppConfig appConfig = AppConfig.getINSTANCE();
        var review = appConfig.reviewWord();
        AddWordUseCase add = appConfig.addWord();
        GetDueWordsUseCase getDue = appConfig.getDueWords();

        StackPane cardStack = new StackPane();

        Button buttonCorrect = new Button("Correct");
        Button buttonWrong = new Button("Wrong");
        Button buttonAdd = new Button("Add");
        HBox reviewBox = new HBox(10, buttonCorrect, buttonWrong);

        ToggleGroup group = new ToggleGroup();
        RadioButton rbDue = new RadioButton("Due");
        RadioButton rbAll = new RadioButton("All");
        rbDue.setToggleGroup(group);
        rbAll.setToggleGroup(group);
        rbDue.setSelected(true);
        HBox filterBox = new HBox(10, rbDue, rbAll);

        TextField textFieldFront = new TextField();
        TextField textFieldBack = new TextField();
        textFieldFront.setPromptText("Front (e.g., EN)");
        textFieldBack.setPromptText("Back (e.g., SV)");
        HBox addBox = new HBox(10, textFieldFront, textFieldBack, buttonAdd);
        HBox.setHgrow(textFieldFront, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(textFieldBack, javafx.scene.layout.Priority.ALWAYS);


        java.util.List<Word> deck = new java.util.ArrayList<>();


        java.util.function.BiFunction<Word, Integer, javafx.scene.Node> buildCard = (w, indexFromTop) -> {
            var front = new Label(w.getFront());
            front.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
            var back = new Label("→ " + w.getBack());
            back.setStyle("-fx-font-size: 16; -fx-opacity: 0.8;");
            var box = new VBox(8, front, back);
            box.setAlignment(Pos.CENTER_LEFT);

            var card = new StackPane(box);
            card.setPrefSize(520, 180);
            card.setStyle("""
                        -fx-background-color: white;
                        -fx-background-radius: 14;
                        -fx-padding: 16;
                        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 16, 0, 0, 6);
                    """);

            // визуальный «стек»
            card.setTranslateY(indexFromTop * 14);
            card.setScaleX(1.0 - indexFromTop * 0.04);
            card.setScaleY(1.0 - indexFromTop * 0.04);
            return card;
        };

        // Обновление данных + перерисовка стека
        Runnable refresh = () -> {
            deck.clear();
            if (rbAll.isSelected()) {
                var all = appConfig.getAllWords().execute();
                all.sort(java.util.Comparator.comparing(Word::getNextReviewDate));
                deck.addAll(all);
            } else {
                deck.addAll(getDue.execute(LocalDate.now()));
            }

            renderDeck(cardStack, deck, buildCard);

            // Перед ручной установкой — на всякий случай снимаем возможные биндинги:
            buttonCorrect.disableProperty().unbind();
            buttonWrong.disableProperty().unbind();

            // Дизейблим, если колода пуста, ИЛИ если выбран режим All (если так задумано)
            boolean disable = deck.isEmpty() || rbAll.isSelected();  // если в All нельзя ревью
            // если в All ревью разрешён — оставь только deck.isEmpty()
            // boolean disable = deck.isEmpty();

            buttonCorrect.setDisable(disable);
            buttonWrong.setDisable(disable);
        };



        // первичный рендер
        refresh.run();

        // слушатель переключателя фильтра
        group.selectedToggleProperty().addListener((obs, o, n) -> refresh.run());
        buttonCorrect.disableProperty().bind(javafx.beans.binding.Bindings.size(
                javafx.collections.FXCollections.observableList(deck)).isEqualTo(0));
        buttonWrong.disableProperty().bind(javafx.beans.binding.Bindings.size(
                javafx.collections.FXCollections.observableList(deck)).isEqualTo(0));

        // Обработчики кнопок
        buttonCorrect.setOnAction(e -> {
            if (deck.isEmpty()) return;
            var w = deck.get(0);
            review.execute(w, true);
            refresh.run();
        });

        buttonWrong.setOnAction(e -> {
            if (deck.isEmpty()) return;
            var w = deck.get(0);
            review.execute(w, false);
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

        // ---- LAYOUT ----
        VBox bottom = new VBox(10, reviewBox, addBox);
        bottom.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(filterBox);
        BorderPane.setMargin(filterBox, new Insets(8));
        root.setCenter(cardStack);
        root.setBottom(bottom);
        root.setPadding(new Insets(12));

        cardStack.setPrefSize(520, 300);
        cardStack.setMinHeight(300);
        cardStack.setStyle("-fx-background-color: transparent;");
        reviewBox.setPadding(new Insets(8, 8, 0, 0));

        var scene = new javafx.scene.Scene(root, 640, 520);
        stage.setScene(scene);
        stage.setTitle("SRS — Due today");
        stage.show();
    }

    // Рисуем верхние 3 карточки (снизу-вверх, чтобы верхняя была последней)
    private void renderDeck(StackPane cardStack,
                            java.util.List<Word> deck,
                            java.util.function.BiFunction<Word, Integer, javafx.scene.Node> buildCard) {
        cardStack.getChildren().clear();
        int n = Math.min(3, deck.size());
        for (int i = n - 1; i >= 0; i--) {
            Word w = deck.get(i);
            int indexFromTop = n - 1 - i; // 0 = верхняя
            cardStack.getChildren().add(buildCard.apply(w, indexFromTop));
        }
    }
}
