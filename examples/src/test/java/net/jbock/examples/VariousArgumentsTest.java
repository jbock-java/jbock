package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

class VariousArgumentsTest {

    private final ParserTestFixture<VariousArguments> f = ParserTestFixture.create(VariousArgumentsParser::parse);

    @Test
    void bigDecimal() {
        f.assertThat(
                "--bigDecimal", "3.14159265358979323846264338327950288419716939937510",
                "--bigInteger", "60221407600000000000000",
                "--path", "/home",
                "--localDate", "2001-02-01",
                "--uri", "http://localhost:8080",
                "--pattern", "^[abc]*$",
                "6.02214076e23",
                "60221407600000000000000",
                "/etc/hosts",
                "/home",
                "2001-02-01",
                "http://localhost:8080",
                "^[abc]*$")
                .has(VariousArguments::bigDecimal, new BigDecimal("3.14159265358979323846264338327950288419716939937510"))
                .has(VariousArguments::pathPos, Optional.of(Paths.get("/home")))
                .has(VariousArguments::uri, URI.create("http://localhost:8080"))
                .has(VariousArguments::uriPos, Optional.of(URI.create("http://localhost:8080")));
    }
}