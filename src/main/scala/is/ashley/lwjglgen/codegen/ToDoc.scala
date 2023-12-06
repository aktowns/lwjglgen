package is.ashley.lwjglgen.codegen

import org.typelevel.paiges.Doc

trait ToDoc[A] {
  def toDoc(a: A): Doc
}

object ToDoc {
  def apply[A](implicit ev: ToDoc[A]): ToDoc[A] = ev

  object syntax {
    implicit class ToDocOps[A: ToDoc](self: A) {
      def toDoc: Doc = implicitly[ToDoc[A]].toDoc(self)
    }
  }
}
