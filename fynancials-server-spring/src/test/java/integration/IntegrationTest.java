package integration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = {"de.as.fynancials", "integration"})
@Sql(scripts = {"/db/example-data/example-data.sql", "/exchange-rates.sql",
    "/historical-security-prices.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public @interface IntegrationTest {}
