package is.ashley.lwjglgen.transformer

import is.ashley.lwjglgen.codegen.{
  BasicScala,
  FunMod,
  MethodBody,
  Name,
  ObjectBody,
  ScalaComment,
  ScalaType,
  TypeParamClauseGroup
}
import is.ashley.lwjglgen.collector.{JavaInfo, Signature}

import java.lang.reflect.{Field, Method}

object JavaStaticClassTransformer {

  private type MethodHandler = (Class[?], Method, Option[JavaInfo.MethodInfo]) => Option[ObjectBody]
  private type VarHandler    = (Class[?], Field, Option[JavaInfo.ValInfo]) => Option[ObjectBody]

  def apply(
    docInfo: Map[Signature, JavaInfo],
    klass: Class[?],
    methodHandler: MethodHandler,
    varHandler: VarHandler): BasicScala = {

    val methods: List[ObjectBody] = klass
      .getDeclaredMethods
      .toList
      .filter(m => m.canAccess(null))
      .flatMap { method =>
        val sig = Signature.fromMethod(method)
        val info: Option[JavaInfo.MethodInfo] = docInfo.get(sig).collect {
          case x @ JavaInfo.MethodInfo(_, _, _) => x
        }
        methodHandler(klass, method, info)
      }

    val vars: List[ObjectBody] = klass
      .getDeclaredFields
      .toList
      .filter(m => m.canAccess(null))
      .flatMap { field =>
        val sig = Signature.fromField(field)
        val info: Option[JavaInfo.ValInfo] = docInfo.get(sig).collect {
          case x @ JavaInfo.ValInfo(_, _) => x
        }
        varHandler(klass, field, info)
      }

    BasicScala.Obj(Name(klass.getSimpleName), vars ++ methods)
  }

}
