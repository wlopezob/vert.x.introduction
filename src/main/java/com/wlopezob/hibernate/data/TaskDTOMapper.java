package com.wlopezob.hibernate.data;

import com.wlopezob.hibernate.model.Task;
import com.wlopezob.hibernate.model.TaskDTO;

import java.util.function.Function;

class TaskDTOMapper implements Function<Task, TaskDTO> {
  @Override
  public TaskDTO apply(Task task) {
    return new
      TaskDTO(task.getId(), task.getUserId(),
      task.getContent(), task.getCompleted(), task.getCreatedAt());
  }
}
