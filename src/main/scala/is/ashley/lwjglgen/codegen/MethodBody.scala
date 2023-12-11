package is.ashley.lwjglgen.codegen

import org.typelevel.paiges.Doc

trait MethodCallArgument {
  def toDoc: Doc
}

sealed trait MethodBody extends MethodCallArgument
object MethodBody {
  case class FunCall(name: Name, args: List[MethodCallArgument]) extends MethodBody {
    override def toDoc: Doc =
      name.toDoc + Doc.char('(') + Doc.intercalate(Doc.char(','), args.map(_.toDoc)) + Doc.char(')')
  }

  object FunCall {
    def apply(name: Name, args: MethodCallArgument*): FunCall = FunCall(name, args.toList)
  }

  implicit val methodBodyToDoc: ToDoc[MethodBody] = _.toDoc
}
