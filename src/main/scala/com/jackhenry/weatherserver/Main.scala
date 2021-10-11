package com.jackhenry.weatherserver

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    WeatherserverServer.stream[IO].compile.drain.as(ExitCode.Success)
}
