package com.jackhenry.weatherserver

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.server.Server
import org.http4s.implicits._
import io.chrisdavenport.natchezhttp4sotel._
import io.chrisdavenport.fiberlocal.GenFiberLocal
import com.comcast.ip4s._
import com.typesafe.config.ConfigFactory

object Main extends IOApp with TracingEntryPoint {
//  def run(args: List[String]) =
//    WeatherserverServer.stream[IO].compile.drain.as(ExitCode.Success)

  def run(args: List[String]) = server[IO].use(_ => IO.never)


  def server[F[_]: Async: GenFiberLocal]: Resource[F, Server] =
    for {
      iClient <- EmberClientBuilder.default[F].build
      conf = ConfigFactory.parseResources("application.conf").resolve()
      ep <- entryPoint[F](conf.getString("conf.writeKey"), conf.getString("conf.service"), conf.getString("conf.dataSet"))
      app = ServerMiddleware.httpApp(ep){implicit T: natchez.Trace[F] =>
        val client = ClientMiddleware.trace(ep)(iClient)
        routes(client, conf.getString("conf.weatherApiKey")).orNotFound
      }
      sv <- EmberServerBuilder.default[F].withPort(port"8080").withHttpApp(app).build
    } yield sv
}
