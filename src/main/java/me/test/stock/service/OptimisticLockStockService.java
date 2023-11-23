package me.test.stock.service;

import me.test.stock.domain.Stock;
import me.test.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    public OptimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 정리: Optimistic Lock
    // 장점:
    // 1.별도의 Lock 을 잡지 않으므로 Pessimistic Lock 보다 성능상 이점이 있음
    // 단점:
    // 1.update 가 실패했을때 재시도 하는 로직을 개발자가 직접 작성해줘야함
    // 2.충돌이 빈번하게 일어날 것이라고 예상된다면 -> Pessimistic Lock
    //   충돌이 빈번하게 일어나지 않을 것이라고 예상된다면 -> Optimistic Lock
    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);
        stock.decrease(quantity);
    }
}
