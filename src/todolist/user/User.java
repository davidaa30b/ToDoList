package todolist.user;
import todolist.database.Inbox;
import todolist.exceptions.InvalidParametersException;
import todolist.exceptions.TaskAlreadyExistsException;
import todolist.exceptions.TaskDoesNotExistException;
import todolist.order.Task;
import todolist.validation.Validation;
import todolist.messagesstatus.StatusMessages;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class User {

    private Inbox inbox = Inbox.getInstance();
    private Set<Task> tasks;
    private String username;
    private String password;




    public User(String username, String password) {
        this.username = username;
        this.password = password;
        tasks = new HashSet<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public Inbox getInbox() {
        return inbox;
    }
    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }




    public void addTask(Task task) throws TaskAlreadyExistsException {

        if (!Validation.isObjNull(getTask(task.getName()))) {
            throw new TaskAlreadyExistsException(
                    StatusMessages.ERROR.getMessage(String.format("The task (%s) already exists", task.getName())));
        }

        if (Validation.isObjNull(task.getDate())) {
            inbox.addTask(task);
        }
        else {
            tasks.add(task);
        }

    }

    public void updateTask(Task task)
            throws TaskAlreadyExistsException, TaskDoesNotExistException {

        deleteTask(task.getName());

        addTask(task);
    }

    public void deleteTask(String name) throws TaskDoesNotExistException {

        if (Validation.isObjNull(getTask(name))) {

            throw new TaskDoesNotExistException(
                    StatusMessages.ERROR.getMessage("Task you are searching for does not exists"));
        }

        tasks.remove(getTask(name));
        inbox.getTasks().remove(getTask(name));
    }

    public Task getTask(String name) {
        Task taskToGet = tasks.stream()
                .filter(task -> task.getName().equals(name))
                .findAny()
                .orElse(null);

        if (Validation.isObjNull(taskToGet)) {
            taskToGet = inbox.getTasks().stream()
                    .filter(task -> task.getName().equals(name))
                    .findAny()
                    .orElse(null);
        }

        return taskToGet;
    }

    public String listOneTask(String name) throws TaskDoesNotExistException {
        Task taskToList = getTask(name);
        if (Validation.isObjNull(taskToList)) {
            throw new TaskDoesNotExistException(
                    StatusMessages.ERROR.getMessage("Task you are searching for does not exists"));
        }

        return taskToList.toString();

    }

    public String listTasks() {
        if ( tasks.size() == 0 && inbox.getTasks().size() == 0) {
            return "The current user has no tasks !";
        }

        StringBuilder result = new StringBuilder() ;

        result.append(System.lineSeparator());
        result.append("Tasks regular : ");
        result.append(System.lineSeparator());

        for (var task : tasks) {
            result.append(task.toString());
            result.append(" ");
        }

        result.append(System.lineSeparator());
        result.append("Tasks inbox : ");
        result.append(System.lineSeparator());

        for (var task : inbox.getTasks()) {
            result.append(task.toString());
            result.append(" ");
        }

        return result.toString();
    }

    public String listTaskByCompletion(boolean parameter) {
        if ( tasks.size() == 0 && inbox.getTasks().size() == 0) {
            return "The current user has no tasks !";
        }

        StringBuilder result = new StringBuilder() ;

        result.append(System.lineSeparator());
        result.append("Tasks regular : ");
        result.append(System.lineSeparator());

        for (var task : tasks) {
            if (task.getIsCompleted() == parameter) {
                result.append(task);
                result.append(" ");
            }
        }

        result.append(System.lineSeparator());
        result.append("Tasks inbox : ");
        result.append(System.lineSeparator());

        for (var task : inbox.getTasks()) {
            if (task.getIsCompleted() == parameter) {
                result.append(task);
                result.append(" ");
            }
        }

        return result.toString();
    }

    public String listDashboard(LocalDate date) throws InvalidParametersException {
        if ( tasks.size() == 0 && inbox.getTasks().size() == 0) {
            return "The current user has no tasks !";
        }
        StringBuilder result = new StringBuilder();

        result.append(System.lineSeparator());
        result.append("Tasks regular : ");
        result.append(System.lineSeparator());
        int counter = 0;
        for (var task : tasks) {
            if (Validation.getDateInDateTime(task.getDate()).isBefore(date) ||
                    Validation.getDateInDateTime(task.getDate()).isEqual(date)) {

                result.append(task);
                result.append(" ");
            }
            counter++;
        }

        result.append(System.lineSeparator());
        result.append("Tasks inbox : ");
        result.append(System.lineSeparator());

        for (var task : inbox.getTasks()) {
            result.append(task);
            result.append(" ");

            counter++;
        }

        if (counter != 0) {
            return result.toString();
        }
        else {
            return "There are no tasks to be done today";
        }
    }


    public void finishTask(String name) throws TaskAlreadyExistsException, TaskDoesNotExistException {

        Task taskToFinish = getTask(name);
        deleteTask(name);
        taskToFinish.setIsCompleted(true);
        addTask(taskToFinish);
    }



    public String toString() {
        return "Name : " + this.username + " Password : " + this.password;
    }




    @Override
    public boolean equals(Object obj) {
        User other = (User) obj;
        return ((other.username == null && this.username == null) || other.username.equals(this.username)) &&
               ((other.password == null && this.password == null) || other.password.equals(this.password));
    }



}
