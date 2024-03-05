package net.jbock.examples;

import net.jbock.examples.GitCommand.AddCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitCommandTest {

    @Test
    void testEscape() {
        String[] args = {"add", "foo", "bar"};
        GitCommand gitCommand = GitCommandParser.parse(List.of(args)).getRight().orElseThrow();
        assertEquals("add", gitCommand.command());
        AddCommand addCommand = GitCommand_AddCommandParser.parse(gitCommand.rest()).getRight().orElseThrow();
        assertEquals(List.of("foo", "bar"), addCommand.pathspec());
    }
}
