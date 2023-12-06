package is.ashley.lwjglgen.transformer

import is.ashley.lwjglgen.codegen.{Name, ScalaType}

object TypeMapper {
  def apply(input: Class[?]): ScalaType =
    if (input.isArray) {
      ScalaType("Array", TypeMapper(input.getComponentType))
    } else if (input.isPrimitive) {
      input.getTypeName match {
        case "void"                                                                   => ScalaType("Unit")
        case x @ ("float" | "int" | "double" | "long" | "short" | "byte" | "boolean") => ScalaType(x.capitalize)
        case x                                                                        => ScalaType(x)
      }
    } else {
      val pkgParts = input.getCanonicalName.split("\\.")
        .map(x => Name.Simple(x))
        .toList.init

      val pkg = Name.Qualified(pkgParts)

      ScalaType.Simple(Name.Qualified(List(pkg, Name.Simple(input.getSimpleName))))
    }

}
