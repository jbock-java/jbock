package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.matching.Match;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public abstract class Matcher extends ParameterScoped {

  private final EnumName enumName;

  protected Matcher(ParameterContext parameterContext, EnumName enumName) {
    super(parameterContext);
    this.enumName = enumName;
  }

  public abstract Optional<Match> tryMatch(AbstractParameter parameter);

  protected ParameterSpec constructorParam(TypeMirror constructorParamType) {
    return ParameterSpec.builder(
        TypeName.get(constructorParamType),
        enumName.camel()).build();
  }
}
