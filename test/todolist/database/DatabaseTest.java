package todolist.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static todolist.command.CommandsList.ADD_COLLABORATION;
import static todolist.command.CommandsList.ADD_TASK;
import static todolist.command.CommandsList.ADD_USER_TO_COLLABORATION;
import static todolist.command.CommandsList.ASSIGN_TASK_COLLABORATION;
import static todolist.command.CommandsList.LIST_TASKS_COLLABORATIONS;
import static todolist.command.CommandsList.LIST_USERS_COLLABORATIONS;
import static todolist.command.CommandsList.LOGIN;
import static todolist.command.CommandsList.REGISTER;
import static todolist.command.CommandsList.UPDATE_TASK;

import todolist.collaboration.Collaboration;
import todolist.command.Command;
import todolist.command.CommandExecutor;
import todolist.exceptions.CollaborationNotCreatorException;
import todolist.exceptions.CollaborationNotExistException;
import todolist.exceptions.InvalidParametersException;
import todolist.exceptions.TaskAlreadyExistsException;
import todolist.exceptions.UserAlreadyExistsException;
import todolist.exceptions.UserAlreadyLoggedException;
import todolist.exceptions.UserDoesNotExistException;
import todolist.messagesstatus.StatusMessages;
import todolist.user.User;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DatabaseTest {

    private Storage storage;

    private File tempFileData;

    private SocketChannel mockClientChannel;

    private final User temp  = new User("tempName", "tempPass");

    private  CommandExecutor cmdExecutor;


    @BeforeEach
    public void setUp() throws IOException {
        tempFileData = File.createTempFile("tempDatabase", ".txt");
        storage = Database.getInstanceWithCustomFile(tempFileData);
        mockClientChannel = mock(SocketChannel.class);
        cmdExecutor = new CommandExecutor(storage);

    }

    @AfterEach
    public void resetDown() {
        tempFileData.deleteOnExit();
    }

    @Test
    void testRegisterSuccess() throws UserAlreadyExistsException {
        assertTrue(storage.register(temp));
    }

    @Test
    void testRegisterUserAlreadyExistsException() throws UserAlreadyExistsException {
        storage.register(temp);
        assertThrows(UserAlreadyExistsException.class, () -> storage.register(temp),
                StatusMessages.ERROR.getMessage(String.format(
                        "User (%s) already exists or the (%s) password is not safe",
                        temp.getUsername(), temp.getPassword())));
    }

    @Test
    void testLogInSuccess() throws
            UserAlreadyExistsException, UserAlreadyLoggedException, UserDoesNotExistException {
        storage.register(temp);
        assertEquals(storage.logIn(temp, mockClientChannel), temp);
    }

    @Test
    void testLogInUserDoesNotExistException() {
        assertThrows(UserDoesNotExistException.class, () -> storage.logIn(temp, mockClientChannel),
                StatusMessages.ERROR.getMessage(String.format(
                        "User (%s) does not exists or the following password (%s) is incorrect",
                        temp.getUsername(), temp.getPassword())));
    }

    @Test
    void testLogInUserAlreadyLoggedException() throws
            UserAlreadyExistsException, UserAlreadyLoggedException, UserDoesNotExistException {
        storage.register(temp);
        storage.logIn(temp, mockClientChannel);
        assertThrows(UserAlreadyLoggedException.class, () -> storage.logIn(temp, mockClientChannel),
                StatusMessages.WARNING.getMessage(String.format(
                "User (%s) is already logged in. Wait until the user has logged off to use this account",
                temp.getUsername())));
    }


    @Test
    void testLogoutSuccess() throws
            UserAlreadyExistsException, UserAlreadyLoggedException, UserDoesNotExistException {
        storage.register(temp);
        storage.logIn(temp, mockClientChannel);
        assertTrue(storage.logout(mockClientChannel, temp));
    }

    @Test
    void testUpdateTaskOfUsersInDatabase() throws
            TaskAlreadyExistsException, InvalidParametersException {

        cmdExecutor.execute(mockClientChannel,new Command(REGISTER, new String[]{temp.getUsername(), temp.getPassword()}));
        cmdExecutor.execute(mockClientChannel,new Command(LOGIN, new String[]{temp.getUsername(), temp.getPassword()}));
        cmdExecutor.execute(mockClientChannel,new Command(ADD_TASK, new String[]{"task1", "14/2/2024", "14/3/2024", "description"}));
        String actual = cmdExecutor.execute(mockClientChannel,new Command(UPDATE_TASK,new String[]{"task1", "14/2/2024", "14/3/2024", "description"}));
        String expected = StatusMessages.SUCCESS.getMessage(String.format(
                "Task has been updated in (%s)'s list of tasks", temp.getUsername()));

        assertEquals(actual, expected);
    }

    @Test
    void testAddCollaboration ()
            throws UserAlreadyExistsException, UserAlreadyLoggedException, UserDoesNotExistException {
        storage.register(temp);
        storage.logIn(temp, mockClientChannel);
        assertTrue(storage.addCollaboration(new Collaboration("tempName", temp)));
    }

    @Test
    void testListCollaborations ()
            throws UserAlreadyExistsException, UserAlreadyLoggedException, UserDoesNotExistException {
        storage.register(temp);
        storage.logIn(temp, mockClientChannel);
        storage.addCollaboration(new Collaboration("tempName", temp));
        storage.addCollaboration(new Collaboration("tempName2", temp));

        String actual = storage.listCollaboration(temp);

        String expected = System.lineSeparator() +
        "tempName" + System.lineSeparator() +
        "tempName2" + System.lineSeparator();
        assertEquals(expected ,actual);
    }

    @Test
    void testDeleteCollaboration ()
            throws UserAlreadyExistsException, UserAlreadyLoggedException,
            UserDoesNotExistException, CollaborationNotCreatorException,
            CollaborationNotExistException {
        storage.register(temp);
        storage.logIn(temp, mockClientChannel);
        storage.addCollaboration(new Collaboration("tempName", temp));
        assertTrue(storage.deleteCollaboration("tempName", temp));
    }

    @Test
    void testDeleteCollaborationCollaborationNotExistException ()
            throws UserAlreadyExistsException, UserAlreadyLoggedException,
            UserDoesNotExistException {
        storage.register(temp);
        storage.logIn(temp, mockClientChannel);
        assertThrows(CollaborationNotExistException.class, ()->storage.deleteCollaboration("unknown", temp));
    }

    @Test
    void testDeleteCollaborationCollaborationNotCreatorException ()
            throws UserAlreadyExistsException, UserAlreadyLoggedException,
            UserDoesNotExistException {
        storage.register(temp);
        storage.logIn(temp, mockClientChannel);
        storage.addCollaboration(new Collaboration("tempName", temp));
        assertThrows(CollaborationNotCreatorException.class,
                ()->storage.deleteCollaboration(
                        "tempName", new User("otherUser", "otherPass")));
    }

    @Test
    void testListTaskCollaboration() throws
            TaskAlreadyExistsException, InvalidParametersException {

        cmdExecutor.execute(mockClientChannel,new Command(REGISTER,
                new String[]{temp.getUsername(), temp.getPassword()}));

        cmdExecutor.execute(mockClientChannel,new Command(LOGIN,
                new String[]{temp.getUsername(), temp.getPassword()}));

        cmdExecutor.execute(mockClientChannel,new Command(ADD_COLLABORATION,
                new String[]{"tempCollaboration", temp.getUsername()}));

        cmdExecutor.execute(mockClientChannel,new Command(ASSIGN_TASK_COLLABORATION,
                new String[]{"tempCollaboration", temp.getUsername(), "tempTask1"}));
        cmdExecutor.execute(mockClientChannel,new Command(ASSIGN_TASK_COLLABORATION,
                new String[]{"tempCollaboration", temp.getUsername(), "tempTask2"}));
        cmdExecutor.execute(mockClientChannel,new Command(ASSIGN_TASK_COLLABORATION,
                new String[]{"tempCollaboration", temp.getUsername(), "tempTask3"}));

        String actual = cmdExecutor.execute(mockClientChannel,new Command(LIST_TASKS_COLLABORATIONS,
                new String[]{"tempCollaboration"}));

        String expected = System.lineSeparator() +
                System.lineSeparator() +
                "Name : tempTask1" +
                System.lineSeparator() +
                System.lineSeparator() +
                System.lineSeparator() +
                "Name : tempTask2" +
                System.lineSeparator() +
                System.lineSeparator() +
                System.lineSeparator() +
                "Name : tempTask3" +
                System.lineSeparator() +
                System.lineSeparator() ;

        assertEquals(expected ,actual);

    }

    @Test
    void testListUsersCollaboration() throws
            TaskAlreadyExistsException, InvalidParametersException {

        cmdExecutor.execute(mockClientChannel,new Command(REGISTER,
                new String[]{temp.getUsername(), temp.getPassword()}));
        cmdExecutor.execute(mockClientChannel,new Command(REGISTER,
                new String[]{"tempUser1", "tempPass1"}));
        cmdExecutor.execute(mockClientChannel,new Command(REGISTER,
                new String[]{"tempUser2", "tempPass2"}));
        cmdExecutor.execute(mockClientChannel,new Command(REGISTER,
                new String[]{"tempUser3", "tempPass3"}));

        cmdExecutor.execute(mockClientChannel,new Command(LOGIN,
                new String[]{temp.getUsername(), temp.getPassword()}));

        cmdExecutor.execute(mockClientChannel,new Command(ADD_COLLABORATION,
                new String[]{"tempCollaboration", temp.getUsername()}));


        cmdExecutor.execute(mockClientChannel,new Command(ADD_USER_TO_COLLABORATION,
                new String[]{"tempCollaboration", "tempUser1"}));
        cmdExecutor.execute(mockClientChannel,new Command(ADD_USER_TO_COLLABORATION,
                new String[]{"tempCollaboration", "tempUser2"}));
        cmdExecutor.execute(mockClientChannel,new Command(ADD_USER_TO_COLLABORATION,
                new String[]{"tempCollaboration", "tempUser3"}));


        String actual = cmdExecutor.execute(mockClientChannel,new Command(LIST_USERS_COLLABORATIONS,
                new String[]{"tempCollaboration"}));

        String expected = System.lineSeparator() +
                "tempName" +  System.lineSeparator() +
                "tempUser1" +  System.lineSeparator() +
                "tempUser2" +  System.lineSeparator() +
                "tempUser3" +  System.lineSeparator() ;

        assertEquals(expected ,actual);

    }


}
