package net.jbock.examples;

import net.jbock.util.StringConverter;

import java.math.BigInteger;

class BigIntegerConverter extends StringConverter<BigInteger> {

    @Override
    protected BigInteger convert(String token) {
        if (token.startsWith("0x")) {
            return new BigInteger(token.substring(2), 16);
        }
        return new BigInteger(token);
    }
}
