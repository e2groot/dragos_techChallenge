package com.dragos.test

class UnauthenticatedException(message: String, cause: Throwable? = null) : Exception(message, cause)

class UnauthorizedException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NotFoundException(message: String, cause: Throwable? = null) : Exception(message, cause)