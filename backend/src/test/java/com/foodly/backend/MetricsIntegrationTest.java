package com.foodly.backend;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Тестування метрик моніторингу (Counter та Gauge)")
class MetricsIntegrationTest {

    private MeterRegistry meterRegistry;
    private AtomicInteger activeSessions; // Об'єкт для відстеження значення Gauge

    @BeforeEach
    void setUp() {
        // Використовуємо ізольований реєстр метрик для кожного тесту
        meterRegistry = new SimpleMeterRegistry();
        activeSessions = new AtomicInteger(0);
    }

    // =========================================================================
    // БЛОК 1: ТЕСТИ ЛІЧИЛЬНИКА (COUNTER TESTS)
    // Умова: Після N запитів лічильник запитів дорівнює N
    // =========================================================================

    @Test
    @DisplayName("1. Counter: Одне замовлення збільшує лічильник рівно на 1")
    void testCounterIncrementByOne() {
        Counter orderCounter = meterRegistry.counter("foodly.orders.total");

        // Імітуємо 1 запит на створення замовлення
        orderCounter.increment();

        assertEquals(1.0, orderCounter.count(), "Лічильник має дорівнювати 1 після одного запиту");
    }

    @Test
    @DisplayName("2. Counter: Серія з N запитів (N=5) встановлює значення лічильника в N")
    void testCounterIncrementByN() {
        Counter orderCounter = meterRegistry.counter("foodly.orders.total");
        int n = 5;

        // Імітуємо N запитів до ендпоінту
        for (int i = 0; i < n; i++) {
            orderCounter.increment();
        }

        assertEquals(5.0, orderCounter.count(), "Після 5 запитів лічильник повинен показувати рівно 5.0");
    }

    @Test
    @DisplayName("3. Counter: Велика кількість запитів (N=100) не викликає збоїв чи втрати точності")
    void testCounterLargeIncrement() {
        Counter apiRequestsCounter = meterRegistry.counter("foodly.api.requests");
        int n = 100;

        for (int i = 0; i < n; i++) {
            apiRequestsCounter.increment();
        }

        assertEquals(100.0, apiRequestsCounter.count(), "Лічильник повинен чітко зафіксувати 100 запитів");
    }

    // =========================================================================
    // БЛОК 2: ТЕСТИ ДАТЧИКА (GAUGE TESTS)
    // Умова: Після відкриття сесії значення зростає, після закриття - спадає
    // =========================================================================

    @Test
    @DisplayName("4. Gauge: Значення зростає на +1 при відкритті нової сесії користувача")
    void testGaugeIncreasesOnSessionOpen() {
        // Реєструємо Gauge, який стежить за значенням activeSessions
        Gauge.builder("foodly.users.active.sessions", activeSessions, AtomicInteger::get)
                .register(meterRegistry);

        // Користувач залогінився (відкриття сесії)
        activeSessions.incrementAndGet();

        double gaugeValue = meterRegistry.get("foodly.users.active.sessions").gauge().value();
        assertEquals(1.0, gaugeValue, "Значення Gauge має зрости до 1.0 після відкриття сесії");
    }

    @Test
    @DisplayName("5. Gauge: Значення зменшується на -1 після закриття сесії (Log Out)")
    void testGaugeDecreasesOnSessionClose() {
        Gauge.builder("foodly.users.active.sessions", activeSessions, AtomicInteger::get)
                .register(meterRegistry);

        // Імітуємо ситуацію: зайшло 2 користувача, потім 1 вийшов
        activeSessions.incrementAndGet(); // +1
        activeSessions.incrementAndGet(); // +1 (разом 2)

        // Користувач натиснув вихід / спрацював таймаут безпеки
        activeSessions.decrementAndGet(); // -1

        double gaugeValue = meterRegistry.get("foodly.users.active.sessions").gauge().value();
        assertEquals(1.0, gaugeValue, "Значення Gauge має впасти до 1.0 після закриття однієї з сесій");
    }

    @Test
    @DisplayName("6. Gauge: Значення повертається в 0, якщо всі відкриті сесії закриваються")
    void testGaugeReturnsToZero() {
        Gauge.builder("foodly.users.active.sessions", activeSessions, AtomicInteger::get)
                .register(meterRegistry);

        // Сесія відкрилась
        activeSessions.incrementAndGet();
        // Сесія закрилась
        activeSessions.decrementAndGet();

        double gaugeValue = meterRegistry.get("foodly.users.active.sessions").gauge().value();
        assertEquals(0.0, gaugeValue, "Gauge повинен показувати 0, якщо немає жодної активної сесії");
    }
}