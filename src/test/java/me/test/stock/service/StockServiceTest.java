package me.test.stock.service;

import me.test.stock.domain.Stock;
import me.test.stock.repository.StockRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    void after() {
        stockRepository.deleteAll();
    }

    @DisplayName("재고 감소 로직 정상동작을 확인한다. (재고: 100개 -> 99개 감소)")
    @Test
    void stockSuccess() {
        // given
        stockService.decrease(1L, 1L);

        // when
        Stock stock = stockRepository.findById(1L).orElseThrow();

        // then
        assertThat(stock.getQuantity()).isEqualTo(99L);
    }

    @DisplayName("동시에 100개의 요청이 들어왔을때, 재고 감소 로직 문제점을 확인한다.")
    @Test
    void multiRequest() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 예상: 100개 - (1 * 100) = 0개
        // 결과: Race Condition 발생
        // quantity 리소스를 얻기 위해 두개 이상의 Thread 가 경쟁
        assertThat(stock.getQuantity()).isEqualTo(0);
    }
}
