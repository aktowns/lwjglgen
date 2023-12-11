import is.ashley.lwjglgen.LWJGLPlugin.*

val lwjglVersion = "3.3.3"
val natives      = "natives-linux"

lazy val root: Project = (project in file("."))
  .enablePlugins(LWJGLPlugin)
  .settings(
    version      := "0.1",
    scalaVersion := "2.13.12",
    lwjglPackages := Seq(
      "org.lwjgl" % "lwjgl"        % lwjglVersion targets (),
      "org.lwjgl" % "lwjgl-vulkan" % lwjglVersion targets ("org.lwjgl.vulkan.VK10"),
      "org.lwjgl" % "lwjgl-glfw"   % lwjglVersion targets (),
      "org.lwjgl" % "lwjgl-stb"    % lwjglVersion targets ()
    ),
    lwjglScalafmtConfig := Some(baseDirectory.value / "scalafmt.conf")
  )
