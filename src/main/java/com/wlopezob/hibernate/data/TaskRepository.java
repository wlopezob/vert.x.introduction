package com.wlopezob.hibernate.data;

import com.wlopezob.hibernate.model.Task;
import com.wlopezob.hibernate.model.TaskDTO;
import com.wlopezob.hibernate.model.TasksListDTO;
import io.vertx.core.Future;

import java.util.Optional;

public interface TaskRepository  {
  Future<TaskDTO> createTask(TaskDTO task);

  Future<TaskDTO> updateTask(TaskDTO task);

  Future<Void> removeTask(Integer id);

  Future<Optional<TaskDTO>> findTaskById(Integer id);

  Future<TasksListDTO> findTaskByUserId(Integer userId);
}
