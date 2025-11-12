package com.vlady.srs.ui.fx;

import com.vlady.srs.application.usecase.AddWordUseCase;
import com.vlady.srs.application.usecase.GetDueWordsUseCase;
import com.vlady.srs.config.AppConfig;
import com.vlady.srs.domain.Word;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;

public class MainApp extends Application {

    private BorderPane root;

    @Override
    public void start(Stage stage) {
        AppConfig appConfig = AppConfig.getINSTANCE();
        var review   = appConfig.reviewWord();
        AddWordUseCase add = appConfig.addWord();
        GetDueWordsUseCase getDue = appConfig.getDueWords();
        var deleteWord = appConfig.deleteWord();


        StackPane cardStack = new StackPane();
        ListView<Word> allList = new ListView<>();
        allList.setPlaceholder(new Label("Empty for now"));

        Button buttonCorrect = new Button("Correct");
        Button buttonWrong   = new Button("Wrong");
        Button buttonAdd     = new Button("Add");
        HBox reviewBox = new HBox(10, buttonCorrect, buttonWrong);

        ToggleGroup group = new ToggleGroup();
        RadioButton rbDue = new RadioButton("Due");
        RadioButton rbAll = new RadioButton("All");
        rbDue.setToggleGroup(group);
        rbAll.setToggleGroup(group);
        rbDue.setSelected(true);
        HBox filterBox = new HBox(10, rbDue, rbAll);

        TextField textFieldFront = new TextField();
        TextField textFieldBack  = new TextField();
        textFieldFront.setPromptText("Front (e.g., EN)");
        textFieldBack.setPromptText("Back (e.g., SV)");
        HBox addBox = new HBox(10, textFieldFront, textFieldBack, buttonAdd);
        HBox.setHgrow(textFieldFront, Priority.ALWAYS);
        HBox.setHgrow(textFieldBack, Priority.ALWAYS);

        List<Word> deck = new ArrayList<>();
        ObservableList<Word> allWordsObs = FXCollections.observableArrayList();
        allList.setItems(allWordsObs);

        BiFunction<Word, Integer, Node> buildCard = (w, indexFromTop) -> {
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

            card.setTranslateY(indexFromTop * 14);
            card.setScaleX(1.0 - indexFromTop * 0.04);
            card.setScaleY(1.0 - indexFromTop * 0.04);
            return card;
        };

        allList.setCellFactory(lv -> new ListCell<>() {
            private final Label  lbl = new Label();
            private final Button btnDelete = new Button("Delete");
            private final HBox   box = new HBox(10, lbl, btnDelete);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(lbl, Priority.ALWAYS);

                btnDelete.setOnAction(e -> {
                    Word w = getItem();
                    if (w == null) return;

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete word");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Delete \"" + w.getFront() + " — " + w.getBack() + "\"?");
                    Optional<ButtonType> res = confirm.showAndWait();

                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        deleteWord.execute(w.getId());
                        getListView().getItems().remove(w);
                    }
                });
            }

            @Override
            protected void updateItem(Word w, boolean empty) {
                super.updateItem(w, empty);
                if (empty || w == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String date = (w.getNextReviewDate() != null)
                            ? w.getNextReviewDate().format(DateTimeFormatter.ISO_DATE)
                            : "-";
                    lbl.setText(w.getFront() + " — " + w.getBack() + "    (next: " + date + ")");
                    setText(null);
                    setGraphic(box);
                }
            }
        });

        VBox bottom = new VBox(10, reviewBox, addBox);
        bottom.setPadding(new Insets(10));

        root = new BorderPane();
        root.setTop(filterBox);
        BorderPane.setMargin(filterBox, new Insets(8));
        root.setCenter(cardStack);
        root.setBottom(bottom);
        root.setPadding(new Insets(12));

        cardStack.setPrefSize(520, 300);
        cardStack.setMinHeight(300);
        cardStack.setStyle("-fx-background-color: transparent;");
        reviewBox.setPadding(new Insets(8, 8, 0, 0));

        Runnable refresh = () -> {
            deck.clear();

            if (rbAll.isSelected()) {
                List<Word> all = appConfig.getAllWords().execute();
                all.sort(Comparator
                        .comparing(Word::getFront, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Word::getBack, String.CASE_INSENSITIVE_ORDER));
                allWordsObs.setAll(all);
                root.setCenter(allList);
            } else {
                deck.addAll(getDue.execute(LocalDate.now()));
                deck.sort(Comparator.comparing(Word::getNextReviewDate,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                root.setCenter(cardStack);
                renderDeck(cardStack, deck, buildCard);
            }

            boolean disable = rbAll.isSelected() || deck.isEmpty();
            buttonCorrect.setDisable(disable);
            buttonWrong.setDisable(disable);
        };


        refresh.run();

        group.selectedToggleProperty().addListener((obs, o, n) -> refresh.run());


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
            String back  = textFieldBack.getText().trim();
            if (front.isEmpty() || back.isEmpty()) return;
            add.execute(front, back);
            textFieldFront.clear();
            textFieldBack.clear();
            refresh.run();
        });

        Scene scene = new Scene(root, 640, 520);
        stage.setScene(scene);
        stage.setTitle("SRS");
        stage.show();
    }

    private void renderDeck(StackPane cardStack,
                            List<Word> deck,
                            BiFunction<Word, Integer, Node> buildCard) {
        cardStack.getChildren().clear();
        int n = Math.min(3, deck.size());
        for (int i = n - 1; i >= 0; i--) {
            Word w = deck.get(i);
            int indexFromTop = n - 1 - i;
            cardStack.getChildren().add(buildCard.apply(w, indexFromTop));
        }
    }
}
