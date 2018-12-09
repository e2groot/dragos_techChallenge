package com.dragos.test.service

import com.dragos.test.model.AuthToken
import com.dragos.test.model.CAN_WRITE_CUSTOMER
import com.dragos.test.model.CustomerCreate
import com.dragos.test.repository.IPersistableCustomerRepository
import com.dragos.test.repository.PrivilegesRepository
import io.reactivex.Single

import java.time.Clock
import java.time.OffsetDateTime

class PersistableCustomerService (private val persistableCustomerRepository: IPersistableCustomerRepository,
                                  privilegesRepository: PrivilegesRepository,
                                  clock: Clock)
    : CustomerService (persistableCustomerRepository, privilegesRepository, clock) {

    /**
     * This block performs a permissions check and performs record persistence following successful authentication
     */
    fun persistToFile(model: CustomerCreate, authToken: AuthToken) : Single<Long> {
        return checkPrivilege(authToken, CAN_WRITE_CUSTOMER)
                .andThen(
                        Single.defer {
                            persistableCustomerRepository.persistData(model, OffsetDateTime.now())
                        }
                )
    }
}