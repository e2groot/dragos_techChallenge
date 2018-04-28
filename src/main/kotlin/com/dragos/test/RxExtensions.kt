package com.dragos.test

import io.reactivex.Maybe
import io.reactivex.Single

fun <T> Maybe<T>.toSingleOrThrow(createThrowable: () -> Throwable): Single<T> =
    switchIfEmpty { Maybe.error<T>(createThrowable()) }.toSingle()