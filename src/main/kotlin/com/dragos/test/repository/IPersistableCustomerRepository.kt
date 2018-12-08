package com.dragos.test.repository

import io.reactivex.Single

interface IPersistableCustomerRepository : CustomerRepository {

    fun persistData() : Single<Integer>
}