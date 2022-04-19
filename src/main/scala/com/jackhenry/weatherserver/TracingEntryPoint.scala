package com.jackhenry.weatherserver

import cats._
import cats.effect.{Trace => _, _}
import cats.syntax.all._
import natchez._
import natchez.honeycomb.Honeycomb
import org.http4s.HttpRoutes
import org.http4s.client.Client

trait TracingEntryPoint {
  // Our routes, in abstract F with a Trace constraint.
  def routes[F[_]: Async: Trace](client: Client[F], weatherApiKey: String)(
    implicit ev: MonadError[F, Throwable]
  ): HttpRoutes[F] = {
    val forecastAlg = Forecasts.impl[F](client)
    WeatherserverRoutes.forecastRoutes[F](forecastAlg, weatherApiKey)
  }

  def entryPoint[F[_]: Sync](writeKey: String, service: String, dataSet: String): Resource[F, EntryPoint[F]] =
    Honeycomb.entryPoint[F](service) { ob =>
      Sync[F].delay {
        ob.setWriteKey(writeKey)
          .setDataset(dataSet).build
      }
    }
}
