package todolist.database;

import todolist.collaboration.Collaboration;
import todolist.exceptions.CollaborationNotCreatorException;
import todolist.exceptions.CollaborationNotExistException;
import todolist.exceptions.UserAlreadyExistsException;
import todolist.exceptions.UserAlreadyLoggedException;
import todolist.exceptions.UserDoesNotExistException;
import todolist.user.User;

import java.nio.channels.SocketChannel;
import java.util.List;

public interface Storage {

    List<User> getUsers();
    void identifyChannel(SocketChannel clientChannel);

    boolean isItAccessible(SocketChannel clientSocket);

    boolean register(User user) throws UserAlreadyExistsException;

    User logIn(User user , SocketChannel clientSocket)
            throws UserDoesNotExistException, UserAlreadyLoggedException;

    boolean logout(SocketChannel clientSocket, User user);

    void updateUsersDatabase(User updatedUser);

    void updateCollaborationsDatabase(Collaboration updatedCollaboration);

    boolean addCollaboration(Collaboration collaboration);

    String listCollaboration(User user);

    boolean deleteCollaboration(String collaborationName, User currentUser)
            throws CollaborationNotExistException, CollaborationNotCreatorException;

    Collaboration getCollaboration(String name);

    String listTaskCollaboration(String collaborationName) throws CollaborationNotExistException;

    String listUsersCollaboration(String collaborationName) throws CollaborationNotExistException;

    User getCurrentUser(String username);


}
