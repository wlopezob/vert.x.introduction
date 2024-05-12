package com.wlopezob.hibernate.data;

import com.wlopezob.hibernate.model.Task;
import com.wlopezob.hibernate.model.TaskDTO;
import com.wlopezob.hibernate.model.TasksListDTO;
import io.vertx.core.Future;
import jakarta.persistence.criteria.*;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public record TaskRepositoryImpl(Stage.SessionFactory sessionFactory)
  implements TaskRepository {
  @Override
  public Future<TaskDTO> createTask(TaskDTO task) {
    TaskEntityMapper taskEntityMapper = new TaskEntityMapper();
    Task taskEntity = taskEntityMapper.apply(task);
    CompletionStage<Void> result = sessionFactory
      .withTransaction((session, tx) ->
        session.persist(taskEntity));
    TaskDTOMapper taskDTOMapper = new TaskDTOMapper();
    return Future.fromCompletionStage(result).map(v ->
      taskDTOMapper.apply(taskEntity));
  }

  @Override
  public Future<TaskDTO> updateTask(TaskDTO task) {
    CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
    CriteriaUpdate<Task> criteriaUpdate = cb.createCriteriaUpdate(Task.class);
    Root<Task> root = criteriaUpdate.from(Task.class);
    Predicate predicate = cb.equal(root.get("id"), task.id());

    criteriaUpdate.set("content", task.content());
    criteriaUpdate.set("completed", task.completed());
    criteriaUpdate.where(predicate);

    CompletionStage<Integer> result = sessionFactory.withTransaction((session, tx) ->
      session.createQuery(criteriaUpdate).executeUpdate());
    Future<TaskDTO> taskDTOFuture = Future.fromCompletionStage(result)
      .map(r -> task);
    return taskDTOFuture;
  }

  @Override
  public Future<Void> removeTask(Integer id) {
    CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
    CriteriaDelete<Task> criteriaDelete = cb.createCriteriaDelete(Task.class);
    Root<Task> root = criteriaDelete.from(Task.class);
    Predicate predicate = cb.equal(root.get("id"), id); //id = {id}
    criteriaDelete.where(predicate);

    // DELETE FROM Tasks WHERE id = {id}
    CompletionStage<Integer> result = sessionFactory.withTransaction((session, tx) ->
      session.createQuery(criteriaDelete).executeUpdate());
    return Future.fromCompletionStage(result).compose(v -> Future.succeededFuture());
  }

  @Override
  public Future<Optional<TaskDTO>> findTaskById(Integer id) {
    TaskDTOMapper taskDTOMapper = new TaskDTOMapper();
    CompletionStage<Task> result = sessionFactory.withSession(
      session -> session.find(Task.class, id));
    return Future.fromCompletionStage(result)
      .map(Optional::ofNullable)
      .map(r -> r.map(taskDTOMapper));
  }

  @Override
  public Future<TasksListDTO> findTaskByUserId(Integer userId) {
    TaskDTOMapper taskDTOMapper = new TaskDTOMapper();
    CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
    CriteriaQuery<Task> criteriaQuery = cb.createQuery(Task.class);
    Root<Task> root = criteriaQuery.from(Task.class);
    Predicate predicate = cb.equal(root.get("userId"), userId);
    criteriaQuery.where(predicate);
    CompletionStage<List<Task>> result = sessionFactory.withTransaction(
      (session, t) -> session.createQuery(criteriaQuery).getResultList());
    return Future.fromCompletionStage(result)
      .map(tasks -> tasks.stream()
        .map(taskDTOMapper)
        .collect(Collectors.toList()))
      .map(TasksListDTO::new);
  }
}
