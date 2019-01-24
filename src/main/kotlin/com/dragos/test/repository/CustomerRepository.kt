package com.dragos.test.repository

import com.dragos.test.model.*
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import java.time.OffsetDateTime

interface CustomerRepository {

    /**
     * Inserts a new [Customer].
     *
     * @param create customer data
     * @param now    the current time
     * @return the created customer [Single]
     */
    fun insert(create: CustomerCreate, now: OffsetDateTime): Customer

    /**
     * Finds one [Customer] by ID.
     *
     * @param id the customer's ID
     * @return the customer [Maybe] (empty if not found)
     */
    fun findOne(id: CustomerId): Maybe<Customer>

    /**
     * Finds [Customer]s by [CustomerFindCriteria].
     *
     * @param criteria the criteria to find customers that match
     * @return a [Flowable] of customers (empty if none match the criteria)
     */
    fun find(criteria: CustomerFindCriteria): Flowable<Customer>

    /**
     * Updates one [Customer] by ID with [CustomerUpdate].
     *
     * @param id     the customer's ID
     * @param update the update fields
     * @return the updated customer [Maybe] (empty if not found)
     */
    fun update(id: CustomerId, update: CustomerUpdate): Maybe<Customer>

    /**
     * Deletes one [Customer] by ID.
     *
     * @param id the customer's ID
     * @return the deleted customer [Maybe] (empty if not found)
     */
    fun delete(id: CustomerId): Maybe<Customer>
}