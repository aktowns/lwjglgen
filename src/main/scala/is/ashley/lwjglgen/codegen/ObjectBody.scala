package is.ashley.lwjglgen.codegen

import ToDoc.syntax.ToDocOps
import org.typelevel.paiges.Doc

case class TypeParamClause(typ: ScalaType, constraint: List[ScalaType] = List())
object TypeParamClause {
  def apply(typ: ScalaType, constraint: ScalaType*): TypeParamClause = TypeParamClause(typ, constraint.toList)
}

case class TypeParamClauseGroup(clauses: List[TypeParamClause])
object TypeParamClauseGroup {
  def apply(clauses: TypeParamClause*): TypeParamClauseGroup = TypeParamClauseGroup(clauses.toList)
}

sealed trait ObjectBody
object ObjectBody {
  case class Val(name: Name, typ: ScalaType, value: Name, comment: Option[ScalaComment] = None) extends ObjectBody

  case class FunDef(
    name: Name,
    args: List[(Name, ScalaType)],
    ret: ScalaType,
    methodBody: List[MethodBody],
    mod: List[FunMod] = List(),
    paramClause: Option[TypeParamClauseGroup] = None,
    comment: Option[ScalaComment] = None,
    isVarArgs: Boolean = false)
      extends ObjectBody

  implicit val typeParamClauseToDoc: ToDoc[TypeParamClause] = {
    case TypeParamClause(typ, constraint) =>
      constraint match {
        case Nil => typ.toDoc
        case _   => typ.toDoc + Doc.text(" : ") + Doc.intercalate(Doc.char('&'), constraint.map(_.toDoc))
      }
  }

  implicit val typeParamClauseGroupToDoc: ToDoc[TypeParamClauseGroup] = (typeParamClauseGroup: TypeParamClauseGroup) =>
    typeParamClauseGroup.clauses match {
      case Nil         => Doc.empty
      case clauses @ _ => Doc.char('[') + Doc.intercalate(Doc.char(','), clauses.map(_.toDoc)) + Doc.char(']')
    }

  implicit val objectBodyToDoc: ToDoc[ObjectBody] = {
    case ObjectBody.Val(name, typ, value, comment) =>
      val commentDoc = comment.map(_.toDoc + Doc.line).getOrElse(Doc.empty)
      commentDoc + Doc.text("val") + Doc.space + name.toDoc + Doc.text(": ") + typ.toDoc + Doc.text(
        " = "
      ) + value.toDoc
    case ObjectBody.FunDef(name, args, ret, body, mods, clauses, comment, isVarArgs) =>
      val varargsDoc = if (isVarArgs) {
        Doc.text("*")
      } else {
        Doc.empty
      }
      val modDoc = if (mods.isEmpty) {
        Doc.empty
      } else {
        Doc.intercalate(Doc.space, mods.map(_.toDoc)) + Doc.space
      }
      val paramDoc = clauses.map(_.toDoc).getOrElse(Doc.empty)
      val argDoc =
        if (args.isEmpty) {
          Doc.empty
        } else {
          Doc.char('(') +
            Doc.intercalate(
              Doc.char(','),
              args.map { case (name, typ) => name.toDoc + Doc.text(": ") + typ.toDoc }
            ) +
            varargsDoc + Doc.char(')')
        }
      val retDoc     = Doc.text(": ") + ret.toDoc
      val bodyDoc    = Doc.intercalate(Doc.line, body.map(_.toDoc))
      val commentDoc = comment.map(_.toDoc + Doc.line).getOrElse(Doc.empty)
      commentDoc + modDoc + Doc.text(
        "def"
      ) + Doc.space + name.toDoc + paramDoc + argDoc + retDoc + Doc.space + Doc.char(
        '='
      ) + Doc.space + bodyDoc
  }

}
