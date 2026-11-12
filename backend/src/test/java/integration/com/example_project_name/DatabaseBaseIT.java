package integration.com.example_project_name;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.example_project_name.app.MyServiceApplication;

/**
 * Base class for integration tests that need a real database (Oracle or SQL Server via Testcontainers).
 * No SSO profile is active, so security is fail-closed - authenticate in tests with @WithMockUser.
 */
@SpringBootTest(classes = MyServiceApplication.class)
public abstract class DatabaseBaseIT {

    private static final JdbcDatabaseContainer<?> DB = DatabaseContainers.getInstance();

    @DynamicPropertySource
    static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DB::getJdbcUrl);
        registry.add("spring.datasource.username", DB::getUsername);
        registry.add("spring.datasource.password", DB::getPassword);
        registry.add("spring.datasource.driver-class-name", DB::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log",
                () -> "classpath:db/changelog/db.changelog-test.xml");
    }
}
