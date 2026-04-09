## 🚀 동시성 제어 테스트 및 검증 (Concurrency Control Test)

### 1. 테스트 목적 🎯
티켓 예매 시스템에서 **100명의 사용자가 동시에 예매**를 시도할 때, 데이터 정합성이 깨지지 않고 정확히 수량이 감소하는지 검증합니다.

### 2. 테스트 환경 🛠️
* **Tools:** `JUnit 5`, `Spring Boot Test`
* **Concurrency Control:** `ExecutorService` (32개 스레드 풀), `CountDownLatch` (100개 작업 대기)
* **Database:** `H2` (In-memory 모드)

### 3. 검증 시나리오 🧪
동일한 티켓(Event)에 대해 두 가지 락(Lock) 전략을 비교 테스트했습니다.

| 전략 | 설명 | 기대 결과 |
| :--- | :--- | :--- |
| **비관적 락 (Pessimistic Lock)** | DB 수준에서 `SELECT ... FOR UPDATE`를 사용하여 데이터 점유 | 100개 요청 모두 순차 처리 성공 |
| **낙관적 락 (Optimistic Lock)** | JPA `@Version`을 활용해 충돌 감지 및 어플리케이션 재시도 | 충돌 시 재시도 로직을 통해 최종 정합성 유지 |

### 4. 핵심 코드 요약 💻

#### 🔄 낙관적 락 재시도 전략 (Spin Lock)
낙관적 락 실패 시 `ObjectOptimisticLockingFailureException`을 포착하여 일정 횟수만큼 재시도하도록 구현했습니다.

```kotlin
while (true) {
    try {
        transactionTemplate.execute {
            // 1. 낙관적 락 조회
            val event = eventRepository.findWithOptimisticLockById(eventId) ?: throw NoSuchElementException()
            event.decreaseTicket(quantity)
        }
        break;
    } catch (e: ObjectOptimisticLockingFailureException) {
        // 3. 충돌 시 잠시 쉬었다가 재시도 (Spin Lock 방식)
        println("Optimistic Locking Failure")
        if(count > retry) println("count: $count")
        if(count > retry) throw IllegalStateException("Optimistic Locking Failure")
        count++
        // sleep 값에 따라 속도 차이 발생, 적절하게 하는게 중요, 오래 걸리는 로직에 짧게 주면 메모리, cpu 고려 대상.
        Thread.sleep(10)
    }
}
```
