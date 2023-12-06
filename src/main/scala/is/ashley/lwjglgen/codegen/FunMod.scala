package is.ashley.lwjglgen.codegen

import org.typelevel.paiges.Doc

sealed trait FunMod
object FunMod {
  case object Inline extends FunMod

  case object Transparent extends FunMod

  implicit val funModToDoc: ToDoc[FunMod] = {
    case FunMod.Inline      => Doc.text("inline")
    case FunMod.Transparent => Doc.text("transparent")
  }
}
