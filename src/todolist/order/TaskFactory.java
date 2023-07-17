package todolist.order;

import todolist.exceptions.DatePeriodException;
import todolist.exceptions.InvalidParametersException;
import todolist.validation.Validation;
import todolist.messagesstatus.StatusMessages;


import static todolist.validation.Validation.validString;

public class TaskFactory {
    private static final int ZERO_PARAMETER = 0;
    private static final int ONE_PARAMETER = 1;
    private static final int TWO_PARAMETER = 2;
    private static final int THREE_PARAMETER = 3;

    private static final int NAME_INDEX = 0;
    private static final int DATE_INDEX = 1;
    private static final int DUE_DATE_INDEX = 2;
    private static final int BEG_DES_INDEX = 3; //beginning description index
    private static final String SPACE = " ";

    public static Task createTask(String[] args) throws DatePeriodException, InvalidParametersException {
        return switch (args.length) {
            case ZERO_PARAMETER -> null;
            case ONE_PARAMETER -> fillName(args[NAME_INDEX]);
            case TWO_PARAMETER -> fillDate(args);
            case THREE_PARAMETER -> fillDeadline(args);
            default -> fillDescription(args);
        };
    }

    private static Task fillName(String name) throws InvalidParametersException {
        if (!validString(name)) {
            throw new InvalidParametersException(
                    StatusMessages.ERROR.getMessage("Invalid arguments for creating a task")
            );
        }

        return Task.builder(name)
                .build();
    }
    private static Task fillDate(String[] args) throws InvalidParametersException, DatePeriodException {
        fillName(args[NAME_INDEX]);
        Validation.getDateInDateTime(args[DATE_INDEX]);

        if (!Validation.checkIfTimeCurrent(args[DATE_INDEX])) {
            throw new DatePeriodException(
                    StatusMessages.ERROR.getMessage(
                            "Can not have a task for the past"));
        }

        return Task.builder(args[NAME_INDEX])
                .setDate(args[DATE_INDEX])
                .build();
    }



    private static Task fillDeadline(String[] args) throws DatePeriodException, InvalidParametersException {

        fillDate(args);

        if (Validation.getDateInDateTime(args[DATE_INDEX])
                        .isAfter(Validation.getDateInDateTime(args[DUE_DATE_INDEX]))) {
            throw new DatePeriodException(
                    StatusMessages.ERROR.getMessage(
                            "Can not begin a task after its deadline"));
        }

        return Task.builder(args[NAME_INDEX])
                .setDate(args[DATE_INDEX])
                .setDueDate(args[DUE_DATE_INDEX])
                .build();
    }

    private static Task fillDescription(String[] args) throws DatePeriodException, InvalidParametersException {

        fillDeadline(args);

        StringBuilder description = new StringBuilder();

        for (int i = BEG_DES_INDEX; i < args.length; i++) {
            description.append(args[i]);
            description.append(SPACE);
        }

        return Task.builder(args[NAME_INDEX])
                .setDate(args[DATE_INDEX])
                .setDueDate(args[DUE_DATE_INDEX])
                .setDescription(description.toString())
                .build();
    }

}
