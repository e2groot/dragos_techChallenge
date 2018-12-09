package com.dragos.test.service

import com.dragos.test.model.AuthToken
import com.dragos.test.model.CAN_WRITE_CUSTOMER
import com.dragos.test.model.CustomerCreate
import com.dragos.test.repository.IPersistableCustomerRepository
import com.dragos.test.repository.PrivilegesRepository
import io.reactivex.Single
import java.io.File
import java.lang.StringBuilder
import java.time.Clock

class PersistableCustomerService (private val persistableCustomerRepository: IPersistableCustomerRepository,
                                  privilegesRepository: PrivilegesRepository,
                                  clock: Clock)
    : CustomerService (persistableCustomerRepository, privilegesRepository, clock) {
    fun persistToDatabase(model: CustomerCreate, authToken: AuthToken) : Single<Integer> {
        return checkPrivilege(authToken, CAN_WRITE_CUSTOMER)
                .andThen(
                        Single.defer {
                            persistableCustomerRepository.persistData(model)
                        }
                )
    }
}