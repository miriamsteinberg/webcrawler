package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Service
public class RedisDataService {
 private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public RedisDataService(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<Boolean> set(String key, String value) {
 ReactiveValueOperations<String, String> reactiveValueOps = reactiveRedisTemplate.opsForValue();
 return reactiveValueOps.set(key, value);
 }

 public Mono<Boolean> exits(String key) {
 return reactiveRedisTemplate.hasKey(key);
 }

 public Mono<String> get(String key) {
 ReactiveValueOperations<String, String> reactiveValueOps = reactiveRedisTemplate.opsForValue();
 return reactiveValueOps.get(key);
 }
}
