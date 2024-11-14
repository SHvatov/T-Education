package ru.tbank.edu.ab.sem10.refactor_tasks.todo_list.finish;

import java.util.List;

public interface TaskManager {
    void addTask(String task);
    void removeTask(int index);
    List<String> getTasks();
}
