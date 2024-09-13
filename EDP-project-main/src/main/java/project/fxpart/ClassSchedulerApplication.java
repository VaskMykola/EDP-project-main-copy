package project.fxpart;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClassSchedulerApplication extends Application {

    private static String HOST_IP = "localhost";
    private final static int PORT = 1234;

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public ClassSchedulerApplication() {

        try {
            socket = new Socket(HOST_IP, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void start(Stage primaryStage) {

        // when client closes the application window, disconnect from the server
        primaryStage.setOnCloseRequest(event -> {
            out.println("DISCONNECT");
            closeResources();
        });

        primaryStage.setTitle("Class Scheduler");

        ChoiceBox<String> actionMenuChoiceBox = new ChoiceBox<>();
        actionMenuChoiceBox.getItems().addAll("Add New Class", "Remove Class", "Display Schedule");

        ChoiceBox<String> dayOfWeekChoiceBox = new ChoiceBox<>();
        dayOfWeekChoiceBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        dayOfWeekChoiceBox.setValue("Monday");


        Button performActionButton = new Button("Perform Action");

        Button requestEarlyClassesButton = createRequestEarlyClassesButton();

        TextField classNameTextField = new TextField();
        classNameTextField.setMaxSize(210, 30);

        TextField roomNumberTextField = new TextField();
        roomNumberTextField.setMaxSize(210, 30);


        Label startsAtLabel = new Label("Starts at");
        startsAtLabel.setMinWidth(60);

        Label finishesAtLabel = new Label("Finishes at");
        finishesAtLabel.setMinWidth(60);


        ComboBox<Integer> startHoursDropdown = new ComboBox<>();
        ComboBox<Integer> startMinutesDropdown = new ComboBox<>();

        ComboBox<Integer> finishHoursDropdown = new ComboBox<>();
        ComboBox<Integer> finishMinutesDropdown = new ComboBox<>();


        for (int i = 9; i < 19; i++) {
            startHoursDropdown.getItems().add(i);
            finishHoursDropdown.getItems().add(i);
        }
        for (int i = 0; i < 60; i += 5) {
            startMinutesDropdown.getItems().add(i);
            finishMinutesDropdown.getItems().add(i);
        }

        HBox hBoxForPickingStartTime = new HBox(10);
        hBoxForPickingStartTime.setAlignment(Pos.CENTER);
        hBoxForPickingStartTime.getChildren().addAll(
                startsAtLabel,
                startHoursDropdown,
                new Label("hours"),
                startMinutesDropdown,
                new Label("minutes"));


        HBox hBoxForPickingFinishTime = new HBox(10);
        hBoxForPickingFinishTime.setAlignment(Pos.CENTER);
        hBoxForPickingFinishTime.getChildren().addAll(
                finishesAtLabel,
                finishHoursDropdown,
                new Label("hours"),
                finishMinutesDropdown,
                new Label("minutes"));

        actionMenuChoiceBox.setOnAction(event -> {
            String selectedAction = actionMenuChoiceBox.getValue();
            if (selectedAction.equals("Display Schedule")) {
                classNameTextField.setPromptText("");
                roomNumberTextField.setPromptText("");
                dayOfWeekChoiceBox.setDisable(true);
                hBoxForPickingStartTime.setDisable(true);
                hBoxForPickingFinishTime.setDisable(true);
                classNameTextField.setDisable(true);
                roomNumberTextField.setDisable(true);
            } else if (selectedAction.equals("Add New Class")) {
                classNameTextField.setPromptText("Enter the class name.");
                roomNumberTextField.setPromptText("Enter the room number.");
                dayOfWeekChoiceBox.setDisable(false);
                hBoxForPickingStartTime.setDisable(false);
                hBoxForPickingFinishTime.setDisable(false);
                classNameTextField.setDisable(false);
                roomNumberTextField.setDisable(false);
            } else {
                classNameTextField.setPromptText("Enter the name of class to remove.");
                roomNumberTextField.setPromptText("");
                dayOfWeekChoiceBox.setDisable(false);
                hBoxForPickingStartTime.setDisable(false);
                hBoxForPickingFinishTime.setDisable(true);
                classNameTextField.setDisable(false);
                roomNumberTextField.setDisable(true);
            }
        });
        actionMenuChoiceBox.setValue("Add New Class"); // sets the default value for the choice box

        performActionButton.setOnAction(event -> {
            // IMPLEMENTATION OF THE JAVAFX.CONCURRENT
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    switch (actionMenuChoiceBox.getValue()) {
                        case "Display Schedule" -> out.println("Display Schedule");
                        case "Add New Class" -> out.println(String.format("Add New Class,%s,%d,%d,%d,%d,%s,%s",
                                dayOfWeekChoiceBox.getValue(),
                                startHoursDropdown.getValue(), startMinutesDropdown.getValue(),
                                finishHoursDropdown.getValue(), finishMinutesDropdown.getValue(),
                                classNameTextField.getText(), roomNumberTextField.getText()));
                        case "Remove Class" -> out.println(String.format("Remove Class,%s,%d,%d,%s",
                                dayOfWeekChoiceBox.getValue(),
                                startHoursDropdown.getValue(), startMinutesDropdown.getValue(),
                                classNameTextField.getText()));
                    }
                    return null;
                }
            };
            // everything below will be executed on the GUI thread, so there is no need to use Platform.runLater() for updating GUI components
            task.setOnSucceeded(e -> {
                String messageFromServer = getMessageFromServer();
                Alert alert;
                if (messageFromServer.startsWith("ERROR MESSAGE")) {
                    alert = createAlert(Alert.AlertType.ERROR, "Error Occurred",
                            messageFromServer.substring(messageFromServer.indexOf(":") + 1));
                } else {
                    alert = createAlert(Alert.AlertType.INFORMATION, "Operation Successful", messageFromServer);
                }
                System.out.println(messageFromServer);
                alert.show();
            });

            new Thread(task).start();
        });

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(actionMenuChoiceBox);
        root.getChildren().add(dayOfWeekChoiceBox);

        root.getChildren().add(hBoxForPickingStartTime);
        root.getChildren().add(hBoxForPickingFinishTime);

        root.getChildren().add(classNameTextField);
        root.getChildren().add(roomNumberTextField);
        root.getChildren().add(performActionButton);
        root.getChildren().add(requestEarlyClassesButton);

        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();

    }

    private Button createRequestEarlyClassesButton() {
        Button requestEarlyClassesButton = new Button("Request Early Classes");

        requestEarlyClassesButton.setOnAction(event -> {
            Alert confirmTheActionAlert = createAlert(Alert.AlertType.CONFIRMATION, "Confirm the Action", "Do you want to shift all your classes to the morning time?");
            confirmTheActionAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    System.out.println("They chose OK.");
                    // IMPLEMENTATION OF THE JAVAFX.CONCURRENT
                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() {
                            out.println("Early Mornings");
                            return null;
                        }

                    };
                    task.setOnSucceeded(e -> {
                        String messageFromServer = getMessageFromServer();
                        System.out.println(messageFromServer);
                        Alert alert = createAlert(Alert.AlertType.INFORMATION, "Operation Successful", messageFromServer);
                        alert.show();
                    });
                    new Thread(task).start();

                } else {
                    System.out.println("Canceled");
                }
            });
        });

        return requestEarlyClassesButton;
    }

    private Alert createAlert(Alert.AlertType alertType, String alertTitle, String message) {

        Alert alert = new Alert(alertType);
        alert.setTitle(alertTitle);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert;
    }

    private String getMessageFromServer() {

        StringBuilder fullMessageFromServer = new StringBuilder();

        try {
            String m = in.readLine();
            System.out.println(m);
            int messageLength = Integer.parseInt(m);

            for (int i = 0; i < messageLength; i++) {
                fullMessageFromServer.append((char) in.read());
            }
            in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fullMessageFromServer.toString();
    }

    public void closeResources() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setHostIp() { // Change it to be a gui thing or something
        System.out.print("Enter the IP address of the server [press 'Enter' for default localhost]:\n>>> ");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String input = bufferedReader.readLine();
            if (!input.isBlank()) {
                HOST_IP = input;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        setHostIp();
        System.out.println(HOST_IP);
        launch(args);
    }
}