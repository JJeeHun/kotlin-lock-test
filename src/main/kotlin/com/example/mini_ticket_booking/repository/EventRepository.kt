package com.example.mini_ticket_booking.repository

import com.example.mini_ticket_booking.entity.Event
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.CrudRepository

interface EventRepository: CrudRepository<Event, Long> {
    // 메서드 이름 뒤에 식별자를 붙여 기본 findById와 구분
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithPessimisticLockById(id: Long): Event?

    @Lock(LockModeType.OPTIMISTIC)
    fun findWithOptimisticLockById(id: Long): Event?
}