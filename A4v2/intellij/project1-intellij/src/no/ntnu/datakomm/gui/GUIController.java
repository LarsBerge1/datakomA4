package no.ntnu.datakomm.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static java.lang.Thread.sleep;
import javafx.collections.ObservableList;
import no.ntnu.datakomm.data.TextMessage;
import no.ntnu.datakomm.facade.ChatListener;
import no.ntnu.datakomm.logic.TCPClient;

/**
 * The main GUI class controlling the wiring between logic and user interface.
 * Part of it is implemented. Part of the code is missing. See assignment
 * description. Hints are shown in the comments. See for comments with TODO.
 */
public class GUIController implements ChatListener {

    @FXML
    private Button submitBtn;

    @FXML
    private Button connectBtn;

    @FXML
    private Button loginBtn;

    @FXML
    private Button helpBtn;

    @FXML
    private VBox userList;

    @FXML
    private VBox textOutput;

    @FXML
    private TextArea textInput;

    @FXML
    private TextField hostInput;

    @FXML
    private TextField portInput;

    @FXML
    private TextField loginInput;

    @FXML
    private TitledPane serverStatus;

    @FXML
    private ScrollPane outputScroll;

    // Interface to the logic 
    private TCPClient tcpClient;

    // One background thread will be used to poll for user list every 10 seconds
    private Thread userPollThread;

    /**
     * Called by the FXML loader after the labels declared above are injected.
     */
    public void initialize() {
        tcpClient = new TCPClient();
        // Set default values
        hostInput.setText("jonoie.com");
        portInput.setText("1300");
        textOutput.heightProperty().addListener((observable, oldValue, newValue)
                -> outputScroll.setVvalue(1.0));
        setKeyAndClickListeners();
    }

