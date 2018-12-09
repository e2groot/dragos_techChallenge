package com.dragos.test.service

import com.dragos.test.NotFoundException
import com.dragos.test.UnauthorizedException
import com.dragos.test.model.*
import com.dragos.test.repository.CustomerRepository
import com.dragos.test.repository.PrivilegesRepository
import com.dragos.test.toSingleOrThrow
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.time.Clock
import java.time.OffsetDateTime

open class CustomerService(
    private val customerRepository: CustomerRepository,
    private val privilegesRepository: PrivilegesRepository,
    private val clock: Clock
) {

    /**
     * Creates a new [Customer]. [AuthToken] must resolve to having [CAN_WRITE_CUSTOMER] privilege.
     *
     * @param model     customer data
     * @param authToken used to resolve the requester's privileges
     * @return the created customer [Single]
     */
    fun create(model: CustomerCreate, authToken: AuthToken): Single<Customer> =
            checkPrivilege(authToken, CAN_WRITE_CUSTOMER)
                    .andThen(
                            Single.defer {
                                customerRepository.insert(model, OffsetDateTime.now(clock))
                            }
                    )

    /**
     * Gets one [Customer] by ID. [AuthToken] must resolve to having [CAN_READ_CUSTOMER] privilege.
     *
     * @param id        the customer's ID
     * @param authToken used to resolve the requester's privileges
     * @return the customer [Single]
     * @throws NotFoundException if no customer found with ID
     */
    @Throws(NotFoundException::class)
    fun getOne(id: CustomerId, authToken: AuthToken): Single<Customer> =
        checkPrivilege(authToken, CAN_READ_CUSTOMER)
            .andThen(customerRepository.findOne(id))
            .toSingleOrThrow { NotFoundException("could not find customer with id $id") }

    /**
     * Gets many [Customer]s by [CustomerFindCriteria]. [AuthToken] must resolve to having [CAN_READ_CUSTOMER] privilege.
     *
     * @param id        the customer's ID
     * @param authToken used to resolve the requester's privileges
     * @return the customer [Single]
     * @throws NotFoundException if no customer found with ID
     */
    fun getMany(criteria: CustomerFindCriteria, authToken: AuthToken): Flowable<Customer> =
            checkPrivilege(authToken, CAN_READ_CUSTOMER)
            .andThen(customerRepository.find(criteria))

    /**
     * Updates one [Customer] by ID. [AuthToken] must resolve to having [CAN_WRITE_CUSTOMER] privilege.
     *
     * @param id        the customer's ID
     * @param authToken used to resolve the requester's privileges
     * @return the customer [Single]
     * @throws NotFoundException if no customer found with ID
     */
    @Throws(NotFoundException::class)
    fun update(id: CustomerId, update: CustomerUpdate, authToken: AuthToken): Single<Customer> =
        checkPrivilege(authToken, CAN_WRITE_CUSTOMER)
            .andThen(customerRepository.update(id, update))
            .toSingleOrThrow { NotFoundException("could not find customer with id $id") }

    /**
     * Deletes one [Customer] by ID. [AuthToken] must resolve to having [CAN_WRITE_CUSTOMER] privilege.
     *
     * @param id        the customer's ID
     * @param authToken used to resolve the requester's privileges
     * @return the customer [Single]
     * @throws NotFoundException if no customer found with ID
     */
    @Throws(NotFoundException::class)
    fun delete(id: CustomerId, authToken: AuthToken): Single<Customer> =
        checkPrivilege(authToken, CAN_WRITE_CUSTOMER)
            .andThen(customerRepository.delete(id))
            .toSingleOrThrow { NotFoundException("could not find customer with id $id") }

    fun checkPrivilege(authToken: AuthToken, requiredPrivilege: Privilege): Completable =
            privilegesRepository.getPrivileges(authToken)
                .filter { it == requiredPrivilege }
                .switchIfEmpty { subscriber -> subscriber.onError(UnauthorizedException("must have '$requiredPrivilege' privilege")) }
                .ignoreElements()
}