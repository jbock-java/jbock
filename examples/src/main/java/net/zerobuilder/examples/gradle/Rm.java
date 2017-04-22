package net.zerobuilder.examples.gradle;

import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.LongName;
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
  Rm(@ShortName('r') @LongName("recursive")
         boolean recursive,
     @ShortName('f') @LongName("force")
         boolean force,
     @OtherTokens @Description("redundant files")
         List<String> goodFiles,
     @EverythingAfter("--") @Description("files named '--force' etc")
         List<String> badFiles) {
    this.recursive = recursive;
    this.force = force;
    this.filesToDelete = Stream.of(goodFiles, badFiles)
        .map(List::stream)
        .flatMap(Function.identity())
        .collect(Collectors.toList());
  }
}
