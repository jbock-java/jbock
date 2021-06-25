package net.jbock.examples;

import net.jbock.either.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.HelpRequested;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ListIntegerArgumentsTest {

  private final ListIntegerArgumentsParser parser = new ListIntegerArgumentsParser();

  private final ParserTestFixture<ListIntegerArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testPresent() {
    f.assertThat("-a", "1").succeeds("a", List.of(1));
  }

  @Test
  void testAbsent() {
    Either<NotSuccess, ListIntegerArguments> result = parser.parse(/* empty */);
    assertTrue(result.getLeft().isPresent());
    assertTrue(result.getLeft().get() instanceof HelpRequested);
  }
}