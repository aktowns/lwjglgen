package is.ashley.lwjglgen.transformer

import is.ashley.lwjglgen.codegen.{Name, ScalaType, TypeParamClause}

object Helpers {
  def typeConstraint(typeVar: String, clause: String): TypeParamClause = {
    val typ = ScalaType(Name(typeVar), ScalaType("_"))

    TypeParamClause(typ, ScalaType("Async"))
  }

  def methodCall(name: String, params: String*): Name =
    Name(Name(name, ScalaType("F")), Name("delay"))

}
