package net.jbock.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuArgumentsTest {

  @Test
  void testSudoku() {
    SudokuArguments_Parser.ParseResult parsed = new SudokuArguments_Parser().parse(new String[]{""});
    assertTrue(parsed instanceof SudokuArguments_Parser.ParsingSuccess);
    SudokuArguments args = ((SudokuArguments_Parser.ParsingSuccess) parsed).getResult();
    assertTrue(args.number().isEmpty());
  }
}