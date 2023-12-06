package is.ashley.lwjglgen.collector

sealed trait JavaInfo

object JavaInfo {
  case class ValInfo(docComment: Option[String]) extends JavaInfo

  case class MethodInfo(docComment: Option[String], argNames: List[(String, String)]) extends JavaInfo
}
