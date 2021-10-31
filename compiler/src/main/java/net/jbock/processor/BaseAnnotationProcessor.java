/*
 * Copyright 2014 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jbock.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

abstract class BaseAnnotationProcessor extends AbstractProcessor {

    private Elements elements;
    private List<? extends Step> steps;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
        this.steps = steps();
    }

    abstract List<? extends Step> steps();

    private Set<TypeElement> getSupportedAnnotationTypeElements() {
        return steps.stream()
                .flatMap(step -> getSupportedAnnotationTypeElements(step).stream())
                .collect(collectingAndThen(toList(), HashSet::new));
    }

    private Set<TypeElement> getSupportedAnnotationTypeElements(Step step) {
        return step.annotations().stream()
                .map(elements::getTypeElement)
                .filter(Objects::nonNull)
                .collect(collectingAndThen(toList(), HashSet::new));
    }

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return steps.stream()
                .flatMap(step -> step.annotations().stream())
                .collect(collectingAndThen(toList(), HashSet::new));
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        process(validElements(roundEnv));
        return false;
    }

    private void process(Map<TypeElement, Set<Element>> validElements) {
        for (Step step : steps) {
            Set<TypeElement> annotationTypes = getSupportedAnnotationTypeElements(step);
            Map<TypeElement, Set<Element>> stepElements = new LinkedHashMap<>();
            validElements.forEach((k, v) -> {
                if (annotationTypes.contains(k)) {
                    stepElements.put(k, v);
                }
            });
            step.process(toClassNameKeyedMultimap(stepElements));
        }
    }

    private Map<TypeElement, Set<Element>> validElements(RoundEnvironment roundEnv) {

        Map<TypeElement, Set<Element>> validElements = new LinkedHashMap<>();

        for (TypeElement annotationType : getSupportedAnnotationTypeElements()) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
                validElements.compute(annotationType, (k, v) -> {
                    if (v == null) {
                        v = new HashSet<>();
                    }
                    v.add(element);
                    return v;
                });
            }
        }

        return validElements;
    }

    private static Map<String, Set<Element>> toClassNameKeyedMultimap(
            Map<TypeElement, Set<Element>> elements) {
        Map<String, Set<Element>> builder = new LinkedHashMap<>(elements.size());
        elements.forEach((k, v) -> builder.put(k.getQualifiedName().toString(), v));
        return builder;
    }

    public interface Step {

        Set<String> annotations();

        void process(Map<String, Set<Element>> elementsByAnnotation);
    }
}
