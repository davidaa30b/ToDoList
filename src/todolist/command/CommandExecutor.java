package todolist.command;



import todolist.collaboration.Collaboration;
import todolist.database.Storage;
import todolist.exceptions.CollaborationNotCreatorException;
import todolist.exceptions.CollaborationNotExistException;
import todolist.exceptions.DatePeriodException;
import todolist.exceptions.InvalidParametersException;
import todolist.exceptions.TaskAlreadyExistsException;
import todolist.exceptions.TaskDoesNotExistException;
import todolist.exceptions.UserAlreadyExistsException;
import todolist.exceptions.UserAlreadyLoggedException;
import todolist.exceptions.UserDoesNotExistException;
import todolist.order.TaskFactory;
import todolist.user.User;
import todolist.validation.Validation;
import todolist.messagesstatus.StatusMessages;
import todolist.user.UserValidation;

import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
import static todolist.messagesstatus.StatusMessages.WARNING;
import static todolist.validation.Validation.validString;

public class CommandExecutor {
    private final Storage database;

    private User currentUser;
    Map<SocketChannel, User> channelsForUsers = new HashMap<>();

    public void addToChannelsForUsers(SocketChannel clientChannel, User user) {
        channelsForUsers.put(clientChannel, user);
    }

    public User getCurrentUser(SocketChannel clientChannel) {
        return channelsForUsers.get(clientChannel);
    }


    public CommandExecutor(Storage database) {
        this.database = database;
    }

    public String execute(SocketChannel clientChannel, Command cmd)
            throws TaskAlreadyExistsException, InvalidParametersException {
        database.identifyChannel(clientChannel);

        if (!database.isItAccessible(clientChannel)) {
            return switch (cmd.command()) {
                case REGISTER -> registerUserCommand(cmd.arguments());
                case LOGIN -> logInUserCommand(clientChannel, cmd.arguments());
                default -> WARNING.getMessage("Unknown command");
            };
        }
        else {
            return switch (cmd.command()) {
                case REGISTER -> WARNING.getMessage(
                        "Already in session! To register a new account logout of the current one");
                case LOGIN -> WARNING.getMessage("Already in session ! You are already logged in!");
                case ADD_TASK -> addTaskUserCommand(clientChannel, cmd.arguments());
                case UPDATE_TASK -> updateTaskUserCommand(clientChannel, cmd.arguments());
                case LIST_TASKS -> listTasksUserCommand(clientChannel, cmd.arguments());
                case DELETE_TASK -> deleteTaskUserCommand(clientChannel, cmd.arguments());
                case GET_TASK -> getTaskUserCommand(clientChannel, cmd.arguments());
                case LIST_DASHBOARD -> listDashboardUserCommand(clientChannel);
                case FINISH_TASK -> finishUserCommand(clientChannel, cmd.arguments());
                case ADD_COLLABORATION -> addCollaborationUserCommand(clientChannel, cmd.arguments());
                case LIST_COLLABORATIONS -> listCollaborationsUserCommand(clientChannel);
                case ADD_USER_TO_COLLABORATION -> addUserToCollaborationUserCommand(clientChannel, cmd.arguments());
                case DELETE_COLLABORATION -> deleteCollaborationUserCommand(clientChannel, cmd.arguments());
                case ASSIGN_TASK_COLLABORATION -> assignTaskCollaborationsUserCommand( cmd.arguments());
                case LIST_TASKS_COLLABORATIONS -> listTasksCollaborationsUserCommand( cmd.arguments());
                case LIST_USERS_COLLABORATIONS -> listUsersCollaborationsUserCommand( cmd.arguments());
                case LOGOUT -> logOutUserCommand(clientChannel);
                default -> WARNING.getMessage("Unknown command");
            };
        }
    }



