package todolist.order;

import todolist.validation.Validation;


public class Task {
    // required parameters
    private final String name;
    // optional parameters

    private String date;
    private String dueDate;
    private String description;

    private boolean isCompleted;

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }



    public String getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public static TaskBuilder builder(String name) {
        return new TaskBuilder(name);
    }

    public String toString() {

        StringBuilder result = new StringBuilder();
        result.append(System.lineSeparator());
        result.append("Name : ").append(this.name).append(System.lineSeparator());

        if (!Validation.isObjNull(this.date)) {
            result.append("Date : ").append(this.date).append(System.lineSeparator());
        }

        if (!Validation.isObjNull(this.dueDate)) {
            result.append("Due Date : ").append(this.dueDate).append(System.lineSeparator());
        }

        if (!Validation.isObjNull(this.description)) {
            result.append("Description : ").append(this.description).append(System.lineSeparator());
        }

        return result.toString();
    }


    private Task(TaskBuilder builder) {
        this.name = builder.name;
        this.date = builder.date;
        this.dueDate = builder.dueDate;
        this.description = builder.description;
        this.isCompleted = builder.isCompleted;
    }

    // Builder Class
    public static class TaskBuilder {

        // required parameters
        private String name;
        // optional parameters
        private String date;
        private String dueDate;
        private String description;

        private boolean isCompleted;

        private TaskBuilder(String name) {
            this.name = name;
        }

        public TaskBuilder setDate(String date) {
            this.date = date;
            return this;
        }

        public TaskBuilder setDueDate(String dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaskBuilder setDescription(String description) {
            this.description = description;
            return this;
        }


        public Task build() {
            return new Task(this);
        }

    }
}
