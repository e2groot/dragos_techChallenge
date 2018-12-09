package com.dragos.test.repository.example

import com.dragos.test.model.*
import com.dragos.test.repository.CustomerRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicLong

open class InMemoryCustomerRepository : CustomerRepository {
    private var nextId = AtomicLong(1)
    private val data = mutableMapOf<CustomerId, Customer>()

    /**
     * @see [CustomerRepository.insert]
     */
    override fun insert(create: CustomerCreate, now: OffsetDateTime): Single<Customer> {
        val customer = Customer(
            id = nextId.getAndIncrement(),
            name = create.name,
            createdAt = now,
            lastLoggedInAt = now
        )
        data[customer.id] = customer
        val json: String = jacksonObjectMapper().writeValueAsString(customer)
        return Single.just(customer)
    }

    /**
     * @see [CustomerRepository.findOne]
     */
    override fun findOne(id: CustomerId): Maybe<Customer> =
        Maybe.just(data[id])

    /**
     * @see [CustomerRepository.find]
     */
    override fun find(criteria: CustomerFindCriteria): Flowable<Customer> =
        data.values
            .filter { customer ->
                criteria.nameStartsWith?.let { nameStartsWith -> customer.name.startsWith(nameStartsWith) } != false &&
                criteria.createdBefore?.let { createdBefore -> customer.createdAt < createdBefore } != false &&
                criteria.createdAfter?.let { createdAfter -> customer.createdAt < createdAfter } != false &&
                criteria.lastLoggedInBefore?.let { lastLoggedInBefore -> customer.lastLoggedInAt < lastLoggedInBefore } != false &&
                criteria.lastLoggedInAfter?.let { lastLoggedInAfter -> customer.lastLoggedInAt < lastLoggedInAfter } != false
            }
            .let {
                founds ->
                Flowable.fromIterable(founds)
            }

    /**
     * @see [CustomerRepository.update]
     */
    override fun update(id: CustomerId, update: CustomerUpdate): Maybe<Customer> {
        val existing = data[id] ?: return Maybe.empty()
        val updated = existing.copy(
            name = update.name ?: existing.name
        )
        data[id] = updated
        return Maybe.just(data[id])
    }

    /**
     * @see [CustomerRepository.delete]
     */
    override fun delete(id: CustomerId): Maybe<Customer> =
        data.remove(id)?.let { deleted -> Maybe.just(deleted) } ?: Maybe.empty<Customer>()
}