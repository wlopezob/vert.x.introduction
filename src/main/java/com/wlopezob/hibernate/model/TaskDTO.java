package com.wlopezob.hibernate.model;

import java.time.LocalDateTime;

public record TaskDTO(Integer id, Integer userId, String content, Boolean completed, LocalDateTime createdAt) {
}
