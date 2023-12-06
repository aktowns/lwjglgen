package is.ashley.lwjglgen.codegen

import ToDoc.syntax.ToDocOps
import org.typelevel.paiges.Doc

sealed trait ScalaType
object ScalaType {
  case class Simple(name: Name) extends ScalaType

  case class Parametrized(name: Name, params: List[ScalaType]) extends ScalaType

  def apply(name: String): ScalaType                     = Simple(Name.Simple(name))
  def apply(name: Name): ScalaType                       = Simple(name)
  def apply(name: String, params: ScalaType*): ScalaType = Parametrized(Name.Simple(name), params.toList)
  def apply(name: Name, params: ScalaType*): ScalaType   = Parametrized(name, params.toList)

  implicit val scalaTypeToDoc: ToDoc[ScalaType] = {
    case ScalaType.Simple(name) => name.toDoc
    case ScalaType.Parametrized(name, params) =>
      name.toDoc + Doc.char('[') + Doc.intercalate(Doc.char(','), params.map(_.toDoc)) + Doc.char(']')
  }
}
