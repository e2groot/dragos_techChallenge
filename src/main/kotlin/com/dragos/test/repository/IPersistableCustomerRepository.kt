package com.dragos.test.repository

import com.dragos.test.model.CustomerCreate
import io.reactivex.Single
import java.time.OffsetDateTime

interface IPersistableCustomerRepository : CustomerRepository {

    /**
     * This function persists the serialized Customer object into a file
     */
    fun persistData(model: CustomerCreate, now: OffsetDateTime) : Single<Long>
}