package com.wlopezob.hibernate.service;

import com.wlopezob.hibernate.auth.Principal;
import com.wlopezob.hibernate.model.TaskDTO;
import com.wlopezob.hibernate.model.TasksListDTO;
import io.vertx.core.Future;

import java.util.Optional;

public interface TaskService {
  Future<TaskDTO> createTask(TaskDTO task);

  Future<TaskDTO> updateTask(Principal principal, TaskDTO task);

  Future<Void> removeTask(Principal principal, Integer id);

  Future<Optional<TaskDTO>> findTaskById(Integer id);

  Future<TasksListDTO> findTaskByUserId(Integer userId);
}
