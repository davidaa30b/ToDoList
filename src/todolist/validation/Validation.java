package todolist.validation;

import todolist.database.Database;
import todolist.exceptions.InvalidParametersException;
import todolist.user.User;
import todolist.messagesstatus.StatusMessages;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Validation {
    public static final String PATTERN = "d/M/yyyy";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(PATTERN);


    public static boolean checkIfTimeCurrent(String time) throws InvalidParametersException {

        return getDateInDateTime(time).isAfter(LocalDate.now()) ||  getDateInDateTime(time).isEqual(LocalDate.now()) ;
    }
    public static <T> boolean isObjNull(T obj) {
        return obj == null;
    }

    public static boolean validString(String str) {
        return !isObjNull(str) && !str.isBlank() && !str.isEmpty();
    }


    public static boolean checkIfUserCorrectInDatabase(Database database, User user) {

        return database.getUsers()
                .stream()
                .anyMatch(other->other.equals(user));

    }


    public static LocalDate getDateInDateTime(String date) throws InvalidParametersException {
        try {

            return LocalDate.parse(date, DATE_FORMAT);
        }
        catch (DateTimeParseException e) {
            throw new InvalidParametersException(
                    StatusMessages.ERROR.getMessage(
                            String.format("Date must in valid format : (%s)", PATTERN)));
        }
    }




}
