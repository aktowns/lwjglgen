package is.ashley.lwjglgen.codegen

import ToDoc.syntax.ToDocOps
import org.typelevel.paiges.*

/** Super basic scala syntax tree for pretty printing
  */
case class SourceFile(packageName: Name, body: List[BasicScala])

sealed trait BasicScala
object BasicScala {
  case class Obj(name: Name, body: List[Either[ObjectBody, BasicScala]]) extends BasicScala
  case class DirectImport(parts: List[String])                           extends BasicScala

  implicit val sourceFileToDoc: ToDoc[SourceFile] = {
    case SourceFile(packageName, body) =>
      Doc.text("package ") + packageName.toDoc + Doc.line + Doc.line + Doc.intercalate(
        Doc.line,
        body.map(_.toDoc)
      ) + Doc.line
  }

  implicit val basicScalaToDoc: ToDoc[BasicScala] = {
    case BasicScala.Obj(name, body) =>
      val bodyDoc = Doc.intercalate(
        Doc.line,
        body.collect {
          case Left(objectBody)  => objectBody.toDoc
          case Right(basicScala) => basicScala.toDoc
        }
      )
      Doc.line + bodyDoc.tightBracketBy(
        Doc.text("object") + Doc.space + name.toDoc + Doc.space + Doc.char('{'),
        Doc.char('}')
      )
    case BasicScala.DirectImport(parts) =>
      Doc.text("import") + Doc.space + Doc.intercalate(Doc.char('.'), parts.map(Doc.text))
  }

}
