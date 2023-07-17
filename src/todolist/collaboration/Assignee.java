package todolist.collaboration;

import todolist.order.Task;
import todolist.user.User;

public record Assignee(Task task, User user) {
}
