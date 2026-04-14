package org.example.controller

import org.example.generated.api.HelloApi
import org.example.generated.model.Greeting
import org.example.service.HelloService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloApiImpl(
    private val helloService: HelloService
) : HelloApi {

    override fun getHello(name: String?): ResponseEntity<Greeting> {
        return ResponseEntity.ok(helloService.getHello(name))
    }
}
