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

  final case class ForecastResponse(condition: String, temperature: Double, alert: String)
  final case class Weather(id: Int, main: String, description: String, icon: String)
  final case class Alert(sender_name: String, event: String, start: Int, end: Int, description: String, tags: List[String])
  final case class ForecastError(e: Throwable) extends RuntimeException

  object ForecastResponse {
    implicit val decoder: Decoder[ForecastResponse] = (hCursor: HCursor) =>
      for {
        weather <- hCursor.downField("current").downField("weather").as[List[Weather]]
        condition = weather.headOption match {
          case Some(weath) => weath.main + ": "+ weath.description
          case None => "No Weather Condition Retrieved"
        }
        temperature <- hCursor.downField("current").downField("temp").as[Double]
        alerts <- hCursor.downField("alerts").as[Option[List[Alert]]]
        parsedAlerts: String = alerts match {
          case Some(als) => als.map(a => a.sender_name + ": " + a.event).reduce((s1, s2) => s1 + "\n" + s2)
          case None => "No current weather Alerts"
        }
      } yield ForecastResponse(condition, temperature, parsedAlerts)
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
        uri"https://api.openweathermap.org/data/2.5/onecall"
          .withQueryParam("lat", lat)
          .withQueryParam("lon", long)
          .withQueryParam("appid", appId)
          .withQueryParam("units", "imperial")
          .withQueryParam("exclude", "minutely,hourly,daily")
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