    /**
     * Set up keyboard and mouse event handlers.
     */
    private void setKeyAndClickListeners() {
        textInput.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER) && event.isShiftDown()) {
                // Shift+Enter pressed
                textInput.setText(textInput.getText() + "\n");
                textInput.requestFocus();
                textInput.end();
            } else if (event.getCode().equals(KeyCode.ENTER)) {
                // Enter pressed, send the message
                submitMessage();
                event.consume();
            }
        });
        submitBtn.setOnMouseClicked(event -> {
            // "Submit" button clicked, send the message
            submitMessage();
            textInput.requestFocus();
        });
        connectBtn.setOnMouseClicked(event -> {
            // "Connect" button clicked
            if (!tcpClient.isConnectionActive()) {
                // If the connection is not active, connect to the server
                // TODO - Step 1: get the host and port values from UI elements
                setupConnection("constant.invalid.host", 1234);
            } else {
                // If the connection is already active, this button actually means "Disconnect". 
                // TODO - Step 4: Call facade methods for disconnect
                // TODO - Step 4: update button enabled/disabled states
            }

        });
        loginBtn.setOnMouseClicked(event -> {
            // "Authorize" button clicked
            // TODO - Step 3: call facade method(s) for login
        });
        helpBtn.setOnMouseClicked(event -> {
            // "Help" button clicked
            // TODO - Step 8: call facade methods for supported command list
        });
    }

    /**
     * Take the message from the text input box, send it to the server.
     */
    private void submitMessage() {
        // TODO - step 2: Implement public message submission
        // TODO - step 6: Implement private message submission
        // Hint: take value of textInput field, check if it starts with /privmsg 
        // then form a TextMessage object and call facade methods for the tcpClient
        // remember to add the sent message to the chat window
        // P.S. When you are done with your implementations it is best to remove 
        // the hints and TODO comments ;)
    }

    /**
     * Add a message to the GUI chat window.
     *
     * @param local When true, this message was sent by us. When false -
     * received from another user
     * @param msg The message to be displayed
     * @param warning When true, this is not a chat message sent by a user.
     * Rather, it is some kind of warning/error message sent by the server. If
     * this is set to true and sender is "err", this is treated as an error
     * message.
     */
    private void addMsgToGui(boolean local, TextMessage msg, boolean warning) {
        // Create GUI elements, set their text and style according to what 
        // type of message this is

        HBox message = new HBox();
        VBox messageContent = new VBox();
        String senderText;
        Label messageSender = new Label();
        messageSender.getStyleClass().add("sender");
        if (msg.isPrivate()) {
            senderText = "Private from " + msg.getSender() + ":";
            messageSender.getStyleClass().add("private");
        } else {
            senderText = msg.getSender() + ":";
        }
        messageSender.setText(senderText);
        Label messageText = new Label(msg.getText());
        ObservableList<String> textStyle = messageText.getStyleClass();
        textStyle.add("message");
        messageText.setWrapText(true);
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10, 1);
        if (warning) {
            messageContent.getChildren().addAll(messageText);
            message.getChildren().addAll(messageContent);
            if (msg.getSender().equals("err")) {
                textStyle.add("warning");
            } else {
                textStyle.add("info");
            }
        } else {
            if (local) {
                if (tcpClient.isConnectionActive()) {
                    textStyle.add("sentMessage");
                } else {
                    serverStatus.setText(
                            "Please login to send messages to server");
                    textStyle.add("failedMessage");
                }
                messageContent.getChildren().addAll(messageText);
                // Add empty space first (left), then the message (right)
                message.getChildren().addAll(spacer, messageContent);
            } else {
                textStyle.add("otherMessage");
                messageContent.getChildren().addAll(messageSender, messageText);
                // Add message first (left), then empty space (right)
                message.getChildren().addAll(messageContent, spacer);
            }
        }
        textOutput.getChildren().add(message);
    }

    /**
     * Start a connection to the server: try to connect Socket, log in and start
     * listening for incoming messages
     *
     * @param host Address of Chat server. Can be IP address, can be hostname
     * @param port TCP port for the chat server
     */
    private void setupConnection(String host, int port) {
        // TODO - Step 4: update the status text and buttons

        // Run the connection in a new background thread to avoid GUI freeze
        Thread connThread = new Thread(() -> {
            boolean connected = tcpClient.connect(host, port);
            if (connected) {
                // Connection established, start listening processes
                tcpClient.addListener(this);
                tcpClient.startListenThread();
                startUserPolling();
            }
            // TODO - Step 4: update update button state according to connection status
        });
        connThread.start();
    }

    /**
     * Update texts and enabled/disabled state of GUI buttons according to
     * connection success.
     *
     * @param connected When true, client has successfully connected to the
     * server. When false, connection failed
     */
    private void updateButtons(boolean connected) {
        String status;
        String connBtnText;
        if (connected) {
            status = "Connected to the server";
            connBtnText = "Disconnect";
        } else {
            status = "Not connected: " + tcpClient.getLastError();
            connBtnText = "Connect";
        }
        // Make sure this will be executed on GUI thread
        Platform.runLater(() -> {
            // Update button texts
            serverStatus.setText(status);
            connectBtn.setText(connBtnText);
            // Connection button was disabled while connection was in progress, 
            // now we enable it
            connectBtn.setDisable(false);

            // Enable/disable buttons: Login, help, submit
            loginBtn.setDisable(!connected);
            submitBtn.setDisable(!connected);
            helpBtn.setDisable(!connected);
        });

    }

    ///////////////////////////////////////////////////////////////////////
    // The methods below are called by the associated TcpClient (facade 
    // object) in another background thread when messages are received
    // from the server.
    ///////////////////////////////////////////////////////////////////////
    /**
     * Start a new thread that will poll the server for currently active users
     */
    private void startUserPolling() {
        // Make sure we have just one polling thread, not duplicates
        if (userPollThread == null) {

            userPollThread = new Thread(() -> {
                ////////////////////////////////////////////////////////////////
                // This block of code will run in the polling thread
                ////////////////////////////////////////////////////////////////
                long threadId = Thread.currentThread().getId();
                System.out.println("Started user polling in Thread "
                        + threadId);
                while (tcpClient.isConnectionActive()) {
                    // TODO - Step 5: ask the server for current user list
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("User polling thread " + threadId
                        + " exiting...");
                // Make sure we start the thread again next time
                userPollThread = null;
                ////////////////////////////////////////////////////////////////
                // EOF polling thread code
                ////////////////////////////////////////////////////////////////
            });

            userPollThread.start();
        }
    }

    /**
     * This method is called when a login procedure is done: either it succeeded
     * or failed.
     *
     * @param success when true, the client has logged in, when false, login
     * failed
     * @param errMsg Error message in case of failure, or null on successful
     * login
     */
    @Override
    public void onLoginResult(boolean success, String errMsg) {
        // This method is called in a background thread. 
        // To update GUI elements, we must use Platform.runLater()
        Platform.runLater(() -> {
            // TODO - Step 3: update status text according to login result: success or failure
            // TODO - Step 3: add message to chat window, if necessary
        });
    }

    /**
     * This method is called when an incoming text message is received
     *
     * @param message
     */
    @Override
    public void onMessageReceived(TextMessage message) {
        Platform.runLater(() -> {
            // TODO - Step 7: show the incoming message in GUI
        });
    }

    /**
     * This method is called when an error happened when we tried to send
     * message to the server (the message was not sent to necessary recipients)
     *
     * @param errMsg Error message
     */
    @Override
    public void onMessageError(String errMsg) {
        Platform.runLater(() -> {
            // TODO - Step 7: show the error message in GUI
        });
    }

    /**
     * This method is called when a list of currently connected users is
     * received
     *
     * @param usernames
     */
    @Override
    public void onUserList(String[] usernames) {
        // Update user list in GUI thread
        Platform.runLater(() -> {
            // Clear the user list, add items with onClick listener on each item
            userList.getChildren().clear();
            for (String user : usernames) {
                Label text = new Label(user);
                text.getStyleClass().add("user");
                text.setOnMouseClicked(event -> {
                    textInput.setText("/privmsg " + user + " ");
                    textInput.requestFocus();
                    textInput.end();
                });
                userList.getChildren().add(text);
            }
        });
    }

    /**
     * This method is called when a list of currently supported commands is
     * received.
     *
     * @param commands
     */
    @Override
    public void onSupportedCommands(String[] commands) {
        // Show the supported commands in the GUI as "warning/info" messages
        Platform.runLater(() -> {
            String cmds = "Commands available: ";
            for (String c : commands) {
                cmds += c + " ";
            }
            addMsgToGui(true, new TextMessage("info", false, cmds), true);
        });
    }

    /**
     * This method is called when the server did not understand the last command
     *
     * @param errMsg Error message
     */
    @Override
    public void onCommandError(String errMsg) {
        // Show the error in the GUI as a "warning" message
        Platform.runLater(() -> {
            TextMessage msg = new TextMessage("err", false, "Error: " + errMsg);
            addMsgToGui(true, msg, true);
        });
    }

    /**
     * This method is called when connection (socket) is closed by the remote
     * end (server).
     */
    @Override
    public void onDisconnect() {
        System.out.println("Socket closed by the remote end");
        updateButtons(false);
    }
}
