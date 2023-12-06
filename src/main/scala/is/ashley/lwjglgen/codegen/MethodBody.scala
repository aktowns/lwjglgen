package is.ashley.lwjglgen.codegen

import ToDoc.syntax.ToDocOps
import org.typelevel.paiges.Doc

sealed trait MethodBody
object MethodBody {
  case class FunCall(name: Name, args: List[Either[Name, MethodBody]]) extends MethodBody

  implicit val methodBodyToDoc: ToDoc[MethodBody] = {
    case MethodBody.FunCall(name, args) =>
      name.toDoc + Doc.char('(') + Doc.intercalate(
        Doc.char(','),
        args.map {
          case Left(name)        => name.toDoc
          case Right(methodBody) => methodBody.toDoc
        }
      ) + Doc.char(')')
  }
}
