package net.jbock.convert;

import io.jbock.util.Optional;

import javax.lang.model.element.TypeElement;

public class ConverterClass {

    private final Optional<TypeElement> converter;

    ConverterClass(Optional<TypeElement> converter) {
        this.converter = converter;
    }

    public Optional<TypeElement> converter() {
        return converter;
    }

    public boolean isPresent() {
        return converter.isPresent();
    }
}
