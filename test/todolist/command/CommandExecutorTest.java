package todolist.command;

import java.nio.channels.SocketChannel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todolist.collaboration.Collaboration;
import todolist.database.Storage;
import todolist.exceptions.CollaborationNotCreatorException;
import todolist.exceptions.CollaborationNotExistException;
import todolist.exceptions.DatePeriodException;
import todolist.exceptions.InvalidParametersException;
import todolist.exceptions.TaskAlreadyExistsException;
import todolist.messagesstatus.StatusMessages;
import todolist.order.TaskFactory;
import todolist.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static todolist.command.CommandsList.ADD_COLLABORATION;
import static todolist.command.CommandsList.ADD_TASK;
import static todolist.command.CommandsList.ADD_USER_TO_COLLABORATION;
import static todolist.command.CommandsList.ASSIGN_TASK_COLLABORATION;
import static todolist.command.CommandsList.DELETE_COLLABORATION;
import static todolist.command.CommandsList.DELETE_TASK;
import static todolist.command.CommandsList.FINISH_TASK;
import static todolist.command.CommandsList.GET_TASK;
import static todolist.command.CommandsList.LIST_COLLABORATIONS;
import static todolist.command.CommandsList.LIST_DASHBOARD;
import static todolist.command.CommandsList.LIST_TASKS;
import static todolist.command.CommandsList.LIST_TASKS_COLLABORATIONS;
import static todolist.command.CommandsList.LIST_USERS_COLLABORATIONS;
import static todolist.command.CommandsList.LOGIN;
import static todolist.command.CommandsList.LOGOUT;
import static todolist.command.CommandsList.REGISTER;
import static todolist.command.CommandsList.UPDATE_TASK;

public class CommandExecutorTest {

    private Storage storage; //database
    private CommandExecutor cmdExecutor;

    private Collaboration mockCollaboration;
    private SocketChannel mockClientChannel;

    private String testUsername = "tempUser";
    private String testUserPass = "tempPass";

    private final String collaborationName = "testCollaboration";

    @BeforeEach
    public void setUp() {
        storage = mock(Storage.class);
        cmdExecutor = new CommandExecutor(storage);
        mockClientChannel = mock(SocketChannel.class);
        mockCollaboration = mock(Collaboration.class);

    }



    @Test
    public void testRegisterUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException {

        Command register = new Command(REGISTER, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, register);

        String expected = StatusMessages.SUCCESS.getMessage(String.format("User %s has been added to the data base", testUsername));

        assertEquals(expected, actual, "Unexpected output for 'register user '");
    }


    @Test
    public void testRegisterUserCommandErrorUsername() throws TaskAlreadyExistsException, InvalidParametersException {

        testUsername = "";

        Command register = new Command(REGISTER, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, register);

        String expected = StatusMessages.ERROR.getMessage("Password or username is not available");

        assertEquals(expected, actual, "Unexpected output for 'register user '");
    }

    @Test
    public void testRegisterUserCommandErrorPassword() throws TaskAlreadyExistsException, InvalidParametersException {

        testUserPass = "";

        Command register = new Command(REGISTER, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, register);

        String expected = StatusMessages.ERROR.getMessage("Password or username is not available");

        assertEquals(expected, actual, "Unexpected output for 'register user '");
    }

    @Test
    public void testRegisterUserCommandErrorBothPassAndUser() throws TaskAlreadyExistsException, InvalidParametersException {

        testUsername = "";
        testUserPass = "";

        Command register = new Command(REGISTER, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, register);

        String expected = StatusMessages.ERROR.getMessage("Password or username is not available");

        assertEquals(expected, actual, "Unexpected output for 'register user '");
    }

    @Test
    public void testLogInUserCommandErrorBothPassAndUser() throws TaskAlreadyExistsException, InvalidParametersException {

        testUsername = "";
        testUserPass = "";

        Command login = new Command(LOGIN, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, login);

        String expected = StatusMessages.ERROR.getMessage("Password or username is not available");

        assertEquals(expected, actual, "Unexpected output for 'register user '");
    }

