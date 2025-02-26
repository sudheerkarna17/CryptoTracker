package com.plcoding.cryptotracker.crypto.domain

import java.time.ZonedDateTime

data class CoinPrice(
    val dateTime: ZonedDateTime,
    val priceUsd: Double
)
