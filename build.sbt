ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.20"

lazy val root = (project in file("."))
  .settings(
    name := "shopping-cart-assign",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.5.2",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.5.2",
      "org.xerial" % "sqlite-jdbc" % "3.49.1.0",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0" % Test,
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10"
    )
  )
