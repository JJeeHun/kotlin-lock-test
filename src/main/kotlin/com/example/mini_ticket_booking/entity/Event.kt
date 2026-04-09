package com.example.mini_ticket_booking.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version

@Entity
class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    var availableTickets: Int,

    @Version
    var version: Long = 0, // 낙관적 락(Optimistic Lock)을 위한 버전 관리
) {
    fun decreaseTicket(quantity: Int) {
        if(availableTickets < quantity) {
            throw IllegalArgumentException("잔여 티켓이 부족합니다.")
        }
        availableTickets -= quantity
    }
}