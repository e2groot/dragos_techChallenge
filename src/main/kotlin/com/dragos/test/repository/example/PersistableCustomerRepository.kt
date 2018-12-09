package com.dragos.test.repository.example

import com.dragos.test.model.CustomerCreate
import com.dragos.test.repository.IPersistableCustomerRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Single
import java.io.File
import java.lang.StringBuilder

class PersistableCustomerRepository(private val file: File): IPersistableCustomerRepository, InMemoryCustomerRepository() {
    
    override fun persistData(model: CustomerCreate): Single<Integer> {
        val strBuilder = StringBuilder()
        strBuilder.append(jacksonObjectMapper().writeValueAsString(model)).append("\n")
        //File("test-database.txt").writeText(strBuilder.toString())
        file.writeText(strBuilder.toString())
        System.out.println("You can reach me!!!")
        return Single.just(Integer(1))
    }


}