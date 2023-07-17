package todolist.exceptions;

public class UserAlreadyLoggedException extends Exception {
    public UserAlreadyLoggedException(String message) {
        super(message);
    }
}
