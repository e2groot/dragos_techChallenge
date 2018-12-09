package com.dragos.test.api.v1

import com.dragos.test.UnauthenticatedException
import com.dragos.test.model.AuthToken
import com.dragos.test.model.CustomerCreate
import com.dragos.test.model.CustomerFindCriteria
import com.dragos.test.model.CustomerUpdate
import com.dragos.test.service.PersistableCustomerService
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.handling.Context
import ratpack.jackson.Jackson.fromJson
import ratpack.jackson.Jackson.json
import ratpack.registry.Registry
import ratpack.rx2.RxRatpack.promise
import ratpack.rx2.RxRatpack.promiseSingle
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicInteger

class ApiV1(registry: Registry) : Action<Chain> {

    companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModules(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        private val objectWriter = objectMapper.writer()

        private fun Context.getAuthToken() = validateAuthToken(request.headers["AuthToken"])

        private fun validateAuthToken(authToken: AuthToken): String {
            if (authToken.isNotEmpty())
                return authToken
            else
                throw UnauthenticatedException("must provide an AuthToken header")
        }
    }

    private val customerService = registry[PersistableCustomerService::class.java]
    private val ctr = AtomicInteger(1)

    override fun execute(chain: Chain) {
        chain.prefix("customer") { customerChain -> customerChain
            .path { context -> context
                .byMethod { spec -> spec
                    .post(::createCustomer)
                    .get(::getManyCustomers)
                }
            }
            .path("opp/persist") { context -> context
                    .byMethod { spec -> spec
                            .post(::persistToDatabase)
                    }
            }
            .path(":id") { context -> context
                .byMethod { spec -> spec
                    .get(::getOneCustomer)
                    .put(::updateCustomer)
                    .delete(::deleteCustomer)
                }
            }
        }
    }

    private fun createCustomer(context: Context) {
        context.parse(fromJson(CustomerCreate::class.java, objectMapper))
            .flatMap {
                create ->
                promiseSingle(customerService.create(create, context.getAuthToken()))
            }
            .then { created -> context.render(json(created, objectWriter)) }
    }

    private fun getOneCustomer(context: Context) {
        promiseSingle(customerService.getOne(context.allPathTokens["id"]!!.toLong(), context.getAuthToken()))
            .then { found -> context.render(json(found, objectWriter)) }
    }

    private fun getManyCustomers(context: Context) {
        val queryParams = context.request.queryParams
        promise(customerService.getMany(
            CustomerFindCriteria(
                nameStartsWith = queryParams["nameStartsWith"],
                createdBefore = queryParams["createdBefore"]?.let(OffsetDateTime::parse),
                createdAfter = queryParams["createdAfter"]?.let(OffsetDateTime::parse),
                lastLoggedInBefore = queryParams["lastLoggedInBefore"]?.let(OffsetDateTime::parse),
                lastLoggedInAfter = queryParams["lastLoggedInAfter"]?.let(OffsetDateTime::parse)
            ),
            context.getAuthToken()
        ))
            .then { found -> context.render(json(found, objectWriter)) }
    }

    private fun updateCustomer(context: Context) {
        context.parse(fromJson(CustomerUpdate::class.java, objectMapper))
            .flatMap { update -> promiseSingle(customerService.update(context.allPathTokens["id"]!!.toLong(), update, context.getAuthToken())) }
            .then { updated -> context.render(json(updated, objectWriter)) }
    }

    private fun deleteCustomer(context: Context) {
        promiseSingle(customerService.delete(context.allPathTokens["id"]!!.toLong(), context.getAuthToken()))
            .then { deleted -> context.render(json(deleted, objectWriter)) }
    }

    private fun persistToDatabase(context: Context) {
        context.parse(fromJson(CustomerCreate::class.java, objectMapper))
                .flatMap { create ->
                    promiseSingle(customerService.persistToFile(create, context.getAuthToken()))
                }
                .then{integer -> context.render(json(integer, objectWriter))}

    }
}