package net.jbock.convert.map;

final class AutoConversion {

    private final String qualifiedName;
    private final MappingBlock block;

    AutoConversion(
            String qualifiedName,
            MappingBlock block) {
        this.qualifiedName = qualifiedName;
        this.block = block;
    }

    String qualifiedName() {
        return qualifiedName;
    }

    MappingBlock block() {
        return block;
    }
}
