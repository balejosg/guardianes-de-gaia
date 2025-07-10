package com.guardianes;

import com.guardianes.testconfig.NoRedisTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(NoRedisTestConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class GuardianesApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // with all the security configurations
    }
}
