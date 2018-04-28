package com.dragos.test

import com.fasterxml.jackson.core.JsonProcessingException
import org.slf4j.LoggerFactory
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context
import ratpack.parse.NoSuchParserException

class ErrorHandler : ServerErrorHandler, ClientErrorHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ErrorHandler::class.java)
    }


    override fun error(context: Context, throwable: Throwable) {
        val code = when(throwable) {
            is IllegalArgumentException,
            is IllegalStateException,
            is JsonProcessingException -> {
                log.info("400: ${throwable.message}", if(log.isDebugEnabled) throwable else null)
                400
            }

            is UnauthenticatedException -> {
                log.info("401: ${throwable.message}", if(log.isDebugEnabled) throwable else null)
                401
            }

            is UnauthorizedException -> {
                log.warn("403: ${throwable.message}", if(log.isDebugEnabled) throwable else null)
                403
            }

            is NotFoundException -> {
                log.info("404: ${throwable.message}", if(log.isDebugEnabled) throwable else null)
                404
            }

            is NoSuchParserException -> {
                log.info("415: ${throwable.message}", if(log.isDebugEnabled) throwable else null)
                415
            }

            else -> {
                log.error("500: ${throwable.message}", throwable)
                500
            }
        }

        context.response.status(code)
        if(throwable.message != null)
            context.response.send(throwable.message)
        else
            context.response.send()
    }

    override fun error(context: Context, statusCode: Int) =
        context.response.status(statusCode).send()
}
