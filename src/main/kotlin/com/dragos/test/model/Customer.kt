package com.dragos.test.model

import java.time.OffsetDateTime

typealias CustomerId = Long

data class Customer(
    val id: CustomerId,
    val name: String,
    val createdAt: OffsetDateTime,
    val lastLoggedInAt: OffsetDateTime
)

data class CustomerCreate(
    val name: String
)

data class CustomerUpdate(
    val name: String?
)

data class CustomerFindCriteria(
    val nameStartsWith: String?,
    val createdBefore: OffsetDateTime?,
    val createdAfter: OffsetDateTime?,
    val lastLoggedInBefore: OffsetDateTime?,
    val lastLoggedInAfter: OffsetDateTime?
)