package com.dragos.test.repository

import com.dragos.test.model.CustomerCreate
import io.reactivex.Single
import java.time.OffsetDateTime

interface IPersistableCustomerRepository : CustomerRepository {

    fun persistData(model: CustomerCreate, now: OffsetDateTime) : Single<Long>
}