package net.jbock.examples;

import net.jbock.Converter;
import net.jbock.util.StringConverter;

import java.math.BigInteger;
import java.util.function.Supplier;

@Converter
class CustomBigIntegerMapperSupplier implements Supplier<StringConverter<BigInteger>> {

    @Override
    public StringConverter<BigInteger> get() {
        return StringConverter.create(s -> {
            if (s.startsWith("0x")) {
                return new BigInteger(s.substring(2), 16);
            }
            return new BigInteger(s);
        });
    }
}
