package com.fonsi13.blogbackend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires full Spring context with MongoDB and SMTP - run only in integration environment")
class BlogBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
