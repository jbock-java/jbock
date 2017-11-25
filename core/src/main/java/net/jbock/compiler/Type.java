package net.jbock.compiler;

import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.OPTIONAL_STRING;
import static net.jbock.compiler.Constants.STRING;

import java.util.EnumSet;
import java.util.Set;
import net.jbock.com.squareup.javapoet.TypeName;

/**
 * An enumeration of the enum constants that {@link OptionType} defines.
 */
enum Type {

  FLAG(BOOLEAN, false, false),
  OPTIONAL(OPTIONAL_STRING, false, true),
  REQUIRED(STRING, false, true),
  REPEATABLE(LIST_OF_STRING, false, true),
  OTHER_TOKENS(LIST_OF_STRING, true, false),
  EVERYTHING_AFTER(LIST_OF_STRING, true, false);

  final TypeName sourceType;
  final boolean special;
  final boolean binding;

  Type(
      TypeName sourceType,
      boolean special,
      boolean binding) {
    this.sourceType = sourceType;
    this.special = special;
    this.binding = binding;
  }

  static Set<Type> nonBinding() {
    EnumSet<Type> result = EnumSet.noneOf(Type.class);
    for (Type type : values()) {
      if (!type.binding) {
        result.add(type);
      }
    }
    return result;
  }

  static Set<Type> special() {
    EnumSet<Type> result = EnumSet.noneOf(Type.class);
    for (Type type : values()) {
      if (type.special) {
        result.add(type);
      }
    }
    return result;
  }
}
