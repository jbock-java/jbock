package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class GitCommandTest {

    @Test
    void testEscape() {
        String[] args = {"add", "foo", "bar"};
        GitCommandParser.parse(List.of(args)).fold(l -> {
            fail("right expected");
            return l;
        }, gitCommand -> {
            assertEquals("add", gitCommand.command());
            GitCommand_AddCommandParser.parse(gitCommand.rest()).fold(l -> {
                fail("right expected");
                return l;
            }, addCommand -> {
                assertEquals(List.of("foo", "bar"), addCommand.pathspec());
                return addCommand;
            });
            return gitCommand;
        });
    }
}
