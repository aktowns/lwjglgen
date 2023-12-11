package is.ashley.lwjglgen.collector

sealed trait JavaInfo {
  val signature: Signature
}

object JavaInfo {
  case class ValInfo(signature: Signature, docComment: Option[String]) extends JavaInfo

  case class MethodInfo(signature: Signature, docComment: Option[String], argNames: List[(String, String)])
      extends JavaInfo
}
