package com.example.mini_ticket_booking.service

import com.example.mini_ticket_booking.entity.Event
import com.example.mini_ticket_booking.repository.EventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TicketServiceTest(
    @Autowired val ticketService: TicketService,
    @Autowired val eventRepository: EventRepository,
    @Autowired val pressMeticLockStrategy: PessimisticLockStrategy,
    @Autowired val optimisticLockStrategy: OptimisticLockStrategy,
) {
    val threadCount = 1000

    @Test
    @Order(2)
    fun `DB Lock - 100명이 동시에 예매하면 남은 수량은 0이어야 한다`() {
        // 1. 초기 데이터 세팅
        val event = eventRepository.save(Event(name = "콘서트", availableTickets = 100))
        val executorService = Executors.newFixedThreadPool(32) // 스레드 풀 생성
        val latch = CountDownLatch(threadCount) // 모든 스레드가 끝날 때까지 기다리기 위한 장치

        // 2. 실행
        repeat(threadCount) {
            executorService.submit {
                try {
                    // 여기에 서비스 호출 로직이 들어갑니다.
                    ticketService.reserve(event.id, quantity = 1, lockStrategy = pressMeticLockStrategy)
                } finally {
                    latch.countDown() // 작업 완료 신호
                }
            }
        }

        latch.await() // 모든 스레드가 종료될 때까지 대기

        // 3. 검증
        val updatedEvent = eventRepository.findById(event.id).get()
        assertEquals(0, updatedEvent.availableTickets)
    }

    @Test
    @Order(1)
    fun `JPA Lock - 100명이 동시에 예매하면 남은 수량은 0이어야 한다`() {
        // 1. 초기 데이터 세팅
        val event = eventRepository.save(Event(name = "콘서트", availableTickets = 100))
        val executorService = Executors.newFixedThreadPool(32) // 스레드 풀 생성
        val latch = CountDownLatch(threadCount) // 모든 스레드가 끝날 때까지 기다리기 위한 장치

        // 2. 실행
        repeat(threadCount) {
            executorService.submit {
                try {
                    // 여기에 서비스 호출 로직이 들어갑니다.
                     ticketService.reserve(event.id, quantity = 1, lockStrategy = optimisticLockStrategy)
                } finally {
                    latch.countDown() // 작업 완료 신호
                }
            }
        }

        latch.await() // 모든 스레드가 종료될 때까지 대기

        // 3. 검증
        val updatedEvent = eventRepository.findById(event.id).get()
        assertEquals(0, updatedEvent.availableTickets)
    }
}