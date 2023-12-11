package is.ashley.lwjglgen

import org.scalafmt.interfaces.Scalafmt

import java.nio.file.{Files, Path, Paths}

class Formatter(config: Path) {
  val scalafmt = Scalafmt.create(this.getClass.getClassLoader)

  def apply(contents: String): String = {
    println(s"using format config: $config")
    scalafmt.format(config, Path.of("src", "main", "scala", "generated", "Main.scala"), contents)
  }
}
