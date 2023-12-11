package is.ashley.lwjglgen.codegen

import ToDoc.syntax.ToDocOps
import org.typelevel.paiges.*

/** Super basic scala syntax tree for pretty printing
  */
trait ScalaNode {
  def toDoc: Doc
}

case class SourceFile(packageName: Name, body: List[BasicScala]) extends ScalaNode {
  override def toDoc: Doc =
    Doc.text("package ") + packageName.toDoc + Doc.line + Doc.line + Doc.intercalate(
      Doc.line,
      body.map(_.toDoc)
    ) + Doc.line
}

trait ObjectBodyNode extends ScalaNode

sealed trait BasicScala extends ObjectBodyNode
object BasicScala {
  case class Obj(name: Name, body: List[ObjectBodyNode]) extends BasicScala {
    override def toDoc: Doc = {
      val bodyDoc = Doc.intercalate(Doc.line, body.map(_.toDoc))
      Doc.line + bodyDoc.tightBracketBy(
        Doc.text("object") + Doc.space + name.toDoc + Doc.space + Doc.char('{'),
        Doc.char('}')
      )
    }

  }
  case class DirectImport(parts: List[String]) extends BasicScala {
    override def toDoc: Doc = Doc.text("import") + Doc.space + Doc.intercalate(Doc.char('.'), parts.map(Doc.text))
  }

  implicit val sourceFileToDoc: ToDoc[SourceFile] = _.toDoc
  implicit val basicScalaToDoc: ToDoc[BasicScala] = _.toDoc
}
