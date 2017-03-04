def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str

lazy val publishResolver = {
  val artifactPattern = s"""${file("publish").absolutePath}/[revision]/[artifact]-[revision](-[classifier]).[ext]"""
  Resolver.file("publish").artifacts(artifactPattern)
}

lazy val commonSettings = Seq(
  name := s"KatLib-${removeSnapshot(spongeApiVersion.value)}",
  organization := "io.github.katrix",
  version := "2.0.1",
  scalaVersion := "2.12.1",
  assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("scala.**"     -> "io.github.katrix.katlib.shade.scala.@1").inAll,
    ShadeRule.rename("shapeless.**" -> "io.github.katrix.katlib.shade.shapeless.@1").inAll
  ),
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Yno-adapted-args", "-Ywarn-dead-code", "-Ywarn-unused-import"),
  crossPaths := false,
  publishTo := Some(publishResolver),
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := false,
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.copy(`classifier` = Some("assembly"))
  },
  addArtifact(artifact in (Compile, assembly), assembly),
  spongePluginInfo := spongePluginInfo.value.copy(
    id = "katlib",
    name = Some("KatLib"),
    version = Some(s"${removeSnapshot(spongeApiVersion.value)}-${version.value}"),
    authors = Seq("Katrix"),
    dependencies = Set(DependencyInfo("spongeapi", Some(removeSnapshot(spongeApiVersion.value))))
  ),
  libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2" exclude ("org.typelevel", "macro-compat_2.12"), //Don't think macro-compat needs to be in the jar
  artifactName := { (sv, module, artifact) =>
    s"A${artifact.name}-${module.revision}.${artifact.extension}"
  },
  assemblyJarName <<= (name, version) map { (name, version) =>
    s"A$name-assembly-$version.jar"
  }
)

lazy val katLibShared = (project in file("shared"))
  .enablePlugins(SpongePlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "KatLib-Shared",
    assembleArtifact := false,
    spongeMetaCreate := false,
    //Default version, needs to build correctly against all supported versions
    spongeApiVersion := "4.1.0",
    libraryDependencies += "org.scalameta" %% "scalameta" % "1.6.0" % Provided,
    resolvers += Resolver.sonatypeRepo("releases"),
    resolvers += Resolver.bintrayIvyRepo("scalameta", "maven"),
    addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M7" cross CrossVersion.full),
    scalacOptions += "-Xplugin-require:macroparadise",
    scalacOptions in (Compile, console) := Seq(), //macroparadise plugin doesn't work in repl yet.
    sources in (Compile, doc) := Nil, //macroparadise doesn't work with scaladoc yet.
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
  )

lazy val katLibV410 = (project in file("4.1.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(katLibShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "4.1.0")

lazy val katLibV500 = (project in file("5.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(katLibShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "5.0.0")

lazy val katLibV600 = (project in file("6.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(katLibShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "6.0.0-SNAPSHOT")

lazy val katLibRoot = (project in file("."))
  .settings(publishArtifact := false)
  .disablePlugins(AssemblyPlugin)
  .aggregate(katLibShared, katLibV410, katLibV500, katLibV600)