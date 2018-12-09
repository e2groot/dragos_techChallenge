package com.dragos.test.repository.example

import com.dragos.test.model.Customer
import com.dragos.test.model.CustomerCreate
import com.dragos.test.repository.IPersistableCustomerRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Single
import java.io.File
import java.lang.StringBuilder
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicLong

class PersistableCustomerRepository(private val file: File, private val objectMapper: ObjectMapper)
    : IPersistableCustomerRepository, InMemoryCustomerRepository() {

    private var nextId = AtomicLong(1)

    /**
     * This function persists the serialized Customer object into a file
     */
    override fun persistData(model: CustomerCreate, now: OffsetDateTime): Single<Long> {
        val strBuilder = StringBuilder()
        val customer = Customer(
                id = nextId.getAndIncrement(),
                name = model.name,
                createdAt = now,
                lastLoggedInAt = now
        )
        strBuilder.append(objectMapper.writeValueAsString(customer)).append("\n")
        file.appendText(strBuilder.toString())
        return Single.just(customer.id)
    }
}