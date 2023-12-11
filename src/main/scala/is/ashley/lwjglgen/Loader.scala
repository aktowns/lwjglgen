package is.ashley.lwjglgen

import is.ashley.lwjglgen.codegen.{
  BasicScala,
  FunMod,
  MethodBody,
  Name,
  ObjectBody,
  ScalaComment,
  ScalaType,
  SourceFile,
  TypeParamClauseGroup
}
import is.ashley.lwjglgen.collector.{JavaInfo, Signature, SignatureCollector}
import is.ashley.lwjglgen.transformer.{Helpers, JavaStaticClassTransformer, TypeMapper}

import java.io.File
import java.lang.reflect.{Field, Method}
import java.nio.file.Path
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

class Loader(jarPaths: Seq[File], docInfo: Map[Signature, JavaInfo] = Map.empty, formatConfig: Option[Path] = None) {
  private val formatter = formatConfig.map(new Formatter(_))
  private val imports = List(
    BasicScala.DirectImport(List("cats", "effect", "Async")),
    BasicScala.DirectImport(List("util", "OpenGLExecutionContext"))
  )

  def varHandler(_klass: Class[?], field: Field, info: Option[JavaInfo.ValInfo]): Option[ObjectBody] = {
    val sig     = info.fold("\n\n@note [No Signature]")(i => s"\n\n@note ${i.signature.str}")
    val comment = ScalaComment(info.flatMap(fi => fi.docComment).fold(sig)(_ ++ sig))

    Some(ObjectBody.Val(
      Name(field.getName),
      TypeMapper(field.getType),
      Name(Name(field.getDeclaringClass.getSimpleName), Name(field.getName)),
      Some(comment)
    ))
  }

  def methodHandler(_klass: Class[?], method: Method, info: Option[JavaInfo.MethodInfo]): Option[ObjectBody] = {
    val sig     = info.fold("\n\n@note [No Signature]")(i => s"\n\n@note ${i.signature.str}")
    val comment = ScalaComment(info.flatMap(mi => mi.docComment).fold(sig)(_ ++ sig))

    val args = method.getParameters.toList.zipWithIndex.map { case (p, i) =>
      val name = info.flatMap(_.argNames.lift(i)).map(_._1).getOrElse(p.getName)
      val ty = if (p.isVarArgs) {
        p.getType.getComponentType
      } else {
        p.getType
      }
      (Name(name), TypeMapper(ty))
    }
    val argsIn = method.getParameters.toList.zipWithIndex.map { case (p, i) =>
      val name = info.flatMap(_.argNames.lift(i)).map(_._1).getOrElse(p.getName)
      if (p.isVarArgs) {
        Name(s"$name*")
      } else {
        Name(name)
      }
    }
    val ret = TypeMapper(method.getReturnType)

    // val call = MethodBody.FunCall(
    //   MethodBody.FunCall(
    //     MethodBody.FunCall(
    //       Name(Name("OpenGLExecutionContext"), Name("evalOnMethodCall")),
    //       MethodBody.FunCall(
    //         MethodBody.FunCall(
    //           Name(Name("OpenGLExecutionContext"), Name("methodCall")),
    //           Name(Name(method.getDeclaringClass.getSimpleName), Name(method.getName))
    //         ),
    //         argsIn
    //       )
    //     ),
    //     Name("OpenGLExecutionContext")
    //   ),
    //   Name("F")
    // )

    val methname = if (method.getName.toLowerCase.startsWith(method.getDeclaringClass.getSimpleName.toLowerCase)) {
      val n = method.getName.drop(method.getDeclaringClass.getSimpleName.length)
      n.head.toLower + n.tail
    } else if (method.getName.startsWith("gl") && method.getName.charAt(2).isUpper) {
      val n = method.getName.drop(2)
      n.head.toLower + n.tail
    } else {
      method.getName
    }

    val typeKlass = Helpers.typeConstraint("F", "Async")

    Some(ObjectBody.FunDef(
      Name(methname),
      args,
      ScalaType("F", ret),
      List( /*call*/ ),
      List(FunMod.Transparent, FunMod.Inline),
      Some(TypeParamClauseGroup(typeKlass)),
      Some(comment),
      method.isVarArgs
    ))
  }

  def load(fqcn: String): String = {
    val loader = new URLClassLoader(jarPaths.toArray.map(_.toURI.toURL), this.getClass.getClassLoader)
    val klass  = Class.forName(fqcn, false, loader)

    val objCode     = JavaStaticClassTransformer(docInfo, klass, methodHandler, varHandler)
    val scalaSource = SourceFile(Name.Simple("generated"), imports :+ objCode).toDoc.render(80)
    formatter.fold(scalaSource)(_(scalaSource))
  }
}

object Loader {
  def withLibraryPaths(jarPaths: Seq[File], paths: List[File]): Loader = {
    val docInfo = SignatureCollector(paths)
    new Loader(jarPaths, docInfo)
  }
}
