package is.ashley.lwjglgen.collector

import com.thoughtworks.qdox.JavaProjectBuilder

import java.io.File
import collection.JavaConverters.*

object SignatureCollector {
  def apply(paths: List[File]): Map[Signature, JavaInfo] = {
    val jbuilder = new JavaProjectBuilder()

    paths.foreach(path => jbuilder.addSourceTree(path))

    val klassList = jbuilder.getClasses.asScala.toList

    klassList.flatMap { jclass =>
      val fields = jclass.getFields.asScala.toList.map { jfield =>
        val doclets = jfield.getTags.asScala.toList.map(d => s"@${d.getName} ${d.getValue}").mkString("\n")
        val doc     = Option(jfield.getComment).map(comment => comment + "\n" + doclets)
        val sig     = Signature.fromJavaField(jfield)
        sig -> JavaInfo.ValInfo(sig, doc)
      }
      val methods = jclass.getMethods.asScala.toList.map { jmeth =>
        val args    = jmeth.getParameters.asScala.toList.map(a => (a.getName, a.getType.getCanonicalName))
        val doclets = jmeth.getTags.asScala.toList.map(d => s"@${d.getName} ${d.getValue}").mkString("\n")
        val doc     = Option(jmeth.getComment).map(comment => comment + "\n" + doclets)
        val sig     = Signature.fromJavaMethod(jmeth)
        sig -> JavaInfo.MethodInfo(sig, doc, args)
      }
      fields ++ methods
    }.toMap
  }
}
