package is.ashley.lwjglgen.collector

import com.thoughtworks.qdox.model.{JavaField, JavaMethod}

import collection.JavaConverters.*
import java.lang.reflect.{Field, Method}

case class Signature(name: String, args: List[String], ret: String) {
  def str = s"$name|${args.mkString("|")}|$ret"
}

object Signature {
  def fromJavaField(jf: JavaField): Signature = {
    val name = jf.getDeclaringClass.getFullyQualifiedName + "." + jf.getName
    val sig  = Signature(name, Nil, jf.getType.getFullyQualifiedName)
    sig
  }

  def fromField(f: Field): Signature = {
    val name = f.getDeclaringClass.getCanonicalName + "." + f.getName
    val sig  = Signature(name, Nil, f.getType.getName)
    sig
  }

  def fromJavaMethod(jm: JavaMethod): Signature = {
    val args = jm.getParameters.asScala.toList.map { arg =>
      if (arg.isVarArgs) {
        arg.getType.getFullyQualifiedName + "[]"
      } else {
        arg.getType.getFullyQualifiedName
      }
    }
    val name = jm.getDeclaringClass.getFullyQualifiedName + "." + jm.getName
    val sig  = Signature(name, args, jm.getReturnType.getFullyQualifiedName)
    sig
  }

  def fromMethod(m: Method): Signature = {
    val args = m.getParameters.toList.map(_.getType.getCanonicalName)
    val name = m.getDeclaringClass.getCanonicalName + "." + m.getName
    val sig  = Signature(name, args, m.getReturnType.getName)
    sig
  }
}
