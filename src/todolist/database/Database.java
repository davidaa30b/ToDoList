package todolist.database;

import todolist.collaboration.Collaboration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import todolist.exceptions.CollaborationNotCreatorException;
import todolist.exceptions.CollaborationNotExistException;
import todolist.exceptions.UserAlreadyExistsException;
import todolist.exceptions.UserAlreadyLoggedException;
import todolist.exceptions.UserDoesNotExistException;
import todolist.user.User;
import todolist.validation.Validation;
import todolist.messagesstatus.StatusMessages;
import todolist.user.UserValidation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database implements Storage {

    private static final String FILE_DATABASE = "database.txt";
    private final Gson gson = new Gson();
    private File file = new File(FILE_DATABASE);
    private List<User> users;
    private List<Collaboration> collaborations;
    private Map<SocketChannel, Boolean> channelsKeysForSessions = new HashMap<>();
    private Map<User, Boolean> keysForLoggedUsers = new HashMap<>();
    // the key to use the systems features
    // is only allowed as a logged-in user

    public static Database getInstance() {
        return INSTANCE;
    }

    public static Database getInstanceWithCustomFile(File file) {
        return new Database(file);
    }



    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public void identifyChannel(SocketChannel clientChannel) {
        channelsKeysForSessions.putIfAbsent(clientChannel, false);
    }

    @Override
    public boolean isItAccessible(SocketChannel clientSocket) {
        return channelsKeysForSessions.get(clientSocket);
    }


    //users
    @Override
    public boolean register(User user) throws UserAlreadyExistsException {
        if (UserValidation.checkIfUserExistsInDatabase(this, user)) {
            throw new UserAlreadyExistsException(StatusMessages.ERROR.getMessage(String.format(
                    "User (%s) already exists or the (%s) password is not safe",
                    user.getUsername(), user.getPassword())));
        }

        users.add(user);
        keysForLoggedUsers.put(user, false);

        addUsersAndCollaborationsToFile();
        return true;
    }

    @Override
    public User logIn(User user , SocketChannel clientSocket)
            throws UserDoesNotExistException, UserAlreadyLoggedException {

        if (!Validation.checkIfUserCorrectInDatabase(this, user)) {
            throw new UserDoesNotExistException(
                    StatusMessages.ERROR.getMessage(String.format(
                            "User (%s) does not exists or the following password (%s) is incorrect",
                            user.getUsername(), user.getPassword())));

        }

        if (keysForLoggedUsers.get(getCurrentUser(user.getUsername()))) {
            throw new UserAlreadyLoggedException(
                    StatusMessages.WARNING.getMessage(String.format(
                    "User (%s) is already logged in. Wait until the user has logged off to use this account",
                    user.getUsername())));
        }

        channelsKeysForSessions.put(clientSocket, true); // logging in the system !
        keysForLoggedUsers.put( getCurrentUser(user.getUsername()), true);
        return getCurrentUser(user.getUsername());
    }

    @Override
    public boolean logout(SocketChannel clientSocket, User user) {

        channelsKeysForSessions.put(clientSocket, false);
        keysForLoggedUsers.put( user, false);
        return true;
    }

    @Override
    public void updateUsersDatabase(User updatedUser) {
        for (var user : users ) {
            if (user.equals(updatedUser)) {
                user.setTasks(updatedUser.getTasks());
                user.setInbox(updatedUser.getInbox());
                break;
            }
        }
        addUsersAndCollaborationsToFile();
    }

    @Override
    public void updateCollaborationsDatabase(Collaboration updatedCollaboration) {
        for (var collaboration : collaborations ) {
            if (collaboration.equals(updatedCollaboration)) {
                collaboration.setAssigneeTasks(updatedCollaboration.getAssigneeTasks());
                collaboration.setUsers(updatedCollaboration.getUsers());
                break;
            }
        }
        addUsersAndCollaborationsToFile();
    }


    @Override
    public boolean addCollaboration(Collaboration collaboration) {
        collaborations.add(collaboration);
        addUsersAndCollaborationsToFile();

        return true;
    }

    @Override
    public String listCollaboration(User user) {
        StringBuilder result = new StringBuilder();
        result.append(System.lineSeparator());

        int counter = 0;
        for (var collaboration : collaborations) {
            if (collaboration.hasUser(user)) {
                result.append(collaboration.getName());
                result.append(System.lineSeparator());
                counter++;
            }
        }

        if (counter != 0) {
            return result.toString();
        }
        else {
            return "You have no collaborations";
        }
    }

    @Override
    public boolean deleteCollaboration(String collaborationName, User currentUser)
            throws CollaborationNotExistException, CollaborationNotCreatorException {

        if (Validation.isObjNull(getCollaboration(collaborationName))) {
            throw new CollaborationNotExistException(StatusMessages.ERROR.getMessage(String.format(
                    "Collaboration (%s) does not exist in database", collaborationName)));
        }

        if (!getCollaboration(collaborationName).getCreator().equals(currentUser)) {
            throw new CollaborationNotCreatorException(StatusMessages.ERROR.getMessage(String.format(
                    " Only creator can delete collaboration (%s)", collaborationName)));
        }

        collaborations.remove(getCollaboration(collaborationName));
        return true;
    }

    @Override
    public Collaboration getCollaboration(String name)  {
        return collaborations.stream().filter(other -> other.getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public String listTaskCollaboration(String collaborationName) throws CollaborationNotExistException {
        Collaboration collaboration = getCollaboration(collaborationName);
        if (Validation.isObjNull(collaboration)) {
            throw new CollaborationNotExistException(StatusMessages.ERROR.getMessage(String.format(
                    "Collaboration (%s) does not exist in database", collaborationName)));
        }
        StringBuilder result = new StringBuilder();
        result.append(System.lineSeparator());
        int counter = 0 ;
        for (var assignee : collaboration.getAssigneeTasks()) {
            result.append(assignee.task());
            result.append(System.lineSeparator());
            counter++;
        }

        if (counter != 0 ) {
            return result.toString();
        }
        else {
            return "The following collaboration has no tasks";
        }
    }

    @Override
    public String listUsersCollaboration(String collaborationName) throws CollaborationNotExistException {
        Collaboration collaboration = getCollaboration(collaborationName);

        if (Validation.isObjNull(collaboration)) {
            throw new CollaborationNotExistException(StatusMessages.ERROR.getMessage(String.format(
                    "Collaboration (%s) does not exist in database", collaborationName)));
        }

        StringBuilder result = new StringBuilder();
        result.append(System.lineSeparator());
        int counter = 0 ;
        for (var user : collaboration.getUsers()) {
            result.append(user.getUsername());
            result.append(System.lineSeparator());
            counter++;
        }

        if (counter != 0 ) {
            return result.toString();
        }
        else {
            return "The following collaboration has no tasks";
        }
    }


    //side helpers
    @Override
    public User getCurrentUser(String username) {
        return users.stream().filter(other->other.getUsername().equals(username)).findAny().orElse(null);
    }

    private void addUsersAndCollaborationsToFile() {
        String userJson = gson.toJson(users);
        String collaborationJson = gson.toJson(collaborations);

        try (FileWriter fr = new FileWriter(file, false)) {
            try (BufferedWriter br = new BufferedWriter(fr)) {
                br.write(userJson);
                br.write(System.lineSeparator());
                br.write(collaborationJson);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void loadDataset() {

        users = new ArrayList<>();
        collaborations = new ArrayList<>();

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String usersLine = reader.readLine();
            String collaborationsLine = reader.readLine();

            Type typeUsers = new TypeToken<List<User>>() { }.getType();
            Type typeCollaborations = new TypeToken<List<Collaboration>>() { }.getType();

            if (usersLine != null) {
                users = new Gson().fromJson(usersLine, typeUsers);
            }

            if (collaborationsLine != null) {
                collaborations = new Gson().fromJson(collaborationsLine, typeCollaborations);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (var user  : users) {
            keysForLoggedUsers.put(user, false);
        }

    }
    private static final Database INSTANCE = new Database();

    private Database(File file) {
        this.file = file;
        loadDataset();
    }
    private Database() {
        loadDataset();
    }

}
