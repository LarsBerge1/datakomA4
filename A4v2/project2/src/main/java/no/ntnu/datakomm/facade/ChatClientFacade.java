package no.ntnu.datakomm.facade;

/**
 * An interface that hides complexities of the ChatClient business logic and
 * collects all the interface methods in a single place. External components
 * (GUI, etc) should access only classes implementing this interface, not
 * business logic classes directly.
 * 
 * For the assignment you probably don't need to modify anything in this file.
 */
public interface ChatClientFacade {

    /**
     * Connect to a chat server.
     *
     * @param host host name or IP address of the chat server
     * @param port TCP port of the chat server
     * @return True on success, false otherwise
     */
    public boolean connect(String host, int port);

    /**
     * Disconnect from the chat server (close the socket)
     */
    public void disconnect();

    /**
     * Start listening for incoming commands from the server. The implementing
     * class should start a new thread for it.
     */
    public void startListenThread();

    /**
     * Register a new listener for events (login result, incoming message, etc)
     *
     * @param listener
     */
    public void addListener(ChatListener listener);

    /**
     * Unregister an event listener.
     *
     * @param listener
     */
    public void removeListener(ChatListener listener);

    /**
     * Return true if the connection is active (opened), false if not.
     *
     * @return
     */
    public boolean isConnectionActive();

    /**
     * Try to log in the chat server, authenticate with a specific username.
     *
     * @param username
     */
    public void tryLogin(String username);

    /**
     * Send a public message to all the recipients
     *
     * @param message
     * @return true if message sent, false on error (for example, message
     * contained illegal characters). use getLastError() to find out
     * the reason for the error.
     */
    public boolean sendPublicMessage(String message);

    /**
     * Send a private message to a single recipient.
     *
     * @param recipient username of the chat user who should receive the message
     * @param message
     * @return true if message sent, false on error (for example, message
     * contained illegal characters). use getLastError() to find out
     * the reason for the error.
     */
    public boolean sendPrivateMessage(String recipient, String message);

    /**
     * Send a request for latest user list to the server. To get the new users,
     * clear your current user list and use events in the listener.
     */
    public void refreshUserList();

    /**
     * Get the last error message that the server returned.
     *
     * @return
     */
    public String getLastError();

    /**
     * Send a request for the list of commands that server supports.
     */
    public void askSupportedCommands();
}
