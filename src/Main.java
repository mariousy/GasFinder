import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Main extends Application {

    private Controller controller;
    private TextField searchField;
    private Button searchButton;
    private ListView<String> listView;
    private ProgressIndicator progressIndicator;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new Controller();

        // Root layout
        BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(20));
        rootLayout.setStyle("-fx-background-color: #f0f4f7;");

        // Personal and course information
        Text personalInfo = new Text("Marious Yousif\nOakland University\nCSI 2300");
        personalInfo.setFont(Font.font("Arial", 12));
        HBox topSection = new HBox();
        topSection.setAlignment(Pos.TOP_RIGHT);
        topSection.getChildren().add(personalInfo);
        rootLayout.setTop(topSection);

        // Center section
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

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false); // Hidden by default

        listView = new ListView<>();
        listView.setPrefSize(600, 400);

        centerSection.getChildren().addAll(programTitle, searchField, searchButton, progressIndicator, listView);
        rootLayout.setCenter(centerSection);

        // Event handlers
        searchButton.setOnAction(event -> handleSearch());

        Scene scene = new Scene(rootLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSearch() {
        String zipCode = searchField.getText();
        searchButton.setDisable(true);
        progressIndicator.setVisible(true); // Show the progress indicator

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
                progressIndicator.setVisible(false); // Hide the progress indicator
            }

            @Override
            protected void failed() {
                super.failed();
                getException().printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to fetch gas stations.");
                errorAlert.showAndWait();
                searchButton.setDisable(false);
                progressIndicator.setVisible(false); // Hide the progress indicator
            }
        };
        new Thread(fetchStationsTask).start();
    }

    private void updateListView(JSONArray stations) {
        listView.getItems().clear();
        for (int i = 0; i < stations.length(); i++) {
            try {
                JSONObject station = stations.getJSONObject(i);
                String stationName = station.optString("name", "Unknown Station");
                String stationAddress = station.optString("formatted_address", "No address available");
                String listItem = stationName + " - " + stationAddress;
                listView.getItems().add(listItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    // In the Main class:

private void showReviewDialog(GasStation gasStation) {
    // Create the custom dialog.
    Dialog<Review> dialog = new Dialog<>();
    dialog.setTitle("Submit Review");

    // Set the button types.
    ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

    // Create the username and rating fields.
    TextField usernameField = new TextField();
    usernameField.setPromptText("Username");
    Spinner<Double> ratingSpinner = new Spinner<>(0.0, 5.0, 5.0, 0.5);
    TextArea commentArea = new TextArea();
    commentArea.setPromptText("Your review");

    // Layout the dialog components.
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.add(new Label("Username:"), 0, 0);
    grid.add(usernameField, 1, 0);
    grid.add(new Label("Rating:"), 0, 1);
    grid.add(ratingSpinner, 1, 1);
    grid.add(new Label("Comment:"), 0, 2);
    grid.add(commentArea, 1, 2);

    dialog.getDialogPane().setContent(grid);

    // Request focus on the username field by default.
    Platform.runLater(() -> usernameField.requestFocus());

    // Convert the result to a Review when the submit button is clicked.
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == submitButtonType) {
            return new Review(usernameField.getText(), ratingSpinner.getValue(), commentArea.getText());
        }
        return null;
    });

    Optional<Review> result = dialog.showAndWait();

    result.ifPresent(review -> {
        gasStation.addReview(review);
        updateListViewWithReviews(gasStation);
    });
}

private void updateListViewWithReviews(GasStation gasStation) {
    listView.getItems().clear();
    for (Review review : gasStation.getReviews()) {
        listView.getItems().add(review.getUsername() + " (" + review.getRating() + "): " + review.getComment());
    }
}

}
