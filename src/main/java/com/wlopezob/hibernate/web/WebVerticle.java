package com.wlopezob.hibernate.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wlopezob.hibernate.data.TaskRepository;
import com.wlopezob.hibernate.data.TaskRepositoryImpl;
import com.wlopezob.hibernate.model.Task;
import com.wlopezob.hibernate.model.TaskDTO;
import com.wlopezob.hibernate.service.TaskService;
import com.wlopezob.hibernate.service.TaskServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.service.ServiceRegistry;

import java.util.Map;
import java.util.Properties;

public class WebVerticle extends AbstractVerticle {

  private final TaskService taskService;

  public WebVerticle(TaskService taskService) {
    this.taskService = taskService;
  }


  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/task").handler(ctx -> {
      JsonObject body = ctx.getBodyAsJson();
      TaskDTO task = body.mapTo(TaskDTO.class);
      taskService.createTask(task)
        .onSuccess(created -> {
          JsonObject response = JsonObject.mapFrom(created);
          ctx.response().setStatusCode(201).end(response.encode());
        })
        .onFailure(err -> {
          ctx.response().setStatusCode(500).end(err.getMessage());
        });
    });

    router.get("/task/one/:id").handler(ctx -> {
      Integer id = Integer.parseInt(ctx.pathParams().get("id"));
      taskService.findTaskById(id)
        .onSuccess(task -> {
          if (task.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            JsonObject body = JsonObject.mapFrom(mapper.convertValue(task.get(), Map.class));
            ctx.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json").end(body.encode());
          } else {
            ctx.response().setStatusCode(404).end();
          }
        })
        .onFailure(err -> {
          ctx.response().setStatusCode(500).end(err.getMessage());
        });
    });


    JsonObject config = config();
    Integer port = config.getInteger("http.port", 8888);
    server.requestHandler(router).listen(port)
      .onSuccess(ok -> {
        System.out.println("HTTP server started on port " + port);
        startPromise.complete();
      }).onFailure(err -> {
        System.out.println("HTTP server failed to start");
        startPromise.fail(err);
      });
  }

  public static void main(String[] args) {
    //1. Hibernate configuration
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

    Configuration hibernateConfiguration = new Configuration();
    hibernateConfiguration.setProperties(hibernateProperties);
    hibernateConfiguration.addAnnotatedClass(Task.class);

    //2. SessionFactory
    ServiceRegistry serviceRegistry = new ReactiveServiceRegistryBuilder()
      .applySettings(hibernateConfiguration.getProperties()).build();
    Stage.SessionFactory sessionFactory = hibernateConfiguration
      .buildSessionFactory(serviceRegistry).unwrap(Stage.SessionFactory.class);

    //3. TaskRepository
    TaskRepository taskRepository = new TaskRepositoryImpl(sessionFactory);

    //4. TaskService
    TaskService taskService = new TaskServiceImpl(taskRepository);

    Vertx vertx = Vertx.vertx();
    WebVerticle webVerticle = new WebVerticle(taskService);
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", 8080));
    vertx.deployVerticle(webVerticle, options)
      .onSuccess(ok -> System.out.println("WebVerticle deployed: " + ok))
      .onFailure(Throwable::printStackTrace);
  }
}
