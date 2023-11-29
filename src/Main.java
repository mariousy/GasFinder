import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    private Controller controller;
    private TextField searchField;
    private Button searchButton, submitReviewButton;
    private ListView<GasStation> listView;
    private List<GasStation> gasStations = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new Controller();

        BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(20));
        rootLayout.setStyle("-fx-background-color: #f0f4f7;");

        Text personalInfo = new Text("Marious Yousif\nOakland University\nCSI 2300");
        personalInfo.setFont(Font.font("Arial", 12));
        HBox topSection = new HBox();
        topSection.setAlignment(Pos.TOP_RIGHT);
        topSection.getChildren().add(personalInfo);
        rootLayout.setTop(topSection);

        VBox centerSection = new VBox(10);
        centerSection.setAlignment(Pos.CENTER);

        Label programTitle = new Label("Gas Station Finder");
        programTitle.setFont(Font.font("Arial", 24));
        programTitle.setTextFill(Color.DARKSLATEBLUE);

        searchField = new TextField();
        searchField.setPromptText("Enter Zip Code");
        searchField.setMaxWidth(300);
        searchField.setFont(Font.font("Arial", 16));

        searchButton = new Button("Search");
        searchButton.setFont(Font.font("Arial", 14));
        searchButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");

        submitReviewButton = new Button("Submit Review");
        submitReviewButton.setFont(Font.font("Arial", 14));
        submitReviewButton.setDisable(true);  // Disabled by default

        listView = new ListView<>(FXCollections.observableArrayList(gasStations));
        listView.setPrefSize(600, 400);
        listView.setCellFactory(param -> new ListCell<GasStation>() {
            @Override
            protected void updateItem(GasStation gasStation, boolean empty) {
                super.updateItem(gasStation, empty);
                if (empty || gasStation == null) {
                    setText(null);
                } else {
                    setText(gasStation.getName() + " - " + gasStation.getAddress());
                }
            }
        });

        // Selection listener to enable the "Submit Review" button when a gas station is selected
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            submitReviewButton.setDisable(newVal == null);
        });

        listView.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                GasStation selectedGasStation = listView.getSelectionModel().getSelectedItem();
                if(selectedGasStation != null) {
                    showReviews(selectedGasStation);
                }
            }
        });

        HBox buttonSection = new HBox(10, searchButton, submitReviewButton);
        buttonSection.setAlignment(Pos.CENTER);

        centerSection.getChildren().addAll(programTitle, searchField, buttonSection, listView);
        rootLayout.setCenter(centerSection);

        searchButton.setOnAction(event -> handleSearch());

        submitReviewButton.setOnAction(event -> {
            GasStation selectedGasStation = listView.getSelectionModel().getSelectedItem();
            if (selectedGasStation != null) {
                showReviewDialog(selectedGasStation);
            }
        });

        Scene scene = new Scene(rootLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void handleSearch() {
        String zipCode = searchField.getText();
        searchButton.setDisable(true);

        Task<JSONArray> fetchStationsTask = new Task<JSONArray>() {
            @Override
            protected JSONArray call() throws JSONException {
                return controller.searchGasStations(zipCode);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateListView(getValue());
                searchButton.setDisable(false);
            }

            @Override
            protected void failed() {
                super.failed();
                getException().printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to fetch gas stations.");
                errorAlert.showAndWait();
                searchButton.setDisable(false);
            }
        };
        new Thread(fetchStationsTask).start();
    }

    private void updateListView(JSONArray stations) {
        gasStations.clear();
        for (int i = 0; i < stations.length(); i++) {
            try {
                JSONObject station = stations.getJSONObject(i);
                String stationName = station.optString("name", "Unknown Station");
                String stationAddress = station.optString("formatted_address", "No address available");
                gasStations.add(new GasStation(stationName, stationAddress));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        listView.setItems(FXCollections.observableArrayList(gasStations));
    }

    private void showReviewDialog(GasStation gasStation) {
        Dialog<Review> dialog = new Dialog<>();
        dialog.setTitle("Submit Review");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        Spinner<Double> ratingSpinner = new Spinner<>(1.0, 5.0, 5.0, 0.5);
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Your review");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Rating:"), 0, 1);
        grid.add(ratingSpinner, 1, 1);
        grid.add(new Label("Comment:"), 0, 2);
        grid.add(commentArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return new Review(nameField.getText(), ratingSpinner.getValue(), commentArea.getText());
            }
            return null;
        });

        Optional<Review> result = dialog.showAndWait();

        result.ifPresent(review -> {
            gasStation.addReview(review);
        });
    }

    private void showReviews(GasStation gasStation) {
        String reviewsText = gasStation.getReviews().stream()
                .map(review -> review.getName() + " (" + review.getRating() + "):\n" + review.getComment() + "\n")
                .reduce("", String::concat);

        Alert reviewsAlert = new Alert(Alert.AlertType.INFORMATION);
        reviewsAlert.setTitle("Reviews for " + gasStation.getName());
        reviewsAlert.setHeaderText("User Reviews");
        reviewsAlert.setContentText(reviewsText.isEmpty() ? "No reviews yet." : reviewsText);
        reviewsAlert.showAndWait();
    }
}
