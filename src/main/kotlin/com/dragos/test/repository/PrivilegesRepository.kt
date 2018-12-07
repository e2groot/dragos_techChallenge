package com.dragos.test.repository

import com.dragos.test.UnauthenticatedException
import com.dragos.test.model.AuthToken
import com.dragos.test.model.Privilege
import io.reactivex.Flowable
import io.reactivex.Single

interface PrivilegesRepository {

    /**
     * Gets a [Flowable] of [Privilege]s that are associated with the provided [AuthToken].
     *
     * @param authToken the auth token to get privileges for
     * @return the privileges associated with the provided auth token
     * @throws UnauthenticatedException if the auth token is unknown
     */
    @Throws(UnauthenticatedException::class)
    fun getPrivileges(authToken: AuthToken): Flowable<Privilege>
}