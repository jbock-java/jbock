package net.jbock.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SudokuArgumentsTest {

  @Test
  void testSudoku() {
    SudokuArguments_Parser.ParseResult parsed = SudokuArguments_Parser.create().parse(new String[]{""});
    assertTrue(parsed instanceof SudokuArguments_Parser.ParsingSuccess);
    SudokuArguments args = ((SudokuArguments_Parser.ParsingSuccess) parsed).getResult();
    assertTrue(args.number().isEmpty());
  }
}