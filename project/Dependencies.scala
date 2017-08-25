import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"
  lazy val googleCloudBigQuery = "com.google.cloud" % "google-cloud-bigquery" % "0.22.0-beta"
  lazy val typeSafeConfig = "com.typesafe" % "config" % "1.3.1"
}
