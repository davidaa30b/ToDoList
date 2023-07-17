package todolist.user;


import todolist.database.Storage;
import todolist.validation.Validation;
import todolist.messagesstatus.StatusMessages;

public class UserValidation extends Validation {

    public static boolean checkIfUserExistsInDatabase(Storage database, User user) {

        return database.getUsers()
                .stream()
                .anyMatch(other->other.getUsername()
                        .equals(user.getUsername()) ||
                        other.getPassword()
                                .equals(user.getPassword()));
    }

    public static String checkIfPassOrNameProvided(String[] args) {
        if (args.length != 2 ||  !validString(args[0]) || !validString(args[1])) {
            return StatusMessages.ERROR.getMessage("Password or username is not available");
        }

        return null;
    }
}
