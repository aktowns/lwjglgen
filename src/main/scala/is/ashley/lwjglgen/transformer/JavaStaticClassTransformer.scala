package is.ashley.lwjglgen.transformer

import is.ashley.lwjglgen.ScalaFile
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
import is.ashley.lwjglgen.collector.{JavaInfo, Signature}

import java.lang.reflect.{Field, Method}

object JavaStaticClassTransformer {

  private def wrapVars(docInfo: Map[Signature, JavaInfo], fields: List[Field]): List[ObjectBody] =
    fields.map { field =>
      val sig = Signature.fromField(field)
      val info: Option[JavaInfo.ValInfo] = docInfo.get(sig).collect {
        case x @ JavaInfo.ValInfo(_) => x
      }

      ObjectBody.Val(
        Name.Simple(field.getName),
        TypeMapper(field.getType),
        Name.Qualified(List(Name.Simple(field.getDeclaringClass.getSimpleName), Name.Simple(field.getName))),
        Some(ScalaComment(info.flatMap(fi => fi.docComment).fold(s"\n\n@note ${sig.str}")(x =>
          x + "\n\n@note " + sig.str
        )))
      )
    }

  private def wrapMethods(
    docInfo: Map[Signature, JavaInfo],
    klass: Class[?],
    methods: List[Method]): List[ObjectBody] = {
    val pkg: Name = Name.Qualified(klass.getPackageName.split("\\.").toList.map(Name.Simple(_)))
    val qualified = Name.Qualified(List(pkg, Name.Simple(klass.getSimpleName)))

    methods.filter(m => m.getName != "access$000").map { method =>
      val sig = Signature.fromMethod(method)
      val info: Option[JavaInfo.MethodInfo] = docInfo.get(sig).collect {
        case x @ JavaInfo.MethodInfo(_, _) => x
      }

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
          Name.Simple(s"${name}*")
        } else {
          Name.Simple(name)
        }
      }
      val ret = TypeMapper(method.getReturnType)

      val call = MethodBody.FunCall(
        evalOnMethodCall,
        List(
          Right(
            MethodBody.FunCall(
              methodCall,
              List(Right(MethodBody.FunCall(
                Name.Qualified(List(qualified, Name.Simple(method.getName))),
                argsIn.map(Left(_))
              )))
            )
          ),
          Left(Name.Simple("OpenGLExecutionContext"))
        )
      )

      val methname = if (method.getName.toLowerCase.startsWith(klass.getSimpleName.toLowerCase)) {
        val n = method.getName.drop(klass.getSimpleName.length)
        n.head.toLower + n.tail
      } else if (method.getName.startsWith("gl") && method.getName.charAt(2).isUpper) {
        val n = method.getName.drop(2)
        n.head.toLower + n.tail
      } else {
        method.getName
      }

      val typeKlass = Helpers.typeConstraint("F", "Async")

      ObjectBody.FunDef(
        Name.Simple(methname),
        args,
        ScalaType.Parametrized(Name.Simple("F"), List(ret)),
        List(call),
        List(FunMod.Transparent, FunMod.Inline),
        Some(TypeParamClauseGroup(List(typeKlass))),
        Some(ScalaComment(info.flatMap(mi => mi.docComment).fold(s"\n\n@note ${sig.str}")(x =>
          x + "\n\n@note " + sig.str
        ))),
        method.isVarArgs
      )

    }
  }

  def apply(docInfo: Map[Signature, JavaInfo], klass: Class[?]): BasicScala = {
    val methods: List[Method]            = klass.getDeclaredMethods.toList.filter(m => m.canAccess(null))
    val vars: List[Field]                = klass.getDeclaredFields.toList.filter(m => m.canAccess(null))
    val wrappedVars: List[ObjectBody]    = wrapVars(docInfo, vars)
    val wrappedMethods: List[ObjectBody] = wrapMethods(docInfo, klass, methods)

    val wrappedKlass = if (klass.getSimpleName.matches("GL\\d+")) {
      BasicScala.Obj(
        Name.Simple(klass.getSimpleName),
        wrappedVars.map(Left(_)) :+ Right(BasicScala.Obj(
          Name.Simple("GL"),
          wrappedMethods.map(Left(_))
        ))
      )
    } else {
      BasicScala.Obj(
        Name.Simple(klass.getSimpleName),
        wrappedVars.map(Left(_)) ++ wrappedMethods.map(Left(_))
      )
    }

    SourceFile(Name.Simple("generated"), imports :+ wrappedKlass)
  }

}
