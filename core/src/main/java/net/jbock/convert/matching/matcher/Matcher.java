package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.convert.matching.Match;
import net.jbock.parameter.AbstractParameter;

import javax.lang.model.type.TypeMirror;
import java.util.Locale;
import java.util.Optional;

public abstract class Matcher {

  private final EnumName enumName;

  protected Matcher(EnumName enumName) {
    this.enumName = enumName;
  }

  public abstract Optional<Match> tryMatch(AbstractParameter parameter);

  protected ParameterSpec constructorParam(TypeMirror constructorParamType) {
    String name = '_' + enumName.enumConstant().toLowerCase(Locale.US);
    TypeName type = TypeName.get(constructorParamType);
    return ParameterSpec.builder(type, name).build();
  }
}
