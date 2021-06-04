package net.jbock.context;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.util.ConverterError;
import net.jbock.util.ConverterFailure;
import net.jbock.util.ItemType;

import javax.inject.Inject;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

public class ConvEx {

  private final CommonFields commonFields;
  private final GeneratedTypes generatedTypes;

  private final ParameterSpec paramFailure = ParameterSpec.builder(ConverterFailure.class, "failure")
      .build();
  private final ParameterSpec paramItemType = ParameterSpec.builder(ItemType.class, "itemType")
      .build();
  private final ParameterSpec paramItemName = ParameterSpec.builder(STRING, "itemName")
      .build();

  @Inject
  ConvEx(GeneratedTypes generatedTypes, CommonFields commonFields) {
    this.generatedTypes = generatedTypes;
    this.commonFields = commonFields;
  }

  public TypeSpec define() {
    FieldSpec error = commonFields.convExError();
    return TypeSpec.classBuilder(generatedTypes.convExType())
        .superclass(RuntimeException.class)
        .addField(error)
        .addMethod(MethodSpec.constructorBuilder()
            .addParameter(paramFailure)
            .addParameter(paramItemType)
            .addParameter(paramItemName)
            .addStatement("this.$N = new $T($N, $N, $N)",
                error, ConverterError.class,
                paramFailure, paramItemType, paramItemName)
            .build())
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
  }
}
