package todolist.database;

import todolist.order.Task;

import java.util.HashSet;
import java.util.Set;

public class Inbox {
    Set<Task> tasks;

    public Set<Task> getTasks() {
        return tasks;
    }

    public static Inbox getInstance() {
        return instance;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    private static Inbox instance = new Inbox();
    private Inbox() {
        tasks = new HashSet<>();
    }

}
