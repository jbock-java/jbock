package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

final class MapperClassValidator {

  private final BasicInfo basicInfo;
  private final TypeMirror expectedReturnType;

  private final TypeVisitor<Boolean, Void> isInvalidBound =
      new SimpleTypeVisitor8<Boolean, Void>() {
        @Override
        protected Boolean defaultAction(TypeMirror e, Void _void) {
          return false;
        }

        @Override
        public Boolean visitDeclared(DeclaredType bound, Void _void) {
          return !tool().isAssignable(expectedReturnType, bound);
        }
      };

  MapperClassValidator(BasicInfo basicInfo, TypeMirror expectedReturnType) {
    this.basicInfo = basicInfo;
    this.expectedReturnType = expectedReturnType;
  }

  MapperType checkReturnType(
      TypeElement mapperClass) {
    commonChecks(basicInfo, mapperClass, "mapper");
    checkBound(mapperClass);
    TmpMapperType mapperType = getMapperType(mapperClass);
    TypeMirror string = tool().asType(String.class);
    TypeMirror t = asDeclared(mapperType.type).getTypeArguments().get(0);
    TypeMirror r = asDeclared(mapperType.type).getTypeArguments().get(1);
    Optional<Map<String, TypeMirror>> t_result = tool().unify(string, t);
    if (!t_result.isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    Optional<Map<String, TypeMirror>> r_result = tool().unify(expectedReturnType, r);
    if (!r_result.isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return mapperType.solve(basicInfo, t_result.get(), r_result.get());
  }

  private void checkBound(TypeElement mapperClass) {
    for (TypeParameterElement typeParameter : mapperClass.getTypeParameters()) {
      for (TypeMirror bound : typeParameter.getBounds()) {
        if (bound.accept(isInvalidBound, null)) {
          throw boom("Invalid bounds on the type parameters of the mapper class");
        }
      }
    }
  }

  private TmpMapperType getMapperType(TypeElement mapperClass) {
    Optional<TypeMirror> supplier = Resolver.typecheck(
        Supplier.class,
        mapperClass.asType(),
        basicInfo.tool());
    if (supplier.isPresent()) {
      List<? extends TypeMirror> typeArgs = asDeclared(supplier.get()).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      return TmpMapperType.create(basicInfo, typeArgs.get(0), true, mapperClass);
    }
    TypeMirror mapper = Resolver.typecheck(
        Function.class,
        mapperClass.asType(),
        basicInfo.tool()).orElseThrow(() ->
        boom("not a Function or Supplier<Function>"));
    return TmpMapperType.create(basicInfo, mapper, false, mapperClass);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }

  static final class TmpMapperType {

    private final TypeElement mapperClass; // implements Function or Supplier<Function>
    private final TypeMirror type; // subtype of Function
    private final boolean supplier; // wrapped in Supplier?

    private TmpMapperType(TypeElement mapperClass, TypeMirror type, boolean supplier) {
      this.mapperClass = mapperClass;
      this.type = type;
      this.supplier = supplier;
    }

    static TmpMapperType create(
        BasicInfo basicInfo,
        TypeMirror type,
        boolean supplier,
        TypeElement mapperClass) {
      if (!basicInfo.tool().isSameErasure(type, Function.class)) {
        throw boom(basicInfo, "must either implement Function or Supplier<Function>");
      }
      if (basicInfo.tool().isRawType(type)) {
        throw boom(basicInfo, "the function type must be parameterized");
      }
      return new TmpMapperType(mapperClass, type, supplier);
    }

    private static ValidationException boom(BasicInfo basicInfo, String message) {
      return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s", message));
    }

    MapperType solve(
        BasicInfo basicInfo,
        Map<String, TypeMirror> t_result,
        Map<String, TypeMirror> r_result) {
      List<? extends TypeParameterElement> typeParameters = mapperClass.getTypeParameters();
      List<TypeMirror> solution = new ArrayList<>(typeParameters.size());
      for (TypeParameterElement typeParameter : typeParameters) {
        String param = typeParameter.toString();
        TypeMirror tMirror = t_result.get(param);
        TypeMirror rMirror = r_result.get(param);
        if (tMirror != null) {
          solution.add(tMirror);
        } else if (rMirror != null) {
          solution.add(rMirror);
        } else {
          throw boom(basicInfo, "could not resolve all type parameters");
        }
      }
      return MapperType.create(basicInfo, supplier, mapperClass, solution);
    }
  }
}
