enablePlugins(GatlingPlugin)

name := "gatling"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies := Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" % "test",
  "io.gatling"            % "gatling-test-framework"    % "2.1.7" % "test"
)