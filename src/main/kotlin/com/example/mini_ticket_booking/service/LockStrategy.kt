package com.example.mini_ticket_booking.service

interface LockStrategy {
    fun apply(eventId: Long, quantity: Int)
}