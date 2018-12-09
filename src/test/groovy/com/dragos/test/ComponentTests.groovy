package com.dragos.test

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Flowable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Executors

class ComponentTests extends Specification {
    private static objectMapper = new ObjectMapper()
    private static applicationJsonMediaType = MediaType.parse('application/json')

    @AutoCleanup
    def application = new Application(
        'app.lockTimeTo=1234567890',
        'app.repositoryType=slow-example',
        'app.port=12345',
        'app.examplePrivilegesYamlPath=examplePrivileges.yml'
    )

    def httpClient = new OkHttpClient.Builder().build()

    @Unroll
    def 'try to create with #description'() {

        when: 'sending create request to application'
        def createResponse = httpClient.newCall(
            new Request.Builder()
                .url("http://localhost:12345/api/v1/customer")
                .post(RequestBody.create(
                    applicationJsonMediaType,
                    objectMapper.writeValueAsString([
                        name: name
                    ])
                ))
                .addHeader('AuthToken', authToken)
                .build()
        ).execute()

        then: 'check expected status code'
        createResponse.code() == expectedCode


        when: 'sending get customers request to application'
        def getResponse = httpClient.newCall(
            new Request.Builder()
                .url("http://localhost:12345/api/v1/customer")
                .get()
                .addHeader('AuthToken', 'example-readonly')
                .build()
        ).execute()

        then: 'verify no customers were created'
        getResponse.code() == 200
        objectMapper.readValue(getResponse.body().byteStream(), List).size() == 0


        where:
        description               | authToken           | name   | expectedCode
        'no name'                 | 'example-readwrite' | null   | 400
        'no auth token'           | ''                  | 'John' | 401
        'invalid auth token'      | 'blah'              | 'John' | 401
        'insufficient privileges' | 'example-readonly'  | 'John' | 403
    }

    def 'successfully create'() {
        when:
        def response = httpClient.newCall(
            new Request.Builder()
                .url("http://localhost:12345/api/v1/customer")
                .post(RequestBody.create(
                    applicationJsonMediaType,
                    objectMapper.writeValueAsString([
                        name: 'abc'
                    ])
                ))
                .addHeader('AuthToken', 'example-readwrite')
                .build()
        ).execute()

        then:
        response.code() == 200
        objectMapper.readValue(response.body().byteStream(), Map) == [
            id: 1,
            name: 'abc',
            createdAt: '2009-02-13T23:31:30Z',
            lastLoggedInAt: '2009-02-13T23:31:30Z'
        ]
    }

    def 'create many'() {
        given:
        def pool = Executors.newFixedThreadPool(8)

        expect:
        Flowable.range(1, 100)
            .parallel(8)
            .runOn(Schedulers.from(pool))
            .map {
                httpClient.newCall(
                    new Request.Builder()
                        .url("http://localhost:12345/api/v1/customer")
                        .post(RequestBody.create(
                            applicationJsonMediaType,
                            objectMapper.writeValueAsString([
                                name: 'abc'
                            ])
                        ))
                        .addHeader('AuthToken', 'example-readwrite')
                        .build()
                ).execute()
            }
            .map({ response -> objectMapper.readValue(response.body().byteStream(), Map)['id'] } as Function)
            .sequential()
            .blockingIterable()
            .toSet()
            .size() == 100

        cleanup:
        pool.shutdownNow()
    }

    def 'persistOne'() {
        def file = new File('database-file.txt')
        file.delete()

        expect:
        Response r = httpClient.newCall(
                new Request.Builder()
                        .url("http://localhost:12345/api/v1/customer/persist")
                        .post(RequestBody.create(
                        applicationJsonMediaType,
                        objectMapper.writeValueAsString([
                                name: 'abc'
                        ])
                ))
                        .addHeader('AuthToken', 'example-readwrite')
                        .build()
        ).execute()
        r.body().string() == "1"
    }

    def 'persistMany'() {
        given:
        def file = new File('database-file.txt')
        file.delete()
        def pool = Executors.newFixedThreadPool(8)

        expect:

        Flowable.range(1, 100)
                .parallel(8)
                .runOn(Schedulers.from(pool))
                .map {
            httpClient.newCall(
                    new Request.Builder()
                            .url("http://localhost:12345/api/v1/customer/persist")
                            .post(RequestBody.create(
                            applicationJsonMediaType,
                            objectMapper.writeValueAsString([
                                    name: 'abc'
                            ])
                    ))
                            .addHeader('AuthToken', 'example-readwrite')
                            .build()
            ).execute()
        }
        .map({ response -> objectMapper.readValue(response.body().string(), String) } as Function)
        .sequential()
        .blockingIterable()
        .toSet()
        .size() == 100
    }
}