package com.wlopezob.hibernate.web;

import com.wlopezob.hibernate.model.TaskDTO;
import com.wlopezob.hibernate.service.TaskService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
class WebVerticleTest {

  @Mock
  TaskService taskService;

  @InjectMocks
  WebVerticle webVerticle;

  WebClient webClient;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", 8080));
    webClient = WebClient.create(vertx);
    vertx.deployVerticle(webVerticle, options)
      .onSuccess(ok -> {
        System.out.println("WebVerticle deployed: " + ok);
        testContext.completeNow();
      })
      .onFailure(err -> {
        System.out.println("WebVerticle failed to deploy: " + err.getMessage());
        testContext.failNow(err);
      });
  }

  @Test
  void findTaskByIdExistTest(Vertx vertx, VertxTestContext testContext) {
    TaskDTO task = new TaskDTO(1, 1, "Task 1", false,
      null);

    Mockito.when(taskService.findTaskById(Mockito.anyInt()))
      .thenReturn(Future.succeededFuture(Optional.of(task)));

    testContext.verify(() -> {
      webClient.get(8080, "localhost", "/task/one/1")
        .send()
        .onSuccess(response -> {
          assertEquals(200, response.statusCode());
          TaskDTO taskDTO = response.bodyAsJsonObject().mapTo(TaskDTO.class);
          testContext.completeNow();
        })
        .onFailure(err -> {
          testContext.failNow(err);
        });
    });
  }

  @Test
  void findTaskByIdNotExistTest(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(taskService.findTaskById(Mockito.anyInt()))
      .thenReturn(Future.succeededFuture(Optional.empty()));

    testContext.verify(() -> {
      webClient.get(8080, "localhost", "/task/one/1")
        .send()
        .onSuccess(response -> {
          assertEquals(404, response.statusCode());
          testContext.completeNow();
        })
        .onFailure(err -> {
          testContext.failNow(err);
        });
    });
  }
}
