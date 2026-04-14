package org.example.service

import org.example.generated.model.Greeting
import org.springframework.stereotype.Service

@Service
class HelloService {

    fun getHello(name: String?): Greeting {
        val message = if (!name.isNullOrBlank()) {
            "Hello, $name!"
        } else {
            "Hello, World!"
        }

        return Greeting(message = message)
    }
}
