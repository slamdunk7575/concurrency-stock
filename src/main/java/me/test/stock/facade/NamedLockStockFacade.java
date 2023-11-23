package me.test.stock.facade;

import me.test.stock.repository.LockRepository;
import me.test.stock.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    // 정리: Named Lock 사용
    // 이름을 가진 MetaData Lock
    // 이름을 가진 Lock 을 획득한후 해제할 때까지 다른 세션은 이 Lock 을 획득할 수 없음
    // 트랜직션이 종료될때 Lock 이 자동으로 해제되지 않기 때문에 별로 Lock 해제 작업을 해주거나 선점 시간이 지나야 Lock 이 풀림
    // MySQL 에서는 get_lock(), release_lock 명령어 사용
    // Pessimistic Lock 예:Stock 에 Lock 을 걸었다면 Named Lock 은 다른곳에 Lock 을 걸음
    // 주로 분산락을 구현할때 사용함
    // Pessimistic Lock 은 Timeout 을 구현하기 힘들지만 Named Lock 은 Timeout 을 쉽게 구현
    // 데이터 삽입시 정합성을 맞춰야 하는 경우 Named Lock 사용
    // 트랜잭션 종료시에 Lock 해제, 세션 관리를 잘해줘야함 (실제로 사용시 구현이 복잡할 수 있음)
    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decrease(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