    private String registerUserCommand(String[] args) {
        if (!Validation.isObjNull(UserValidation.checkIfPassOrNameProvided(args))) {
            return UserValidation.checkIfPassOrNameProvided(args);
        }

        final int username = 0;
        final int password = 1;

        try {
            database.register(new User(args[username], args[password]));
        } catch (UserAlreadyExistsException e) {
            return e.getLocalizedMessage();
        }

        return StatusMessages.SUCCESS.getMessage(
                String.format("User %s has been added to the data base", args[username]));
    }

    private String logInUserCommand(SocketChannel clientChannel, String[] args) {

        if (!Validation.isObjNull(UserValidation.checkIfPassOrNameProvided(args))) {
            return UserValidation.checkIfPassOrNameProvided(args);
        }

        final int username = 0;
        final int password = 1;

        //User currentUser;

        try {
            currentUser = database.logIn(new User(args[username], args[password]), clientChannel);
            addToChannelsForUsers(clientChannel, currentUser);
        } catch (UserDoesNotExistException | UserAlreadyLoggedException e) {
            return e.getLocalizedMessage();
        }

        return StatusMessages.SUCCESS.getMessage(String.format("User (%s) has logged in", args[username]));
    }

    private String logOutUserCommand(SocketChannel clientChannel) {
        database.logout(clientChannel, getCurrentUser(clientChannel));
        return StatusMessages.SUCCESS.getMessage(
                String.format("User (%s) has logged off", getCurrentUser(clientChannel).getUsername()));
    }

    private String addTaskUserCommand(SocketChannel clientChannel, String[] args)  {

        final int noArguments = 0;
        if (args.length == noArguments ) {
            return  StatusMessages.ERROR.getMessage("Name of a task must be provided");
        }

        currentUser =  getCurrentUser(clientChannel);

        try  {
            currentUser.addTask(TaskFactory.createTask(args));
            database.updateUsersDatabase(currentUser);
        }
        catch (TaskAlreadyExistsException | DatePeriodException | InvalidParametersException e ) {
            return e.getLocalizedMessage();
        }

        return StatusMessages.SUCCESS.getMessage(String.format(
                "Task has been added to (%s)'s list of tasks", currentUser.getUsername()));
    }

    private String listTasksUserCommand(SocketChannel clientChannel, String[] args)  {
        //User
        currentUser =  getCurrentUser(clientChannel);
        final int noSpecifiers = 0;
        final int specifier = 0;
        final int hasSpecifiers = 2;
        final int parameter = 1;
        if (args.length == noSpecifiers) {
            return currentUser.listTasks();
        }
        else if (args.length == hasSpecifiers) {
            if (args[specifier].equals("completed")) {
                if (args[parameter].equals("true")) {
                    return currentUser.listTaskByCompletion(true);
                }
                else  if (args[parameter].equals("false")) {
                    return currentUser.listTaskByCompletion(false);
                }
                else {
                    return StatusMessages.ERROR.getMessage("Completed can only be followed by true or false parameter");
                }
            }
            else if (args[specifier].equals("date")) {
                try {

                    return currentUser.listDashboard(Validation.getDateInDateTime(args[1]));
                }
                catch (InvalidParametersException e) {
                    return e.getLocalizedMessage();
                }
            }
            else {
                return StatusMessages.ERROR.getMessage(
                        "Unavailable specifiers has been used (you can list only by completion or date)");
            }
        }

        return StatusMessages
                .ERROR.getMessage("Undefined number of specifiers have been provided (only one is available)");
    }

    private String updateTaskUserCommand(SocketChannel clientChannel, String[] args) throws TaskAlreadyExistsException {
        //User
        currentUser =  getCurrentUser(clientChannel);
        try {
            currentUser.updateTask(TaskFactory.createTask(args));
            database.updateUsersDatabase(currentUser);
        }
        catch (TaskDoesNotExistException | DatePeriodException | InvalidParametersException e) {
            return e.getLocalizedMessage();
        }
        return StatusMessages.SUCCESS.getMessage(String.format(
                "Task has been updated in (%s)'s list of tasks", currentUser.getUsername()));
    }

