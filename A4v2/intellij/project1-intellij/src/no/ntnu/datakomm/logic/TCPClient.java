package no.ntnu.datakomm.logic;

import no.ntnu.datakomm.facade.ChatListener;
import no.ntnu.datakomm.facade.ChatClientFacade;
import no.ntnu.datakomm.data.TextMessage;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents (business) logic of the chat client.
 * Part of it is implemented. Part of the code is missing. See assignment
 * description. Hints are shown in the comments. See for comments with TODO.
 */
public class TCPClient implements ChatClientFacade {

    private InputStream in;
    private OutputStream out;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private Socket connection;

    private String lastError = null; // Last error message will be stored here

    private final List<ChatListener> listeners = new LinkedList<>();

    /**
     * Connect to a chat server.
     *
     * @param host host name or IP address of the chat server
     * @param port TCP port of the chat server
     * @return True on success, false otherwise
     */
    @Override
    public boolean connect(String host, int port) {
        try {
            System.out.println("Connecting to " + host + ", port " + port);
            // Open TCP connection to the server
            connection = new Socket(host, port);
            // Set up input and output streams
            in = connection.getInputStream();
            out = connection.getOutputStream();
            toServer = new PrintWriter(out, true);
            fromServer = new BufferedReader(new InputStreamReader(in));
            return true;
        } catch (UnknownHostException e) {
            lastError = "Unknown host";
            System.err.println(lastError);
            return false;
        } catch (ConnectException e) {
            lastError = "No chat server listening on given port";
            System.err.println(lastError);
            return false;
        } catch (IOException e) {
            lastError = "I/O error for the socket";
            System.err.println(lastError);
            return false;
        }
    }

    /**
     * Close the socket. This method must be synchronized, because several
     * threads may try to call it. For example: When "Disconnect" button is
     * pressed in the GUI thread, the connection will get closed. Meanwhile, the
     * background thread trying to read server's response will get error in the
     * input stream and may try to call this method when the socket is already
     * in the process of being closed. with "synchronized" keyword we make sure
     * that no two threads call this method in parallel.
     */
    @Override
    public synchronized void disconnect() {
        if (connection != null) {
            System.out.println("Disconnecting...");
            try {
                // Close the socket and streams
                toServer.close();
                fromServer.close();
                connection.close();
            } catch (IOException e) {
                System.out.println("Error while closing connection: "
                        + e.getMessage());
                lastError = e.getMessage();
                connection = null;
            }
        } else {
            System.out.println("No connection to close");
        }
        System.out.println("Disconnected");
        connection = null;
    }

    /**
     * Return true if the connection is active (opened), false if not.
     *
     * @return
     */
    @Override
    public boolean isConnectionActive() {
        return connection != null;
    }

    /**
     * Send a command to server - one line of text.
     *
     * @param cmd
     * @return true on success, false otherwise
     */
    private boolean sendCommand(String cmd) {
        // TODO - Step 2 - implement this method. 
        // Hint: use variable toServer which is a writer for the output stream
        // Hint: you should check if the connection is active
        return false;
    }

    /**
     * Try to log in the chat server, authenticate with a specific username.
     *
     * @param username
     */
    @Override
    public void tryLogin(String username) {
        // TODO - Step 3 - implement it. Reuse sendCommand() method.
    }

    /**
     * Send a request for latest user list to the server. To get the new users,
     * clear your current user list and use events in the listener.
     */
    @Override
    public void refreshUserList() {
        // TODO - Step 5 - implement it. Reuse sendCommand() method.
    }

    /**
     * Send a request for the list of commands that server supports.
     */
    @Override
    public void askSupportedCommands() {
        // TODO - Step 8 - implement it. Reuse sendCommand() method.
    }

