package net.jbock.context;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.util.Misconvert;
import net.jbock.util.ItemType;

import javax.inject.Inject;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class ConvEx {

  private final CommonFields commonFields;
  private final GeneratedTypes generatedTypes;
  private final ToConverterErrorMethod toConverterErrorMethod;

  private final ParameterSpec paramFailure = ParameterSpec.builder(Misconvert.class, "failure")
      .build();
  private final ParameterSpec paramItemType = ParameterSpec.builder(ItemType.class, "itemType")
      .build();
  private final ParameterSpec paramItemName = ParameterSpec.builder(STRING, "itemName")
      .build();

  @Inject
  ConvEx(
      GeneratedTypes generatedTypes,
      CommonFields commonFields,
      ToConverterErrorMethod toConverterErrorMethod) {
    this.generatedTypes = generatedTypes;
    this.commonFields = commonFields;
    this.toConverterErrorMethod = toConverterErrorMethod;
  }

  public TypeSpec define() {
    return TypeSpec.classBuilder(generatedTypes.convExType())
        .superclass(Exception.class)
        .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
            .addMember("value", CodeBlock.of("$S", "serial")).build())
        .addField(commonFields.convExFailure())
        .addField(commonFields.convExItemType())
        .addField(commonFields.convExItemName())
        .addMethod(MethodSpec.constructorBuilder()
            .addParameter(paramFailure)
            .addParameter(paramItemType)
            .addParameter(paramItemName)
            .addStatement("this.$N = $N", commonFields.convExFailure(), paramFailure)
            .addStatement("this.$N = $N", commonFields.convExItemType(), paramItemType)
            .addStatement("this.$N = $N", commonFields.convExItemName(), paramItemName)
            .build())
        .addMethod(toConverterErrorMethod.get())
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
  }
}
