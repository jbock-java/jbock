package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import net.jbock.common.Util;
import net.jbock.model.CommandModel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;

@ContextScope
public class CreateModelMethod extends Cached<MethodSpec> {

  private final Util util;

  @Inject
  CreateModelMethod(Util util) {
    this.util = util;
  }

  @Override
  MethodSpec define() {
    List<CodeBlock> code = new ArrayList<>();
    code.add(CodeBlock.of("return $T.builder()", CommandModel.class));
    code.add(CodeBlock.of(".build()"));
    return methodBuilder("createModel")
        .addStatement(util.joinByNewline(code))
        .returns(CommandModel.class)
        .addModifiers(PRIVATE)
        .build();
  }
}
