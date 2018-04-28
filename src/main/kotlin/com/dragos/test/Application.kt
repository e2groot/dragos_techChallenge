package com.dragos.test

import com.dragos.test.api.v1.ApiV1
import com.dragos.test.repository.CustomerRepository
import com.dragos.test.repository.PrivilegesRepository
import com.dragos.test.repository.example.InMemoryCustomerRepository
import com.dragos.test.repository.example.SlowPrivilegesRepository
import com.dragos.test.service.CustomerService
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.guice.Guice
import ratpack.rx2.RxRatpack
import ratpack.server.RatpackServer
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    RxRatpack.initialize()

    Application(args)
}

class Properties(
    var examplePrivilegesYamlPath: String? = null
)

class MainModule(
    private val lockTimeTo: Long? = null
) : AbstractModule() {

    override fun configure() {
    }


    // Services

    @Provides
    @Singleton
    fun customerService(customerRepository: CustomerRepository, privilegesRepository: PrivilegesRepository, clock: Clock): CustomerService =
        CustomerService(customerRepository, privilegesRepository, clock)


    // Repositories

    @Provides
    @Singleton
    fun customerRepository(): CustomerRepository =
        InMemoryCustomerRepository()

    @Provides
    @Singleton
    fun privilegesRepository(properties: Properties): PrivilegesRepository =
        SlowPrivilegesRepository(checkNotNull(properties.examplePrivilegesYamlPath) { "app.examplePrivilegesYamlPath is a required argument" })


    // Utilities

    @Provides
    @Singleton
    fun clock(): Clock =
        if(lockTimeTo != null)
            Clock.fixed(Instant.ofEpochSecond(lockTimeTo), ZoneOffset.UTC)
        else
            Clock.systemUTC()


    // Error handlers

    @Provides
    @Singleton
    fun errorHandler(): ErrorHandler =
        ErrorHandler()

    @Provides
    @Singleton
    fun serverErrorHandler(errorHandler: ErrorHandler): ServerErrorHandler =
        errorHandler

    @Provides
    @Singleton
    fun clientErrorHandler(errorHandler: ErrorHandler): ClientErrorHandler =
        errorHandler
}

class Application(private val args: Array<String>) : AutoCloseable {

    companion object {
        private val log = LoggerFactory.getLogger(Application::class.java)
    }


    private val server = RatpackServer.of { server -> server
        .serverConfig { config -> config
            .development(false)
            .args("app.", "=", args)
            .env("APP_")
            .findBaseDir("basedir.marker")
            .port(
                args.find { it.startsWith("app.port=") }
                    ?.split(delimiters = *charArrayOf('='), limit = 2)
                    ?.last()
                    ?.toInt()
                    ?: 5050
            )
        }
        .registry(Guice.registry { bindings -> bindings
            .bindInstance(bindings.serverConfig.get(Properties::class.java))
            .module(MainModule(
                bindings.serverConfig.rootNode.get("lockTimeTo")?.asLong()
            ))
        })
        .handlers { chain -> chain
            .prefix("api/v1", ApiV1(chain.registry))
        }
    }

    private val shutdownThread = thread(start = false, name = "shutdown", block = ::close)

    init {
        Runtime.getRuntime().addShutdownHook(shutdownThread)

        log.info("Starting application...")
        server.start()
        log.info("Started application")
    }

    override fun close() {
        log.info("Stopping application...")

        try {
            if(server.isRunning)
                server.stop()
        } finally {
            Runtime.getRuntime().removeShutdownHook(shutdownThread)
            log.info("Stopped application")
        }
    }
}