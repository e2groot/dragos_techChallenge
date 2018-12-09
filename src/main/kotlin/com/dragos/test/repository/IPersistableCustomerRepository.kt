package com.dragos.test.repository

import com.dragos.test.model.CustomerCreate
import io.reactivex.Single

interface IPersistableCustomerRepository : CustomerRepository {

    fun persistData(model: CustomerCreate) : Single<Integer>
}