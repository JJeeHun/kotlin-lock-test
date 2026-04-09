package com.example.mini_ticket_booking.service

import com.example.mini_ticket_booking.repository.EventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PessimisticLockStrategy(
    private val eventRepository: EventRepository,
) : LockStrategy {

    @Transactional
    override fun apply(eventId: Long, quantity: Int) {
        val event =
            eventRepository.findWithPessimisticLockById(eventId) ?: throw NoSuchElementException("이벤트를 찾을 수 없습니다.")
        event.decreaseTicket(quantity)
    }
}