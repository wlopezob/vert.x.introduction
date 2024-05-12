package com.wlopezob.hibernate.data;

import com.wlopezob.hibernate.model.Task;
import com.wlopezob.hibernate.model.TaskDTO;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

@ExtendWith(VertxExtension.class)
class HibernateConfigurationTest {

  TaskRepositoryImpl repository;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext context) {
    // 1. Create Properties with config data
    Properties hibernateProperties = new Properties();
    hibernateProperties.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/hdemodb");
    hibernateProperties.put("jakarta.persistence.jdbc.user", "hdemouser");
    hibernateProperties.put("jakarta.persistence.jdbc.password", "secret");
    hibernateProperties.put("hibernate.default_schema", "hdemodb");
    // schema generation
    hibernateProperties.put("jakarta.persistence.schema-generation.database.action", "create");
    hibernateProperties.put("jakarta.persistence.create-database-schemas", "true");
    //dialect
    hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    // log
//    hibernateProperties.put("hibernate.show_sql", "true");
//    hibernateProperties.put("hibernate.format_sql", "true");
//    hibernateProperties.put("hibernate.highlight_sql", "true");


    // 2. Create Hibernate Configuration
    Configuration hibernateConfiguration = new Configuration();
    hibernateConfiguration.setProperties(hibernateProperties);
    hibernateConfiguration.addAnnotatedClass(Task.class);

    // 3. Create ServiceRegistry
    ServiceRegistry serviceRegistry = new ReactiveServiceRegistryBuilder()
      .applySettings(hibernateConfiguration.getProperties()).build();

    // 4. Create SessionFactory
    Stage.SessionFactory sessionFactory = hibernateConfiguration
      .buildSessionFactory(serviceRegistry).unwrap(Stage.SessionFactory.class);

    repository = new TaskRepositoryImpl(sessionFactory);
    context.completeNow();
  }

  @Test
  void createTaskTest(Vertx vertx, VertxTestContext context) {
    TaskDTO taskDTO = new TaskDTO(null, 1, "My Task",
      false, LocalDateTime.now());
    Future<TaskDTO> future = repository.createTask(taskDTO);
    context.verify(() -> future.onFailure(context::failNow)
      .onSuccess(task -> {
        Assertions.assertNotNull(task);
        Assertions.assertNotNull(task.id());
        Assertions.assertEquals(1, task.userId());
        context.completeNow();
      }));
  }

  @Test
  void findTaskByIdDoesNotExistTest(Vertx vertx, VertxTestContext context) {
    Future<Optional<TaskDTO>> future = repository.findTaskById(100);
    context.verify(() -> future.onFailure(context::failNow)
      .onSuccess(task -> {
        Assertions.assertTrue(task.isEmpty());
        context.completeNow();
      }));
  }

  @Test
  void findTaskByIdTest(Vertx vertx, VertxTestContext context) {
    TaskDTO taskDTO = new TaskDTO(null, 1, "My Task",
      false, LocalDateTime.now());
    Future<TaskDTO> future = repository.createTask(taskDTO);
    context.verify(() -> future.onFailure(context::failNow)
      .compose(r -> repository.findTaskById(r.id()))
      .onSuccess(task -> {
        Assertions.assertTrue(task.isPresent());
        Assertions.assertEquals(taskDTO.userId(), task.get().userId());
        Assertions.assertEquals(taskDTO.content(), task.get().content());
        context.completeNow();
      }));
  }

  @Test
  void removeTaskTest(Vertx vertx, VertxTestContext context) {
    TaskDTO taskDTO = new TaskDTO(null, 1, "My Task",
      false, LocalDateTime.now());
    Future<TaskDTO> future = repository.createTask(taskDTO);
    context.verify(() -> future.onFailure(context::failNow)
      .compose(r -> repository.removeTask(r.id()))
      .onSuccess(v -> repository.findTaskById(taskDTO.id())
        .onSuccess(task -> {
          Assertions.assertTrue(task.isEmpty());
          context.completeNow();
        })));
  }

  @Test
  void updateTaskTest(Vertx vertx, VertxTestContext context) {
    TaskDTO taskDTO = new TaskDTO(null, 1, "My Task",
      false, LocalDateTime.now());
    context.verify(() -> repository.createTask(taskDTO)
      .compose(r -> {
        Assertions.assertNotNull(r.id());
        TaskDTO updatedTask = new TaskDTO(r.id(), taskDTO.userId(),
          "Updated content", true, r.createdAt());
        return repository.updateTask(updatedTask);
      })
      .compose(r -> {
        Assertions.assertTrue(r.completed());
        Assertions.assertEquals("Updated content", r.content());
        return repository.findTaskById(r.id());
      }).onFailure(context::failNow)
      .onSuccess(task -> {
        Assertions.assertTrue(task.isPresent());
        TaskDTO result = task.get();
        Assertions.assertTrue(result.completed());
        Assertions.assertEquals("Updated content", result.content());
        context.completeNow();
      }));
  }

  @Test
  void findTaskByUserTest(Vertx vertx, VertxTestContext context) {
    TaskDTO task1 = new TaskDTO(null, 1, "My task",
      false, LocalDateTime.now());
    TaskDTO task2 = new TaskDTO(null, 1, "My task",
      false, LocalDateTime.now());
    TaskDTO task3 = new TaskDTO(null, 2, "My task",
      false, LocalDateTime.now());
    CompositeFuture createTasks = CompositeFuture.join(
      repository.createTask(task1),
      repository.createTask(task2),
      repository.createTask(task3)
    );
    context.verify(() -> createTasks
      .compose(r -> repository.findTaskByUserId(1))
      .onFailure(context::failNow)
      .onSuccess(tasks -> {
        Assertions.assertTrue(tasks.tasks().size() >= 2);
        context.completeNow();
      }));
  }

  @Test
  void initializeHibernateWithCodeTest(Vertx vertx, VertxTestContext context) {
    // 1. Create Properties with config data
    Properties hibernateProperties = new Properties();
    hibernateProperties.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/hdemodb");
    hibernateProperties.put("jakarta.persistence.jdbc.user", "hdemouser");
    hibernateProperties.put("jakarta.persistence.jdbc.password", "secret");
    hibernateProperties.put("hibernate.default_schema", "hdemodb");
    // schema generation
    hibernateProperties.put("jakarta.persistence.schema-generation.database.action", "create");
    hibernateProperties.put("jakarta.persistence.create-database-schemas", "true");
    //dialect
    hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    // log
    hibernateProperties.put("hibernate.show_sql", "true");
    hibernateProperties.put("hibernate.format_sql", "true");
    hibernateProperties.put("hibernate.highlight_sql", "true");


    // 2. Create Hibernate Configuration
    Configuration hibernateConfiguration = new Configuration();
    hibernateConfiguration.setProperties(hibernateProperties);
    hibernateConfiguration.addAnnotatedClass(Task.class);

    // 3. Create ServiceRegistry
    ServiceRegistry serviceRegistry = new ReactiveServiceRegistryBuilder()
      .applySettings(hibernateConfiguration.getProperties()).build();

    // 4. Create SessionFactory
    Stage.SessionFactory sessionFactory = hibernateConfiguration
      .buildSessionFactory(serviceRegistry).unwrap(Stage.SessionFactory.class);

    // Do something with db
    Task task = new Task();
    task.setContent("Do something");
    task.setCompleted(false);
    task.setUserId(1);
    task.setCreatedAt(LocalDateTime.now());

    System.out.println("Task ID befor insertion is: " + task.getId());
    CompletionStage<Void> insertionResult = sessionFactory.withTransaction((session, tx) ->
      session.persist(task)
    );
    Future<Void> future = Future.fromCompletionStage(insertionResult);
    context.verify(() -> future.onFailure(context::failNow)
      .onSuccess(v -> {
        System.out.println("Task ID after insertion is: " + task.getId());
        context.completeNow();
      }));
  }
}
