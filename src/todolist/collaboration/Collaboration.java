package todolist.collaboration;

import todolist.order.Task;
import todolist.user.User;
import todolist.validation.Validation;

import java.util.ArrayList;
import java.util.List;

public class Collaboration {

    private final String name;
    private final User creator;
    private List<User> users;
    private List<Assignee> assigneeTasks;
    public Collaboration(String name, User creator) {
        this.name = name;
        users = new ArrayList<>();
        assigneeTasks = new ArrayList<>();
        this.creator = creator;
        addUser(creator);
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setAssigneeTasks(List<Assignee> assigneeTasks) {
        this.assigneeTasks = assigneeTasks;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Assignee> getAssigneeTasks() {
        return assigneeTasks;
    }

    public String getName() {
        return name;
    }

    public User getCreator() {
        return creator;
    }

    public boolean hasUser(User user) {

        if (Validation.isObjNull(user)) {
            return false;
        }

        return users.stream().anyMatch(other -> other.equals(user));
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void assignTask(User user, Task task) {
        assigneeTasks.add(new Assignee(task, user));
    }


    @Override
    public boolean equals(Object obj) {
        Collaboration other = (Collaboration) obj;
        return ((other.name == null && this.name == null) || other.getName().equals(this.name));
    }
}
