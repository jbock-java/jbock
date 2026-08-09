package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;
import net.jbock.SuperCommand;

import java.util.List;

@SuperCommand(
        name = "git",
        description = "Git is software for tracking changes in any set of files.")
interface GitCommand {

    @Command(
            name = "git-add",
            description = "Add file contents to the index",
            parseOrExitMethodAcceptsList = true)
    interface AddCommand {
        List<String> pathspec();

        // more parameters and options...
    }

    @Parameter(index = 0)
    String command();

    List<String> rest();

    // more options...
}
