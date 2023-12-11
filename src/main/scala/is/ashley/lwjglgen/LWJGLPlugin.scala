package is.ashley.lwjglgen

import sbt.Keys.{dependencyClasspath, dependencyPicklePath, libraryDependencies}
import sbt.librarymanagement.ModuleID
import sbt.*

import java.io.File
import java.nio.file.Paths

object LWJGLPlugin extends AutoPlugin {
  override def trigger = allRequirements

  case class ModuleIDWithTargets(moduleId: ModuleID, targets: List[String] = List.empty)

  implicit class AddModuleIDWithTargets(moduleId: ModuleID) {
    def targets(klasses: String*): ModuleIDWithTargets =
      LWJGLPlugin.ModuleIDWithTargets(moduleId, klasses.toList)
  }

  object autoImport {
    val lwjglPackages       = settingKey[Seq[ModuleIDWithTargets]]("Packages to load dependencies from")
    val lwjglGenFiles       = taskKey[Seq[Attributed[File]]]("Classpath to load dependencies from")
    val lwjglScalafmtConfig = settingKey[Option[File]]("Path to scalafmt config file")
  }

  import autoImport.*

  private def jarPathFromModuleId(moduleId: ModuleID): String = {
    val org     = moduleId.organization.replace('.', '/')
    val name    = moduleId.name
    val version = moduleId.revision
    s"$org/$name/$version/$name-$version.jar"
  }

  override val projectSettings = Seq(
    libraryDependencies ++= lwjglPackages.value.map(_.moduleId),
    lwjglGenFiles := {
      lwjglPackages.value.foreach(println)
      val dependencies: List[(Attributed[File], ModuleIDWithTargets)] =
        (Compile / dependencyClasspath).value.flatMap { dependency =>
          val modId = dependency.get[ModuleID](AttributeKey("moduleID")).get

          lwjglPackages.value.find { pkg =>
            pkg.moduleId.organization == modId.organization &&
            pkg.moduleId.name == modId.name &&
            pkg.moduleId.revision == modId.revision
          }.map(pkg => (dependency, pkg))
        }.toList

      val loader = new Loader(
        dependencies.map(_._1.data),
        formatConfig = lwjglScalafmtConfig.value.map(file => Paths.get(file.toURI))
      )
      dependencies.foreach { case (dep, pkg) =>
        println(s"Found $dep for $pkg")
        println(s"Loading targets ${pkg.targets}")
        pkg.targets.foreach { klass =>
          loader.load(klass)
        }
      }
      Seq()
    }
  )
}
