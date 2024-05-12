package com.wlopezob.hibernate.service;

import com.wlopezob.hibernate.auth.NotOwnerException;
import com.wlopezob.hibernate.auth.Principal;
import com.wlopezob.hibernate.data.TaskRepository;
import com.wlopezob.hibernate.model.TaskDTO;
import com.wlopezob.hibernate.model.TasksListDTO;
import io.vertx.core.Future;

import java.util.Optional;

public record TaskServiceImpl(TaskRepository taskRepository) implements TaskService {
  @Override
  public Future<TaskDTO> createTask(TaskDTO task) {
    return taskRepository.createTask(task);
  }

  @Override
  public Future<TaskDTO> updateTask(Principal principal, TaskDTO task) {
    int taskId = task.id();
    return taskRepository.findTaskById(taskId)
      .compose(taskDTO -> {
        if (taskDTO.isEmpty()) {
          return Future.failedFuture(new RuntimeException("Task not found"));
        }
        if (taskDTO.get().userId() != principal.userId()) {
          return Future.failedFuture(new NotOwnerException() );
        }
        return taskRepository.updateTask(task);
      });
  }

  @Override
  public Future<Void> removeTask(Principal principal, Integer id) {
    return null;
  }

  @Override
  public Future<Optional<TaskDTO>> findTaskById(Integer id) {
    return taskRepository.findTaskById(id);
  }

  @Override
  public Future<TasksListDTO> findTaskByUserId(Integer userId) {
    return null;
  }
}
