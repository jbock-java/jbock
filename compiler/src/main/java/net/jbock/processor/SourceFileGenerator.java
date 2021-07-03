package net.jbock.processor;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.jbock.common.OperationMode;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@ProcessorScope
public class SourceFileGenerator {

    private final Filer filer;
    private final Messager messager;
    private final OperationMode operationMode;

    @Inject
    SourceFileGenerator(
            Filer filer,
            Messager messager,
            OperationMode operationMode) {
        this.filer = filer;
        this.messager = messager;
        this.operationMode = operationMode;
    }

    void write(SourceElement sourceElement, TypeSpec typeSpec) {
        Preconditions.checkArgument(typeSpec.originatingElements.size() == 1);
        String packageName = sourceElement.generatedClass().packageName();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .skipJavaLangImports(true)
                .build();
        try {
            javaFile.writeTo(filer);
            if (operationMode.isTest()) {
                System.out.println("Printing generated code in OperationMode TEST");
                System.out.flush();
                javaFile.writeTo(System.err);
            }
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            messager.printMessage(Diagnostic.Kind.ERROR, stack, sourceElement.element());
        }
    }
}