    /**
     * Check if a given string can be sent as a valid chat message
     *
     * @param message
     * @return True when it is a valid message, false otherwise
     */
    private boolean isValidMessage(String message) {
        if (message.indexOf('\n') >= 0) {
            lastError = "Message contains newline, ignored";
            System.out.println(lastError);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Send a public or private message
     *
     * @param cmd The command to be used for the message: msg or privmsg
     * @param message
     * @return
     */
    private boolean sendTextMessage(String cmd, String recipient, String message) {
        if (!isValidMessage(message)) {
            return false;
        }
        String cmdToSend = cmd + " ";
        if (recipient != null && cmdToSend.length() > 0) {
            cmdToSend += recipient + " ";
        }
        cmdToSend += message;
        sendCommand(cmdToSend);
        return true;
    }

    /**
     * Send a public message to all the recipients
     *
     * @param message
     * @return true if message sent, false on error (for example, message
     * contained illegal characters). use getLastError() to find out
     * the reason for the error.
     */
    @Override
    public boolean sendPublicMessage(String message) {
        // TODO - Step 2 - implement it. Reuse sendTextMessage() method.
        return false;
    }

    /**
     * Send a private message to a single recipient.
     *
     * @param recipient username of the chat user who should receive the message
     * @param message
     * @return true if message sent, false on error (for example, message
     * contained illegal characters). use getLastError() to find out
     * the reason for the error.
     */
    @Override
    public boolean sendPrivateMessage(String recipient, String message) {
        // TODO - Step 6 - implement it. Reuse sendTextMessage() method.
        return false;
    }

    /**
     * Wait for one server's response: one line of text.
     * Close connection if an error occurred.
     *
     * @return the received command, null on error (socket closed)
     */
    private String getOneResponseCommand() {
        String output = null;
        try {
            output = fromServer.readLine();
            if (output != null) {
                System.out.println("<<< " + output);
            } else {
                // This happens when the connection is closed
                // Clean the connection on our end
                disconnect();
            }
        } catch (IOException ex) {
            System.out.println(
                    "Err while reading server response, socket seems to be closed");
            lastError = "Server closed socket";
            disconnect();
            // Notify the listeners that connection is interrupted
            onDisconnect();
        }
        return output;
    }

    /**
     * Get the last error message that the server returned.
     *
     * @return
     */
    @Override
    public String getLastError() {
        if (lastError != null) {
            return lastError;
        } else {
            return "";
        }
    }

    /**
     * Start listening for incoming commands from the server on a new background
     * thread.
     */
    @Override
    public void startListenThread() {
        Thread t = new Thread(() -> {
            parseIncomingCommands();
        });
        t.start();
    }

    /**
     * Register a new listener for events (login result, incoming message, etc)
     *
     * @param listener
     */
    @Override
    public void addListener(ChatListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregister an event listener.
     *
     * @param listener
     */
    @Override
    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
    }

    /**
     * Read incoming messages one by one, generate events for the listeners.
     * This method should be called in a background thread, not in the main
     * user-interface thread
     */
    private void parseIncomingCommands() {
        while (isConnectionActive()) {
            // Wait for the next response from the server. 
            // The next line will block until a response is received. 
            String line = getOneResponseCommand();

            if (line != null && line.length() > 0) {
                // Split the line in command and parameters
                int spacePos = line.indexOf(' ');
                String cmd;
                String params;
                if (spacePos >= 0) {
                    cmd = line.substring(0, spacePos);
                    params = line.substring(spacePos + 1);
                } else {
                    cmd = line;
                    params = "";
                }

                // TODO - Step 3 and onwards - Handle the different commands
                // TODO - notify the listeners with necessary events. Reuse the on...() methods below
                // For example, if server sends a response which means "login successful", 
                // you should call onLoginResult()
            }
        }
    }

    /**
     * Notify listeners that login operation is complete (either with success or
     * failure)
     *
     * @param success When true, login successful. When false, it failed
     * @param errMsg Error message if any
     */
    private void onLoginResult(boolean success, String errMsg) {
        for (ChatListener l : listeners) {
            l.onLoginResult(success, errMsg);
        }
    }

    /**
     * Notify listeners that a message is received from the server
     *
     * @param priv When true, this is a private message
     * @param sender Username of the sender
     * @param text Message text
     */
    private void onMsgReceived(boolean priv, String sender, String text) {
        TextMessage msg = new TextMessage(sender, priv, text);
        for (ChatListener l : listeners) {
            l.onMessageReceived(msg);
        }
    }

    /**
     * Notify listeners that our message was not delivered
     *
     * @param errMsg Error description returned by the server
     */
    private void onMsgError(String errMsg) {
        for (ChatListener l : listeners) {
            l.onMessageError(errMsg);
        }
    }

    /**
     * Notify listeners that command was not understood by the server.
     *
     * @param errMsg
     */
    private void onCmdError(String errMsg) {
        for (ChatListener l : listeners) {
            l.onCommandError(errMsg);
        }
    }

    /**
     * Notify listeners that server sent us a list of currently connected users
     *
     * @param users List with usernames
     */
    private void onUsersList(String[] users) {
        for (ChatListener l : listeners) {
            l.onUserList(users);
        }
    }

    /**
     * Notify listeners that a help response (supported commands) was received
     * from the server
     *
     * @param commands Commands supported by the server
     */
    private void onHelp(String[] commands) {
        for (ChatListener l : listeners) {
            l.onSupportedCommands(commands);
        }
    }

    /**
     * Notify listeners that socket was closed by the remote end (server or
     * Internet error)
     */
    private void onDisconnect() {
        for (ChatListener l : listeners) {
            l.onDisconnect();
        }
    }
}
