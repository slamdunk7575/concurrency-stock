package me.test.stock.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockRepository {

    private RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Lettuce Redis 정리
    // setnx 명령어(set if not exist 줄임말: key/value 를 set 할때 기존에 값이 없을때만 set 수행)를 활용하여 분산락을 구현
    // Spin Lock 방식이므로 retry 로직을 개발자가 직접 구현해야함
    // 스핀락이란? Lock 을 사용하려는 쓰레드가 Lock 을 사용할 수 있는지 반복적으로 확인
    // MySQL Named Lock 과 거의 비슷함 (다른점: 세션 관리에 신경을 쓰지 않아도됨)
    // 장점:
    // 1.구현이 간단함
    // 단점:
    // 1.Spin Lock 방식으로 Redis 에 부하를 줄 수 있음
    //   그래서 Thread.sleep() 으로 Lock 획득 or 재시도 간에 텀을 주어야함

    public Boolean lock(Long key) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));
    }

    public Boolean unlock(Long key) {
        return redisTemplate.delete(generateKey(key));
    }

    private String generateKey(Long key) {
        return key.toString();
    }
}