    @Test
    public void testLogInUserCommandErrorPassword() throws TaskAlreadyExistsException, InvalidParametersException {

        testUserPass = "";

        Command login = new Command(LOGIN, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, login);

        String expected = StatusMessages.ERROR.getMessage("Password or username is not available");

        assertEquals(expected, actual, "Unexpected output for 'register user '");
    }

    @Test
    public void testLogInUserCommandErrorUsername() throws TaskAlreadyExistsException, InvalidParametersException {

        testUsername = "";

        Command login = new Command(LOGIN, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, login);

        String expected = StatusMessages.ERROR.getMessage("Password or username is not available");

        assertEquals(expected, actual, "Unexpected output for 'register user '");
    }


    @Test
    public void testLogInUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException {


        Command login = new Command(LOGIN, new String[]{testUsername,testUserPass});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, login);

        String expected = StatusMessages.SUCCESS.getMessage(String.format("User (%s) has logged in", testUsername));

        assertEquals(expected, actual, "Unexpected output for 'login user '");
    }

    @Test
    public void testUnknownCommand() throws TaskAlreadyExistsException, InvalidParametersException {

        String unknownCommand = "unknown";

        Command command = new Command(unknownCommand, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(false);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.WARNING.getMessage("Unknown command");
        assertEquals(expected, actual, "Unexpected output for ' unknown command '");
    }

    @Test
    public void testRegisterUserCommandWhenInSession() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(REGISTER, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.WARNING.getMessage(
                "Already in session! To register a new account logout of the current one");
        assertEquals(expected, actual, "Unexpected output for ' register user '");
    }

    @Test
    public void testLogInUserCommandWhenInSession() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(LOGIN, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.WARNING.getMessage("Already in session ! You are already logged in!");
        assertEquals(expected, actual, "Unexpected output for ' login user '");
    }

    @Test
    public void testAddTaskUserCommandErrorNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(ADD_TASK, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Name of a task must be provided");
        assertEquals(expected, actual, "Unexpected output for ' add task '");
    }

    @Test
    public void testAddTaskUserCommandErrorInvalidName() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(ADD_TASK, new String[]{""});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for creating a task");

        assertEquals(expected, actual, "Unexpected output for ' add task '");
    }

    @Test
    public void testAddTaskUserCommandErrorInvalidDateInput() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(ADD_TASK, new String[]{"task1","invalidDate"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Date must in valid format : (d/M/yyyy)");

        assertEquals(expected, actual, "Unexpected output for ' add task '");
    }

    @Test
    public void testAddTaskUserCommandErrorInvalidDatePastTime() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(ADD_TASK, new String[]{"task1","11/2/2023"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Can not have a task for the past");

        assertEquals(expected, actual, "Unexpected output for ' add task '");
    }



    @Test
    public void testAddTaskUserCommandErrorInvalidDeadLine() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(ADD_TASK, new String[]{"task1", "12/3/2023", "invalidDate"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Date must in valid format : (d/M/yyyy)");

        assertEquals(expected, actual, "Unexpected output for ' add task '");
    }

    @Test
    public void testAddTaskUserCommandErrorDeadLineBeforeBeginDate() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(ADD_TASK, new String[]{"task1", "12/3/2024", "12/1/2024"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Can not begin a task after its deadline");

        assertEquals(expected, actual, "Unexpected output for ' add task '");
    }

    @Test
    public void testAddTaskUserCommandSuccessAllParameters() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_TASK, new String[]{"task1", "12/3/2024", "12/7/2024", "This is a brief test description"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);
        String expected = StatusMessages.SUCCESS.getMessage(String.format(
                "Task has been added to (%s)'s list of tasks", testUsername));

       assertEquals(expected, actual, "Unexpected output for ' add task '");
    }

    @Test
    public void testUpdateTaskUserCommandErrorTaskDoesNotExist() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(UPDATE_TASK, new String[]{"task1", "12/3/2024", "12/7/2024", "This is a brief test description"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Task you are searching for does not exists");

        assertEquals(expected, actual, "Unexpected output for ' update task '");
    }

    @Test
    public void testUpdateTaskUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {


        Command command = new Command(UPDATE_TASK, new String[]{"task1", "12/3/2024", "12/7/2024", "This is a brief test description"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        cmdExecutor.getCurrentUser(mockClientChannel).addTask(TaskFactory.createTask(new String[]{"task1"}));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.SUCCESS.getMessage(String.format(
                "Task has been updated in (%s)'s list of tasks", cmdExecutor.getCurrentUser(mockClientChannel).getUsername()));

        assertEquals(expected, actual, "Unexpected output for ' update task '");
    }



    @Test
    public void testListTasksUserCommandNoTasks() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(LIST_TASKS, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = "The current user has no tasks !";

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserCommandAllTasks() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {


        Command command = new Command(LIST_TASKS, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));

        cmdExecutor.getCurrentUser(mockClientChannel).addTask(
                TaskFactory.createTask(new String[]{"task1", "12/3/2024", "12/7/2024", "This is a brief test description"}));

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = System.lineSeparator() +
                "Tasks regular : " +  System.lineSeparator() +
                System.lineSeparator() +
                "Name : task1" +  System.lineSeparator() +
                "Date : 12/3/2024" +  System.lineSeparator() +
                "Date : 12/7/2024" +  System.lineSeparator() +
                "Date : This is a brief test description " +  System.lineSeparator() +
                " "+ System.lineSeparator() +
                "Tasks inbox : " +  System.lineSeparator();
        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }


    @Test
    public void testListTasksUserErrorUnavailableCountOfSpecifiers() throws TaskAlreadyExistsException, InvalidParametersException {


        String parameter = "unavailable";

        Command command = new Command(LIST_TASKS, new String[]{parameter, parameter, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages
                .ERROR.getMessage("Undefined number of specifiers have been provided (only one is available)");

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserErrorUnavailableSpecifier() throws TaskAlreadyExistsException, InvalidParametersException {


        String parameter = "unavailable";

        Command command = new Command(LIST_TASKS, new String[]{parameter, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages
                .ERROR.getMessage("Unavailable specifiers has been used (you can list only by completion or date)");

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserErrorUnavailableParameterCompleted() throws TaskAlreadyExistsException, InvalidParametersException {


        String specifier = "completed";
        String parameter = "unavailable";

        Command command = new Command(LIST_TASKS, new String[]{specifier, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages
                .ERROR.getMessage("Completed can only be followed by true or false parameter");

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserErrorUnavailableParameterDate() throws TaskAlreadyExistsException, InvalidParametersException {


        String specifier = "date";
        String parameter = "unavailable";

        Command command = new Command(LIST_TASKS, new String[]{specifier, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages
                .ERROR.getMessage("Date must in valid format : (d/M/yyyy)");

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserParameterDate() throws TaskAlreadyExistsException, InvalidParametersException {


        String specifier = "date";
        String parameter = "13/3/2024";

        Command command = new Command(LIST_TASKS, new String[]{specifier, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected =  "The current user has no tasks !";

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserParameterCompleted() throws TaskAlreadyExistsException, InvalidParametersException {


        String specifier = "completed";
        String parameter = "true";

        Command command = new Command(LIST_TASKS, new String[]{specifier, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected =  "The current user has no tasks !";

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserParameterCompletedHasTasks() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {


        String specifier = "completed";
        String parameter = "false";

        Command command = new Command(LIST_TASKS, new String[]{specifier, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        cmdExecutor.getCurrentUser(mockClientChannel).addTask(
                TaskFactory.createTask(new String[]{"task1", "12/3/2024", "12/7/2024", "This is a brief test description"}));


        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = System.lineSeparator() +
                "Tasks regular : " +  System.lineSeparator() +
                System.lineSeparator() +
                "Name : task1" +  System.lineSeparator() +
                "Date : 12/3/2024" +  System.lineSeparator() +
                "Date : 12/7/2024" +  System.lineSeparator() +
                "Date : This is a brief test description " +  System.lineSeparator() +
                " "+ System.lineSeparator() +
                "Tasks inbox : " +  System.lineSeparator();

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testListTasksUserParameterDateHasTasks() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {


        String specifier = "date";
        String parameter = "12/4/2024";

        Command command = new Command(LIST_TASKS, new String[]{specifier, parameter});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        cmdExecutor.getCurrentUser(mockClientChannel).addTask(
                TaskFactory.createTask(new String[]{"task1", "12/3/2024", "12/7/2024", "This is a brief test description"}));


        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = System.lineSeparator() +
                "Tasks regular : " +  System.lineSeparator() +
                System.lineSeparator() +
                "Name : task1" +  System.lineSeparator() +
                "Date : 12/3/2024" +  System.lineSeparator() +
                "Date : 12/7/2024" +  System.lineSeparator() +
                "Date : This is a brief test description " +  System.lineSeparator() +
                " "+ System.lineSeparator() +
                "Tasks inbox : " +  System.lineSeparator();

        assertEquals(expected, actual, "Unexpected output for ' list tasks '");
    }

    @Test
    public void testDeleteTaskUserCommandErrorNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(DELETE_TASK, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Name of a task must be provided");
        assertEquals(expected, actual, "Unexpected output for ' delete task '");
    }

    @Test
    public void testDeleteTaskUserCommandErrorInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(DELETE_TASK, new String[]{""});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected =  StatusMessages.ERROR.getMessage("Invalid arguments for deleting a task");
        assertEquals(expected, actual, "Unexpected output for ' delete task '");
    }

    @Test
    public void testDeleteTaskUserCommandErrorTaskNotExist() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(DELETE_TASK, new String[]{"non-existent"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Task you are searching for does not exists");
        assertEquals(expected, actual, "Unexpected output for ' delete task '");
    }

    @Test
    public void testDeleteTaskUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {


        Command command = new Command(DELETE_TASK, new String[]{"task5"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        cmdExecutor.getCurrentUser(mockClientChannel).addTask(
                TaskFactory.createTask(new String[]{"task5"}));

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.SUCCESS.getMessage(
                String.format("Task has been deleted from (%s)'s list of tasks", testUsername));
        assertEquals(expected, actual, "Unexpected output for ' delete task '");
    }

    @Test
    public void testGetTaskUserCommandCommandNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(GET_TASK, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Name of a task must be provided");

        assertEquals(expected, actual, "Unexpected output for ' get task '");
    }

    @Test
    public void testGetTaskUserCommandCommandInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(GET_TASK, new String[]{""});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for getting a task");

        assertEquals(expected, actual, "Unexpected output for ' get task '");
    }

    @Test
    public void testGetTaskUserCommandTaskNotExist() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(GET_TASK, new String[]{"task5"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Task you are searching for does not exists");

        assertEquals(expected, actual, "Unexpected output for ' get task '");
    }

    @Test
    public void testGetTaskUserCommand() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {

        Command command = new Command(GET_TASK, new String[]{"task5"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        cmdExecutor.getCurrentUser(mockClientChannel).addTask(
                TaskFactory.createTask(new String[]{"task5", "13/2/2023", "20/2/2023", "This is description test"}));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = "" + System.lineSeparator() +
                "Name : task5" + System.lineSeparator()+
                "Date : 15/2/2023" + System.lineSeparator()+
                "Date : 20/2/2023" + System.lineSeparator()+
                "Date : This is description test " + System.lineSeparator();

        assertEquals(expected, actual, "Unexpected output for ' get task '");
    }

    @Test
    public void testListDashboardUserCommand() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {

        String specifier = "date";

        Command command = new Command(LIST_DASHBOARD, new String[]{specifier});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));

        cmdExecutor.getCurrentUser(mockClientChannel).addTask(
                TaskFactory.createTask(new String[]{"task7", "13/2/2023", "12/7/2024", "This is a brief test description"}));


        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = System.lineSeparator() +
                "Tasks regular : " +  System.lineSeparator() +
                System.lineSeparator() +
                "Name : task7" +  System.lineSeparator() +
                "Date : 13/2/2023" +  System.lineSeparator() +
                "Date : 12/7/2024" +  System.lineSeparator() +
                "Date : This is a brief test description " +  System.lineSeparator() +
                " "+ System.lineSeparator() +
                "Tasks inbox : " +  System.lineSeparator();

        assertEquals(expected, actual, "Unexpected output for ' list dashboard '");
    }

    @Test
    public void testFinishUserCommandNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(FINISH_TASK, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Name of a task must be provided");

        assertEquals(expected, actual, "Unexpected output for ' finish task '");
    }

    @Test
    public void testFinishUserCommandInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(FINISH_TASK, new String[]{""});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for finishing a task");

        assertEquals(expected, actual, "Unexpected output for ' finish task '");
    }

    @Test
    public void testFinishUserCommandTaskNotExist() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(FINISH_TASK, new String[]{"task5"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Task you are searching for does not exists");

        assertEquals(expected, actual, "Unexpected output for ' finish task '");
    }

    @Test
    public void testFinishUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException, DatePeriodException {

        Command command = new Command(FINISH_TASK, new String[]{"task5"});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        cmdExecutor.getCurrentUser(mockClientChannel).addTask(TaskFactory
                .createTask(new String[]{"task5", "13/2/2023", "20/2/2023", "This is description test"}));
        cmdExecutor.getCurrentUser(mockClientChannel).getTask("task5").setIsCompleted(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.SUCCESS.getMessage(
                String.format("Task has been finished from (%s)'s list of tasks", testUsername));

        assertEquals(expected, actual, "Unexpected output for ' finish task '");
    }

    @Test
    public void testAddCollaborationUserCommandErrorNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {
        Command command = new Command(ADD_COLLABORATION, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Name of a collaboration must be provided");

        assertEquals(expected, actual, "Unexpected output for ' add collaboration '");
    }

    @Test
    public void testAddCollaborationUserCommandErrorInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {
        Command command = new Command(ADD_COLLABORATION, new String[]{""});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for creating a collaboration");

        assertEquals(expected, actual, "Unexpected output for ' add collaboration '");
    }

    @Test
    public void testAddCollaborationUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_COLLABORATION, new String[]{collaborationName});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        storage.addCollaboration(new Collaboration(collaborationName, cmdExecutor.getCurrentUser(mockClientChannel)));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.SUCCESS.getMessage(String.format(
                "Collaboration has been created by (%s)", testUsername));

        assertEquals(expected, actual, "Unexpected output for ' add collaboration '");
    }

    @Test
    public void testListCollaborationsUserCommandNoCollaborations() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(LIST_COLLABORATIONS, new String[]{collaborationName});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));

        storage.addCollaboration(new Collaboration(collaborationName, cmdExecutor.getCurrentUser(mockClientChannel)));

        String actual = cmdExecutor.execute(mockClientChannel, command);

        assertNull(actual, "Unexpected output for ' list collaboration '");
    }

    @Test
    public void testAddUserToCollaborationUserCommandNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_USER_TO_COLLABORATION, new String[]{});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        storage.addCollaboration(new Collaboration(collaborationName, cmdExecutor.getCurrentUser(mockClientChannel)));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Collaboration name and account's username must be provided");

        assertEquals(expected, actual, "Unexpected output for ' add users to collaboration '");
    }

    @Test
    public void testAddUserToCollaborationUserCommandInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_USER_TO_COLLABORATION, new String[]{"", " "});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        storage.addCollaboration(new Collaboration(collaborationName, cmdExecutor.getCurrentUser(mockClientChannel)));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for searching a collaboration");

        assertEquals(expected, actual, "Unexpected output for ' add users to collaboration '");
    }

    @Test
    public void testAddUserToCollaborationUserCommandCollaborationDoesNotExist() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_USER_TO_COLLABORATION, new String[]{collaborationName, testUsername});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected =  StatusMessages.ERROR.getMessage("Provided collaboration does not exist in the database");

        assertEquals(expected, actual, "Unexpected output for ' add users to collaboration '");
    }

    @Test
    public void testAddUserToCollaborationUserCommandUserDoesNotExist() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_USER_TO_COLLABORATION, new String[]{collaborationName, testUsername});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        when(storage.getCollaboration(collaborationName)).thenReturn(new Collaboration(collaborationName, cmdExecutor.getCurrentUser(mockClientChannel)));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected =  StatusMessages.ERROR.getMessage("Provided user does not exist in the database");

        assertEquals(expected, actual, "Unexpected output for ' add users to collaboration '");
    }


    @Test
    public void testAddUserToCollaborationUserCommandUserPartOfCollaboration() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_USER_TO_COLLABORATION, new String[]{collaborationName, testUsername});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        when(storage.getCollaboration(collaborationName)).thenReturn(mockCollaboration);
        when(storage.getCurrentUser(testUsername)).thenReturn(new User(testUsername, testUserPass));
        when(storage.getCurrentUser(testUsername)).thenReturn(new User(testUsername, testUserPass));

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Provided user is not a part of this collaboration");

        assertEquals(expected, actual, "Unexpected output for ' add users to collaboration '");
    }

    @Test
    public void testAddUserToCollaborationUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException {


        Command command = new Command(ADD_USER_TO_COLLABORATION, new String[]{collaborationName, testUsername});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        when(storage.getCollaboration(collaborationName)).thenReturn(mockCollaboration);
        when(storage.getCurrentUser(testUsername)).thenReturn(new User(testUsername, testUserPass));
        when(storage.getCurrentUser(testUsername)).thenReturn(new User(testUsername, testUserPass));
        when(mockCollaboration.hasUser(cmdExecutor.getCurrentUser(mockClientChannel))).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.SUCCESS.getMessage(
                String.format(
                        "User (%s) has been added to collaboration (%s)", testUsername, collaborationName));

        assertEquals(expected, actual, "Unexpected output for ' add users to collaboration '");
    }

    @Test
    public void testDeleteCollaborationUserCommandNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(DELETE_COLLABORATION, new String[]{});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Name of a collaboration must be provided");

        assertEquals(expected, actual, "Unexpected output for '  collaboration '");
    }

    @Test
    public void testDeleteCollaborationUserCommandInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(DELETE_COLLABORATION, new String[]{""});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for deleting a collaboration");

        assertEquals(expected, actual, "Unexpected output for ' delete collaboration '");
    }

    @Test
    public void testDeleteCollaborationUserCommandCollaborationNotExist()
            throws TaskAlreadyExistsException, InvalidParametersException,
            CollaborationNotCreatorException, CollaborationNotExistException {



        Command command = new Command(DELETE_COLLABORATION, new String[]{collaborationName});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        when(storage.deleteCollaboration(collaborationName,  cmdExecutor.getCurrentUser(mockClientChannel)))
                .thenThrow(new CollaborationNotExistException(StatusMessages.ERROR.getMessage(String.format(
                        "Collaboration (%s) does not exist in database", collaborationName))));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage(String.format(
                "Collaboration (%s) does not exist in database", collaborationName));

        assertEquals(expected, actual, "Unexpected output for ' delete collaboration '");
    }

    @Test
    public void testDeleteCollaborationUserCommandNotCreator()
            throws TaskAlreadyExistsException, InvalidParametersException,
            CollaborationNotCreatorException, CollaborationNotExistException {



        Command command = new Command(DELETE_COLLABORATION, new String[]{collaborationName});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        when(storage.deleteCollaboration(collaborationName,  cmdExecutor.getCurrentUser(mockClientChannel)))
                .thenThrow(new CollaborationNotCreatorException(StatusMessages.ERROR.getMessage(String.format(
                        " Only creator can delete collaboration (%s)", collaborationName))));
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage(String.format(
                " Only creator can delete collaboration (%s)", collaborationName));

        assertEquals(expected, actual, "Unexpected output for ' delete collaboration '");
    }



    @Test
    public void testDeleteCollaborationUserCommandSuccess()
            throws TaskAlreadyExistsException, InvalidParametersException {



        Command command = new Command(DELETE_COLLABORATION, new String[]{collaborationName});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);


        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages
                .SUCCESS.getMessage(String.format("Collaboration (%s) has been deleted", collaborationName));

        assertEquals(expected, actual, "Unexpected output for ' delete collaboration '");
    }


    @Test
    public void testAssignTaskCollaborationsUserCommandNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(ASSIGN_TASK_COLLABORATION, new String[]{});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Collaboration name, account's username and task name must be provided");

        assertEquals(expected, actual, "Unexpected output for ' assigning to collaboration '");
    }

    @Test
    public void testAssignTaskCollaborationsUserCommandInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {
        Command command = new Command(ASSIGN_TASK_COLLABORATION, new String[]{collaborationName, "", ""});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for searching a collaboration");

        assertEquals(expected, actual, "Unexpected output for ' assigning to  collaboration '");
    }

    @Test
    public void testAssignTaskCollaborationsUserCommandCollaborationNotExist() throws TaskAlreadyExistsException, InvalidParametersException {
        String collaborationName = "collaborationTest";
        Command command = new Command(ASSIGN_TASK_COLLABORATION, new String[]{collaborationName, "user", "task"});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Provided collaboration does not exist in the database");

        assertEquals(expected, actual, "Unexpected output for ' assigning to  collaboration '");
    }

    @Test
    public void testAssignTaskCollaborationsUserCommandUserNotExist() throws TaskAlreadyExistsException, InvalidParametersException {
        String collaborationName = "collaborationTest";
        Command command = new Command(ASSIGN_TASK_COLLABORATION, new String[]{collaborationName, "user", "task"});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        when(storage.getCollaboration(collaborationName)).thenReturn(mockCollaboration);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Provided user is not a part of this collaboration");

        assertEquals(expected, actual, "Unexpected output for ' assigning to  collaboration '");
    }


    @Test
    public void testAssignTaskCollaborationsUserCommandSuccess() throws TaskAlreadyExistsException, InvalidParametersException {
        Command command = new Command(ASSIGN_TASK_COLLABORATION, new String[]{collaborationName, "user", "task"});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        when(storage.getCollaboration(collaborationName)).thenReturn(mockCollaboration);
        when(mockCollaboration.hasUser(cmdExecutor.getCurrentUser(mockClientChannel))).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);
        String expected = StatusMessages.SUCCESS.getMessage(
                String.format(
                        "Task has been assigned to user (%s) from collaboration (%s)",
                        "user", collaborationName));

        assertEquals(expected, actual, "Unexpected output for ' assigning to  collaboration '");
    }


    @Test
    public void testListTasksCollaborationsUserCommandNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(LIST_TASKS_COLLABORATIONS, new String[]{});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Name of a collaboration must be provided");

        assertEquals(expected, actual, "Unexpected output for ' listing from collaboration '");
    }

    @Test
    public void testListTasksCollaborationsUserCommandInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {
        Command command = new Command(LIST_TASKS_COLLABORATIONS, new String[]{""});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for listing tasks from collaboration");

        assertEquals(expected, actual, "Unexpected output for ' listing from  collaboration '");
    }

    //listUsersCollaborationsUserCommand

    @Test
    public void testListUsersCollaborationsUserCommandNoArguments() throws TaskAlreadyExistsException, InvalidParametersException {

        Command command = new Command(LIST_USERS_COLLABORATIONS, new String[]{});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Collaboration name must be provided ");

        assertEquals(expected, actual, "Unexpected output for ' listing from collaboration '");
    }

    @Test
    public void testListUsersCollaborationsUserCommandInvalidArguments() throws TaskAlreadyExistsException, InvalidParametersException {
        Command command = new Command(LIST_USERS_COLLABORATIONS, new String[]{""});
        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.ERROR.getMessage("Invalid arguments for listing users from collaboration");

        assertEquals(expected, actual, "Unexpected output for ' listing from  collaboration '");
    }

    @Test
    public void testLogOutUserCommand() throws TaskAlreadyExistsException, InvalidParametersException {
        Command command = new Command(LOGOUT, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        cmdExecutor.addToChannelsForUsers(mockClientChannel, new User(testUsername, testUserPass));
        when(storage.logout(mockClientChannel, cmdExecutor.getCurrentUser(mockClientChannel))).thenReturn(true);
        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.SUCCESS.getMessage(
                String.format("User (%s) has logged off", testUsername));

        assertEquals(expected, actual, "Unexpected output for ' listing from  collaboration '");
    }
    @Test
    public void testLogOutUserUnknownCommandActiveSession() throws TaskAlreadyExistsException, InvalidParametersException {

        String unknownCommand = "unknown";
        Command command = new Command(unknownCommand, new String[]{});

        when(storage.isItAccessible(mockClientChannel)).thenReturn(true);

        String actual = cmdExecutor.execute(mockClientChannel, command);

        String expected = StatusMessages.WARNING.getMessage("Unknown command");

        assertEquals(expected, actual, "Unexpected output for ' listing from  collaboration '");
    }







}