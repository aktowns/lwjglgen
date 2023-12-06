package is.ashley.lwjglgen

import org.scalafmt.interfaces.Scalafmt

import java.nio.file.{Path, Paths}

object Formatter {
  val scalafmt = Scalafmt.create(this.getClass.getClassLoader)
  val config   = Paths.get(".scalafmt.conf")

  def apply(contents: String): String =
    scalafmt.format(config, Path.of("src", "main", "scala", "generated", "Main.scala"), contents)
}
