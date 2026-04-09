package com.example.mini_ticket_booking.service

import com.example.mini_ticket_booking.repository.EventRepository
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class OptimisticLockStrategy(
    private val eventRepository: EventRepository,
    private val transactionTemplate: TransactionTemplate,
): LockStrategy {
    val retry = 20

    override fun apply(eventId: Long, quantity: Int) {
        var count = 1;
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
    }
}