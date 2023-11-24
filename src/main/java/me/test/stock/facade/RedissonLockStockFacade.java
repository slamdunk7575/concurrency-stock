package me.test.stock.facade;

import me.test.stock.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockStockFacade {

    private RedissonClient redissonClient;

    private StockService stockService;

    public RedissonLockStockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    // Redisson Lock 정리
    // pub-sub 기반으로 Lock 구현
    // 채널을 하나 만들고 Lock 을 점유중인 쓰레드가 Lock 획득을 대기중인 쓰레드에게 해제를 알려주면
    // 안내를 받은 쓰레드가 Lock 획득 시도를 하는 방식
    // 장점:
    // 1.Lettuce 와 다르게 별도의 retry 로직을 작성하지 않아도됨
    // 2.Lock 이 해제되었을때 한번 or 몇번만 Lock 획득 시도를 하기 때문에 Redis 의 부하를 줄여줌
    // 3.Lock 획득/해제 관련 클래스를 라이브러리에서 제공해주어 별도의 Repository 작업안함
    // 4.로직 실행전후로 Lock 획득/해제를 위해 Facade 클래스 추가함
    // 단점:
    // 1.구현이 조금 복잡함
    // 2.별도의 Redisson 라이브러리 사용
    public void decrease(Long id, Long quantity) {
        RLock lock = redissonClient.getLock(id.toString());

        try {
            boolean available = lock.tryLock(15, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("Lock 획득 실패");
                return;
            }

            stockService.decrease(id, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
