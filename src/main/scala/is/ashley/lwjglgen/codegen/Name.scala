package is.ashley.lwjglgen.codegen

import org.typelevel.paiges.Doc

import ToDoc.syntax.*

sealed trait Name
object Name {
  case class Simple(name: String)                              extends Name
  case class Qualified(parts: List[Name])                      extends Name
  case class Parametrized(name: Name, params: List[ScalaType]) extends Name

  def apply(name: String): Name                     = Simple(name)
  def apply(parts: Name*): Name                     = Qualified(parts.toList)
  def apply(name: String, params: ScalaType*): Name = Parametrized(Simple(name), params.toList)

  private def sanitize(in: String): String = {
    val keywords = Set(
      "type",
      "val",
      "var",
      "def",
      "class",
      "object",
      "package",
      "trait",
      "extends",
      "with",
      "forSome",
      "new",
      "final",
      "private",
      "protected",
      "implicit",
      "override",
      "abstract",
      "sealed",
      "case",
      "if",
      "else",
      "while",
      "do",
      "for",
      "yield",
      "return",
      "throw",
      "try",
      "catch",
      "finally",
      "match",
      "case",
      "with",
      "type",
      "this",
      "super",
      "true",
      "false",
      "null",
      "macro",
      "given",
      "using",
      "derives",
      "extension"
    )
    if (keywords.contains(in)) {
      s"`$in`"
    } else {
      in
    }
  }

  implicit val nameToDoc: ToDoc[Name] = (name: Name) =>
    name match {
      case Name.Simple(name)    => Doc.text(sanitize(name))
      case Name.Qualified(name) => Doc.intercalate(Doc.char('.'), name.map(_.toDoc))
      case Name.Parametrized(name, params) =>
        name.toDoc + Doc.char('[') + Doc.intercalate(Doc.char(','), params.map(_.toDoc)) + Doc.char(']')
    }

}
