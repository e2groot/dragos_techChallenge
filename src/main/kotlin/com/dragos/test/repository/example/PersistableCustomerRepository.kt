package com.dragos.test.repository.example

import com.dragos.test.repository.IPersistableCustomerRepository
import io.reactivex.Single

class PersistableCustomerRepository: IPersistableCustomerRepository, InMemoryCustomerRepository() {

    override fun persistData(): Single<Integer> {
        System.out.println("You can reach me!!!")
        return Single.just(Integer(1))
    }
}