package com.example.mini_ticket_booking.service

import org.springframework.stereotype.Service

@Service
class TicketService {
    // test 용으로 파라미터 주입
    fun reserve(eventId: Long, quantity: Int, lockStrategy: LockStrategy) {
        lockStrategy.apply(eventId, quantity)
    }
}