package com.wlopezob.hibernate.service;

import com.wlopezob.hibernate.auth.Principal;
import com.wlopezob.hibernate.data.TaskRepository;
import com.wlopezob.hibernate.model.TaskDTO;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

  @Mock
  TaskRepository taskRepository;

  @InjectMocks
  TaskServiceImpl taskService;

//  @Test
//  void updateTaskIsOnwerTest(Vertx vertx, VertxTestContext context) {
//    // Given
//    TaskDTO taskDTO = new TaskDTO(1, 1, "Description", false, LocalDateTime.now());
//    // When
//    Mockito.when(taskRepository.findTaskById(1))
//      .thenReturn(Future.succeededFuture(Optional.of(taskDTO)));
//    Mockito.when(taskRepository.updateTask(Mockito.any(TaskDTO.class)))
//      .thenReturn(Future.succeededFuture(taskDTO));
//    Principal principal = new Principal(1);
//    // Then
//    context.verify(() -> {
//      taskService.updateTask(principal, taskDTO)
//        .onSuccess(result -> {})
//        .onFailure(context::failNow);
//    });
//  }
}
