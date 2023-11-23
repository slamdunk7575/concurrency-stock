package me.test.stock.service;

import me.test.stock.domain.Stock;
import me.test.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessimisticLockStockService {

    private final StockRepository stockRepository;

    public PessimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 정리: Pessimistic Lock 사용
    // select stock0_.id as id1_0_, stock0_.product_id as product_2_0_, stock0_.quantity as quantity3_0_, stock0_.version as version4_0_ from stock stock0_ where stock0_.id=? for update
    // 쿼리에 for update 이부분이 Lock 을 걸고 데이터를 가져오는 부분
    // 장점:
    // 1.충돌이 빈번하게 일어난다면 Pessimistic Lock 이 Optimistic Lock 보다 성능이 좋을 수 있음
    // 2.Lock 을 통해 업데이트를 하기 때문에 데이터 정합성이 보장됨
    // 단점:
    // 1.별도의 Lock 을 잡기 때문에 성능 감소가 있을 수 있음
    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);
        stock.decrease(quantity);
    }
}
