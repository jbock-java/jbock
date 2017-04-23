package net.zerobuilder.examples.gradle;

import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Rm {

  final boolean recursive;
  final boolean force;
  final List<String> filesToDelete;

  @CommandLineArguments
  Rm(@ShortName('r') boolean recursive,
     @ShortName('f') boolean force,
     @OtherTokens List<String> fileNames,
     @EverythingAfter("--") @Description({
         "Last resort for problematic arguments",
         "For example, when file name is '-r'"})
         List<String> escapedFileNames) {
    this.recursive = recursive;
    this.force = force;
    this.filesToDelete = Stream.of(fileNames, escapedFileNames)
        .map(List::stream)
        .flatMap(Function.identity())
        .collect(Collectors.toList());
  }
}
