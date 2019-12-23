package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariousArgumentsTest {

  @Test
  void bigDecimal() {
    VariousArguments_Parser.ParseResult parsed = new VariousArguments_Parser().parse(new String[]{
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
        "^[abc]*$"
    });
    assertTrue(parsed instanceof VariousArguments_Parser.ParsingSuccess);
    VariousArguments args = ((VariousArguments_Parser.ParsingSuccess) parsed).getResult();
    assertEquals(new BigDecimal("3.14159265358979323846264338327950288419716939937510"), args.bigDecimal());
    assertEquals(Optional.of(Paths.get("/home")), args.pathPos());
    assertEquals(URI.create("http://localhost:8080"), args.uri());
    assertEquals(Optional.of(URI.create("http://localhost:8080")), args.uriPos());
  }
}