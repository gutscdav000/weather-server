package com.jackhenry.weatherserver

import cats.effect.Sync
import cats.implicits._
import org.http4s.{ HttpRoutes}
import org.http4s.dsl.Http4sDsl

object WeatherserverRoutes {
  def forecastRoutes[F[_]: Sync](forecasts: Forecasts[F], weatherApiKey: String): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    // query param matchers
    object LatParam extends OptionalQueryParamDecoderMatcher[Double]("lat")
    object LongParam extends OptionalQueryParamDecoderMatcher[Double]("long")
    object appIdParam extends OptionalQueryParamDecoderMatcher[String]("appid")

    def validateInputs(lat: Option[Double], long: Option[Double]): Boolean = {
      val notNone = (an: Option[Any]) =>
        an match {
          case Some(a) => true
          case None => false
        }

      notNone(lat) && notNone(long)
    }

    HttpRoutes.of[F] {
      case GET -> Root / "forecast" :? LatParam(lat) +& LongParam(long) +& appIdParam(appId)  =>
        validateInputs(lat, long) match {
          case true => for {
            forecast <- forecasts.get(lat.get, long.get, appId.getOrElse(weatherApiKey))
            resp <- Ok(forecast)
          } yield resp
          case false => BadRequest("Invalid Parameter!")
        }

    }
  }
}