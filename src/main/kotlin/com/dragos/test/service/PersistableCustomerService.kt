package com.dragos.test.service

import com.dragos.test.UnauthenticatedException
import com.dragos.test.model.AuthToken
import com.dragos.test.model.CAN_WRITE_CUSTOMER
import com.dragos.test.repository.IPersistableCustomerRepository
import com.dragos.test.repository.PrivilegesRepository
import io.reactivex.Single
import java.time.Clock

class PersistableCustomerService (private val persistableCustomerRepository: IPersistableCustomerRepository,
                                  private val privilegesRepository: PrivilegesRepository,
                                  private val clock: Clock)
    : CustomerService (persistableCustomerRepository, privilegesRepository, clock) {
    fun persistToDatabase(authToken: AuthToken) : Single<Integer> {
        return checkPrivilege(authToken, CAN_WRITE_CUSTOMER)
                .andThen(
                        Single.defer {
                            persistableCustomerRepository.persistData()
                        }
                )
    }
}