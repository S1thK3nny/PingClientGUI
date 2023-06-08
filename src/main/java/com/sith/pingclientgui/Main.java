package com.sith.pingclientgui;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.regex.Pattern;

public class Main extends Application {
    ComboBox<String> boardsDropDown;

    HBox mainHBox;

    VBox vBoxLeftSide;
    VBox vBoxName;
    VBox vBoxThumbURL;
    VBox vBoxDropDown;
    VBox vBoxRefID;

    VBox vBoxRightSide;
    VBox vBoxContent;

    int wWidth = 1280;
    int wHeight = 720;

    StackPane root;

    String selectBoardOrFail = "Select a board";

    TextArea textAreaContent;

    TextField textFieldName;
    TextField textFieldThumbURL;
    TextField textFieldDropDown;
    TextField textFieldRefID;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        //sendPost();
        root = new StackPane();
        configureWindow(stage);

        root.getChildren().addAll(mainHBox);

        Scene scene = new Scene(root, wWidth, wHeight);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/fontstyle.css")).toExternalForm());

        stage.setTitle("PingClient: GUI");
        stage.setMinWidth(624);
        stage.setMinHeight(352);
        stage.setScene(scene);
        stage.show();
    }



    public void configureWindow(Stage stage) throws Exception {
        mainHBox = new HBox();
        mainHBox.setAlignment(Pos.CENTER);
        mainHBox.setMinWidth(wWidth);
        mainHBox.setMinHeight(wHeight);

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            mainHBox.setMinWidth((Double) newVal);
            vBoxLeftSide.setPrefWidth(((Double) newVal)/2);
            vBoxRightSide.setPrefWidth(((Double) newVal/2) - ((Double) newVal/5));

            vBoxName.setMaxWidth(((Double) newVal)/2);
            vBoxThumbURL.setMaxWidth(((Double) newVal)/2);
            vBoxDropDown.setMaxWidth(((Double) newVal)/2);
            vBoxRefID.setMaxWidth(((Double) newVal)/2);

            textFieldName.setMaxWidth(vBoxName.getMaxWidth());
            textFieldThumbURL.setMaxWidth(vBoxThumbURL.getMaxWidth());
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> mainHBox.setMinHeight((Double) newVal));

        vBoxLeftSide = new VBox();
        vBoxRightSide = new VBox();

        configureLeftSide();
        configureRightSide();

        mainHBox.getChildren().addAll(vBoxLeftSide, vBoxRightSide);
    }



    public void configureLeftSide() throws Exception {
        //vBoxLeftSide.setStyle("-fx-border-color: blue");
        vBoxLeftSide.setAlignment(Pos.CENTER);
        vBoxLeftSide.setSpacing(30);
        HBox.setHgrow(vBoxLeftSide, Priority.ALWAYS);
        vBoxLeftSide.setPadding(new Insets(0,0,0,20));

        configureUserName();
        configureThumbUrl();
        configureDropDown();
        configureRefID();
    }

    public void configureUserName() {
        Text username = new Text("Username");

        textFieldName = createLeftSideTextField("Got a name, stranger?");

        vBoxName = createDefaultVBox();
        vBoxName.getChildren().addAll(username, textFieldName);
        vBoxLeftSide.getChildren().add(vBoxName);
    }

    public void configureThumbUrl() {
        Text text = new Text("Thumb URL");

        textFieldThumbURL = createLeftSideTextField("Got an image? Share it!");
        textFieldThumbURL.setTextFormatter(formatBoard()); //Eh, why not. Only needs links anyway

        vBoxThumbURL = createDefaultVBox();
        vBoxThumbURL.getChildren().addAll(text, textFieldThumbURL);
        vBoxLeftSide.getChildren().add(vBoxThumbURL);
    }

    public void configureDropDown() throws Exception {
        Text text = new Text("Board (*)");

        boardsDropDown = new ComboBox<>();
        String[] boards = receiveBoards();
        if(boards!=null) {
            for(String board: boards) {
                boardsDropDown.getItems().add(board);
            }
        }
        else {
            selectBoardOrFail = "Connection issues!";
        }
        boardsDropDown.setValue(selectBoardOrFail);

        HBox hBox = new HBox();
        hBox.setSpacing(3);

        Button addBoard = new Button("+");

        textFieldDropDown = createLeftSideTextField("Fitting board doesn't exist? Create it!");
        textFieldDropDown.setOnKeyTyped(e -> checkIfComboBoxHasEntry(textFieldDropDown.getText())); //Will automatically select the correct board if typed correctly.
        textFieldDropDown.prefWidthProperty().bind(hBox.widthProperty());
        textFieldDropDown.setTextFormatter(formatBoard());

        addBoard.setOnAction(event -> {
            if(textFieldDropDown.getText().length()>0 && !checkIfComboBoxHasEntry(textFieldDropDown.getText())) {
                textFieldDropDown.setText(textFieldDropDown.getText().trim());
                boardsDropDown.getItems().add(textFieldDropDown.getText());
                boardsDropDown.getSelectionModel().select(textFieldDropDown.getText());
                textFieldDropDown.clear();
            }
        });

        hBox.getChildren().addAll(textFieldDropDown, addBoard);

        vBoxDropDown = createDefaultVBox();
        vBoxDropDown.getChildren().addAll(text, boardsDropDown, hBox);
        vBoxLeftSide.getChildren().add(vBoxDropDown);

        boardsDropDown.prefWidthProperty().bind(vBoxDropDown.widthProperty()); //Make the dropdown as big as the combo-box
        hBox.prefWidthProperty().bind(vBoxDropDown.widthProperty());
    }

    public void configureRefID() {
        Text text = new Text("Reference ID");

        textFieldRefID = createLeftSideTextField("Want to reply to someone/something?");
        textFieldRefID.setTextFormatter(formatRefID());

        vBoxRefID = createDefaultVBox();
        vBoxRefID.getChildren().addAll(text, textFieldRefID);
        vBoxLeftSide.getChildren().add(vBoxRefID);
    }



    public void configureRightSide() {
        //vBoxRightSide.setStyle("-fx-border-color: green");
        vBoxRightSide.setAlignment(Pos.CENTER);
        vBoxRightSide.setSpacing(30);
        HBox.setHgrow(vBoxRightSide, Priority.ALWAYS);
        vBoxRightSide.setPadding(new Insets(0,50,0,0));

        configureContent();
        configurePostButton();
    }

    public void configureContent() {
        Text text = new Text("Content (*)");

        textAreaContent = new TextArea();
        textAreaContent.setPromptText("Share your message with the world!");
        textAreaContent.setWrapText(true);

        vBoxContent = createDefaultVBox();
        vBoxContent.getChildren().addAll(text, textAreaContent);
        Platform.runLater(() -> textAreaContent.requestFocus());
        vBoxRightSide.getChildren().add(vBoxContent);
    }

    public void configurePostButton() {
        Button button = new Button("Post");
        button.prefWidthProperty().bind(vBoxContent.widthProperty());
        button.setOnAction(event -> {
            Alert popup = null;
            try {
                popup = checkForMistakesPrePosting();
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert popup != null;

            if(popup.getAlertType().equals(Alert.AlertType.INFORMATION)) {
                try {
                    sendPost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            popup.showAndWait();
        });

        vBoxRightSide.getChildren().add(button);
    }



    public Transcript createTranscript() {
        Transcript transcript = new Transcript();

        if(!textFieldName.getText().isEmpty()) transcript.setUsername(textFieldName.getText());
        if(!textFieldThumbURL.getText().isEmpty()) transcript.setThumb_url(textFieldThumbURL.getText());
        String refIDText = textFieldRefID.getText();
        if (!refIDText.isEmpty() && !refIDText.equals("0")) transcript.setRef_ID(refIDText);
        transcript.setContent(textAreaContent.getText());

        return transcript;
    }

    public void sendPost() throws Exception {
        Transcript transcript = createTranscript();

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);

        System.out.println(jsonRequest);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://ping.qwq.sh/posts/" + boardsDropDown.getSelectionModel().getSelectedItem()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println(postResponse.body());
    }

    public String[] receiveBoards() throws Exception {
        HttpRequest getBoardRequest = HttpRequest.newBuilder()
                .uri(new URI("https://ping.qwq.sh/boards"))
                .header("Content-Type", "application/json")
                .GET().build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> getResponse;

        try {
            getResponse = httpClient.send(getBoardRequest, HttpResponse.BodyHandlers.ofString());
        }
        catch (ConnectException e) {
            return null;
        }

        Gson gson = new Gson();

        if((gson.fromJson(getResponse.body(), String[].class)==null)) return null;

        return gson.fromJson(getResponse.body(), String[].class);
    }



    private VBox createDefaultVBox() {
        VBox vBox = new VBox();
        vBox.setSpacing(7);
        //vBox.setStyle("-fx-border-color: black");
        vBox.setMaxWidth(vBox.getMaxWidth());
        return vBox;
    }

    private TextField createLeftSideTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        return textField;
    }

    private TextFormatter<?> formatBoard() {
        return new TextFormatter<>((TextFormatter.Change change) -> {
            String text = change.getText();

            // if text was added, fix the text to fit the requirements
            if (!text.isEmpty()) {
                String newText = text.replace(" ", "").toLowerCase();

                int caretPos = change.getCaretPosition() - text.length() + newText.length();
                change.setText(newText);

                // fix caret position based on difference in originally added text and fixed text
                change.selectRange(caretPos, caretPos);
            }
            return change;
        });
    }

    public TextFormatter<?> formatRefID() {
        Pattern pattern = Pattern.compile(".{0,19}");
        return new TextFormatter<>((TextFormatter.Change change) -> {
            String text = change.getText();

            // if text was added, fix the text to fit the requirements
            if (!text.isEmpty()) {
                String newText = text.replaceAll("[^0-9]", "");

                int caretPos = change.getCaretPosition() - text.length() + newText.length();
                change.setText(newText);

                // fix caret position based on difference in originally added text and fixed text
                change.selectRange(caretPos, caretPos);
            }
            return pattern.matcher(change.getControlNewText()).matches() ? change : null;
        });
    }



    private boolean stringOnlyHasSpaces(String text) {
        String trimmedText = text.trim();
        return trimmedText.isEmpty();
    }

    private boolean checkIfComboBoxHasEntry(String stringToCheck) {
        for (String item : boardsDropDown.getItems()) {
            if (item.equalsIgnoreCase(stringToCheck)) {
                boardsDropDown.getSelectionModel().select(stringToCheck);
                return true;
            }
        }
        return false;
    }

    private String checkIfRefIDExists(String refID) throws Exception {
        HttpRequest getBoardRequest = HttpRequest.newBuilder()
                .uri(new URI("https://ping.qwq.sh/post/" + refID))
                .header("Content-Type", "application/json")
                .GET().build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> getResponse;

        try {
            getResponse = httpClient.send(getBoardRequest, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e) {
            return "0";
        }

        Gson gson = new Gson();
        if((gson.fromJson(getResponse.body(), Message.class)==null)) return "0";

        Message message = gson.fromJson(getResponse.body(), Message.class);

        return message.getId();
    }

    private Alert checkForMistakesPrePosting() throws Exception {

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Success!");
        success.setHeaderText("Post published!");
        success.setContentText("Now, we wait for responses! :)");
        success.setResizable(false);

        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Error!");
        error.setHeaderText("CHANGE THIS DEPENDING ON THE ERROR");
        error.setResizable(false);

        if(textFieldName.getText().length()>0 && stringOnlyHasSpaces(textFieldName.getText())) {
            error.setHeaderText("Incorrect name input");
            error.setContentText("A name cannot consist only of spaces, come on! \nWho would want that as a name anyway?");
            return error;
        }

        if(boardsDropDown.getSelectionModel().getSelectedIndex()==-1) {
            error.setHeaderText("Missing board");
            error.setContentText("Where is this supposed to get posted?!");
            return error;
        }

        if(textAreaContent.getText().isEmpty()) {
            error.setHeaderText("Missing input");
            error.setContentText("Do you...not want to share anything with the world?");
            return error;
        }
        else if(stringOnlyHasSpaces(textAreaContent.getText())) {
            error.setHeaderText("Incorrect content input");
            error.setContentText("No, your message to the world cannot just consist only out of spaces. Quite literally 1984.");
            return error;
        }

        if((!textFieldRefID.getText().isEmpty()) && checkIfRefIDExists( textFieldRefID.getText() ).equals("0") ) {
            error.setHeaderText("Incorrect reference ID");
            error.setContentText("Well, looks like you can't reply to it. Sad!");
            return error;
        }

        return success;
    }
}