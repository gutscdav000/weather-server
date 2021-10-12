package com.jackhenry.weatherserver

import cats.effect.Concurrent
import cats.implicits._
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._
import org.http4s.headers.Accept
import io.circe.generic.auto._

trait Forecasts[F[_]]{
  def get(lat: Double, long: Double, appId: String): F[Forecasts.ForecastResponse]
}

object Forecasts {
  def apply[F[_]](implicit ev: Forecasts[F]): Forecasts[F] = ev

  final case class ForecastResponse(condition: String, temperature: Double, alert: Option[String])
  final case class Weather(id: Int, main: String, description: String, icon: String)
  final case class ForecastError(e: Throwable) extends RuntimeException

  object ForecastResponse {
    implicit val decoder: Decoder[ForecastResponse] = (hCursor: HCursor) =>
      for {
        weather <- hCursor.downField("weather").as[List[Weather]]
        condition = weather.headOption match {
          case Some(weath) => weath.main + ": "+ weath.description
          case None => "No Weather Condition Retrieved"
        }
        temperature <- hCursor.downField("main").downField("temp").as[Double]
        //    alert <- hCursor.get[String]("")
      } yield ForecastResponse(condition, temperature, None)
    implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, ForecastResponse] = jsonOf

    implicit val forecastEncoder: Encoder[ForecastResponse] = deriveEncoder[ForecastResponse]
    implicit def forecastEntityEncoder[F[_]]: EntityEncoder[F, ForecastResponse] =
      jsonEncoderOf
  }

  def impl[F[_]: Concurrent](C: Client[F]): Forecasts[F] = new Forecasts[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._
    def get(lat: Double, long: Double, appId: String): F[Forecasts.ForecastResponse] = {
      C.expect[ForecastResponse](GET(
        uri"https://api.openweathermap.org/data/2.5/weather"
          .withQueryParam("lat", lat)
          .withQueryParam("lon", long)
          .withQueryParam("appid", appId)
          .withQueryParam("units", "imperial")
        ,
        Accept(MediaType.application.json)
      ))
        .adaptError{ case t => {
          println("ERROR: " + t)
          throw ForecastError(t)
        }} // Prevent Client Json Decoding Failure Leaking
    }
  }
}