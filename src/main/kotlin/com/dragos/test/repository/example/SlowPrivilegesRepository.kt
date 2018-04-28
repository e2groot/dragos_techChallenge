package com.dragos.test.repository.example

import com.dragos.test.UnauthenticatedException
import com.dragos.test.model.AuthToken
import com.dragos.test.model.Privilege
import com.dragos.test.repository.PrivilegesRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import io.reactivex.Flowable

/**
 * An example [PrivilegesRepository] implementation that simulates a slow call to an external service to resolve the
 * [Privilege]s for an [AuthToken]. In reality this is just loading data from an example YAML file at initialization
 * and then upon being called, waits a second before returning.
 */
class SlowPrivilegesRepository(exampleDataSourceYmlClasspath: String) : PrivilegesRepository {

    // loads privileges by auth token mappings from a YAML file on the class path
    private val data = YAMLMapper().readValue<Map<AuthToken, Set<Privilege>>>(
        javaClass.classLoader.getResourceAsStream(exampleDataSourceYmlClasspath),
        object : TypeReference<Map<AuthToken, Set<Privilege>>>() {}
    )

    /**
     * Waits a second then returns a [Flowable] of [Privilege]s that are associated with the provided [AuthToken].
     *
     * @see [PrivilegesRepository.getPrivileges]
     */
    @Throws(UnauthenticatedException::class)
    override fun getPrivileges(authToken: AuthToken): Flowable<Privilege> {
        // just here to simulate a slow external service
        Thread.sleep(1000)

        return data[authToken]?.let { Flowable.fromIterable(it) } ?: Flowable.error(UnauthenticatedException("invalid auth token"))
    }
}