    private String deleteTaskUserCommand(SocketChannel clientChannel, String[] args) {
        //User
        currentUser =  getCurrentUser(clientChannel);
        final int noArguments = 0;
        final int taskName = 0;
        if (args.length == noArguments) {
            return  StatusMessages.ERROR.getMessage("Name of a task must be provided");
        }

        if (!validString(args[taskName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for deleting a task");
        }

        try {
            currentUser.deleteTask(args[taskName]);
            database.updateUsersDatabase(currentUser);
        }
        catch (TaskDoesNotExistException e) {
            return e.getLocalizedMessage();
        }

        return StatusMessages.SUCCESS.getMessage(
                String.format("Task has been deleted from (%s)'s list of tasks", currentUser.getUsername()));
    }

    private String getTaskUserCommand(SocketChannel clientChannel, String[] args) {
        //User
        currentUser =  getCurrentUser(clientChannel);
        final int noArguments = 0;
        final int taskName = 0;

        if (args.length == noArguments ) {
            return  StatusMessages.ERROR.getMessage("Name of a task must be provided");
        }

        if (!validString(args[taskName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for getting a task");
        }

        try {
            return currentUser.listOneTask(args[taskName]);
        }
        catch (TaskDoesNotExistException e) {
            return e.getLocalizedMessage();
        }

    }

    private String listDashboardUserCommand(SocketChannel clientChannel) throws InvalidParametersException {
        //User
        currentUser =  getCurrentUser(clientChannel);
        return currentUser.listDashboard(LocalDate.now());
    }

    private String finishUserCommand(SocketChannel clientChannel, String[] args) throws TaskAlreadyExistsException {
        final int noArguments = 0;
        final int taskName = 0;

        if (args.length == noArguments ) {
            return  StatusMessages.ERROR.getMessage("Name of a task must be provided");
        }

        if (!validString(args[taskName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for finishing a task");
        }

        //User
        currentUser =  getCurrentUser(clientChannel);

        try {
            currentUser.finishTask(args[taskName]);
            database.updateUsersDatabase(currentUser);
        }
        catch (TaskDoesNotExistException e) {
            return e.getLocalizedMessage();
        }

        return StatusMessages.SUCCESS.getMessage(
                String.format("Task has been finished from (%s)'s list of tasks", currentUser.getUsername()));
    }

    private String addCollaborationUserCommand(SocketChannel clientChannel, String[] args) {
        //User
        currentUser =  getCurrentUser(clientChannel);
        final int noArguments = 0;
        final int collaborationName = 0;

        if (args.length == noArguments ) {
            return  StatusMessages.ERROR.getMessage("Name of a collaboration must be provided");
        }

        if (!validString(args[collaborationName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for creating a collaboration");
        }

        database.addCollaboration(new Collaboration(args[collaborationName], currentUser));

        return StatusMessages.SUCCESS.getMessage(String.format(
                "Collaboration has been created by (%s)", currentUser.getUsername()));
    }

    private String listCollaborationsUserCommand(SocketChannel clientChannel) {

        currentUser =  getCurrentUser(clientChannel);

        return database.listCollaboration(currentUser);
    }

    private String addUserToCollaborationUserCommand(SocketChannel clientChannel, String[] args) {
        currentUser =  getCurrentUser(clientChannel);
        final int hasArguments = 2;
        final int collaborationName = 0;
        final int username = 1;
        if (args.length != hasArguments ) {
            return  StatusMessages.ERROR.getMessage("Collaboration name and account's username must be provided");
        }

        if (!validString(args[collaborationName]) || !validString(args[username])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for searching a collaboration");
        }

        Collaboration collaboration = database.getCollaboration(args[collaborationName]);
        User userToAdd = database.getCurrentUser(args[username]);

        if (Validation.isObjNull(collaboration)) {
            return StatusMessages.ERROR.getMessage("Provided collaboration does not exist in the database");
        }

        if (Validation.isObjNull(database.getCurrentUser(args[username]))) {
            return StatusMessages.ERROR.getMessage("Provided user does not exist in the database");
        }

        if (!database.getCollaboration(args[collaborationName]).hasUser(currentUser)) {
            return StatusMessages.ERROR.getMessage("Provided user is not a part of this collaboration");
        }

        collaboration.addUser(userToAdd);
        database.updateCollaborationsDatabase(collaboration);

        return StatusMessages.SUCCESS.getMessage(
                String.format(
                        "User (%s) has been added to collaboration (%s)", args[username], args[collaborationName]));
    }

    private String deleteCollaborationUserCommand(SocketChannel clientChannel, String[] args) {
        //User
        currentUser =  getCurrentUser(clientChannel);

        final int collaborationName = 0;
        final int noArguments = 0;
        if (args.length == noArguments ) {
            return  StatusMessages.ERROR.getMessage("Name of a collaboration must be provided");
        }

        if (!validString(args[collaborationName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for deleting a collaboration");
        }

        try {
            database.deleteCollaboration(args[collaborationName], currentUser);
            database.updateUsersDatabase(currentUser);
        } catch (CollaborationNotExistException | CollaborationNotCreatorException e) {
            return e.getLocalizedMessage();
        }

        return StatusMessages
                .SUCCESS.getMessage(String.format("Collaboration (%s) has been deleted", args[collaborationName]));
    }

    private String assignTaskCollaborationsUserCommand(String[] args) {


        final int hasArguments = 3;
        final int collaborationName = 0;
        final int username = 1;
        final int taskName = 2;

        if (args.length != hasArguments ) {
            return  StatusMessages
                    .ERROR.getMessage("Collaboration name, account's username and task name must be provided");
        }

        if (!validString(args[collaborationName]) || !validString(args[username]) || !validString(args[taskName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for searching a collaboration");
        }

        Collaboration collaboration = database.getCollaboration(args[collaborationName]);
        User user = database.getCurrentUser(args[username]);

        if (Validation.isObjNull(collaboration)) {
            return StatusMessages.ERROR.getMessage("Provided collaboration does not exist in the database");
        }

        if (!collaboration.hasUser(user)) {
            return StatusMessages.ERROR.getMessage("Provided user is not a part of this collaboration");
        }

        List<String> taskArguments = new ArrayList<>(Arrays.asList(args).subList(taskName, args.length));

        try {
            collaboration.assignTask(
                    user, TaskFactory.createTask(Arrays.copyOf(
                            taskArguments.toArray(), taskArguments.size(), String[].class)));
            database.updateCollaborationsDatabase(collaboration);

        } catch (DatePeriodException | InvalidParametersException e) {
            return e.getLocalizedMessage();
        }

        return StatusMessages.SUCCESS.getMessage(
                String.format("Task has been assigned to user (%s) from collaboration (%s)",
                        args[username], args[collaborationName]));

    }

    private String listTasksCollaborationsUserCommand(String[] args) {
        final int noArguments = 0;
        final int collaborationName = 0;

        if (args.length == noArguments ) {
            return  StatusMessages.ERROR.getMessage("Name of a collaboration must be provided");
        }

        if (!validString(args[collaborationName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for listing tasks from collaboration");
        }

        try {
            return database.listTaskCollaboration(args[collaborationName]);
        } catch (CollaborationNotExistException e) {
            return e.getLocalizedMessage();
        }

    }

    private String listUsersCollaborationsUserCommand(String[] args) {

        final int noArguments = 0;
        final int collaborationName = 0;
        if (args.length == noArguments ) {
            return StatusMessages.ERROR.getMessage("Collaboration name must be provided ");
        }

        if (!validString(args[collaborationName])) {
            return StatusMessages.ERROR.getMessage("Invalid arguments for listing users from collaboration");
        }


        try {
            return database.listUsersCollaboration(args[collaborationName]);
        } catch (CollaborationNotExistException e) {
            return e.getLocalizedMessage();
        }

    }


}