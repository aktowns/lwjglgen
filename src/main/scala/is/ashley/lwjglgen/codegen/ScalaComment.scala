package is.ashley.lwjglgen.codegen

import org.typelevel.paiges.Doc

case class ScalaComment(comment: String)

object ScalaComment {
  implicit val scalaCommentToDoc: ToDoc[ScalaComment] =
    (scalaComment: ScalaComment) => Doc.text(scalaComment.comment).tightBracketBy(Doc.text("/**"), Doc.text("*/"))
}
