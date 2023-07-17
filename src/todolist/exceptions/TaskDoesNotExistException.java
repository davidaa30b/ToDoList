package todolist.exceptions;

public class TaskDoesNotExistException extends Exception {
    public TaskDoesNotExistException(String message) {
        super(message);
    }
}
