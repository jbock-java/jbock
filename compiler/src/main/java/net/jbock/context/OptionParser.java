package net.jbock.context;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.util.ExToken;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

/**
 * Generates the inner class OptionParser and its subtypes.
 */
@ContextScope
public final class OptionParser {

  private final GeneratedTypes generatedTypes;
  private final NamedOptions namedOptions;
  private final FlagParser flagParser;
  private final RepeatableOptionParser repeatableOptionParser;
  private final RegularOptionParser regularOptionParser;

  @Inject
  OptionParser(
      GeneratedTypes generatedTypes,
      NamedOptions namedOptions,
      FlagParser flagParser,
      RepeatableOptionParser repeatableOptionParser,
      RegularOptionParser regularOptionParser) {
    this.generatedTypes = generatedTypes;
    this.namedOptions = namedOptions;
    this.flagParser = flagParser;
    this.repeatableOptionParser = repeatableOptionParser;
    this.regularOptionParser = regularOptionParser;
  }

  List<TypeSpec> define() {
    if (namedOptions.isEmpty()) {
      return List.of();
    }
    List<TypeSpec> result = new ArrayList<>();
    result.add(TypeSpec.classBuilder(generatedTypes.optionParserType())
        .addMethod(readMethodAbstract())
        .addMethod(streamMethodAbstract())
        .addModifiers(PRIVATE, STATIC, ABSTRACT)
        .build());
    if (namedOptions.anyFlags()) {
      result.add(flagParser.define());
    }
    if (namedOptions.anyRepeatable()) {
      result.add(repeatableOptionParser.define());
    }
    if (namedOptions.anyRegular()) {
      result.add(regularOptionParser.define());
    }
    return result;
  }

  private MethodSpec readMethodAbstract() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("read")
        .addException(ExToken.class)
        .addParameters(asList(token, it))
        .addModifiers(ABSTRACT)
        .returns(namedOptions.readMethodReturnType())
        .build();
  }

  MethodSpec streamMethodAbstract() {
    ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
    return MethodSpec.methodBuilder("stream")
        .returns(streamOfString)
        .addModifiers(ABSTRACT)
        .build();
  }
}
