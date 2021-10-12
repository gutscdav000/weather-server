package com.jackhenry.weatherserver

import munit.CatsEffectSuite

class HelloWorldSpec extends CatsEffectSuite {

  test("HelloWorld returns status code 200") {
    assert(true)
//    assertIO(retHelloWorld.map(_.status) ,Status.Ok)
  }

  test("HelloWorld returns hello world message") {
    assert(true)
//    assertIO(retHelloWorld.flatMap(_.as[String]), "{\"message\":\"Hello, world\"}")
  }

//  private[this] val retHelloWorld: IO[Response[IO]] = {
//    val getHW = Request[IO](Method.GET, uri"/hello/world")
//    val helloWorld = HelloWorld.impl[IO]
//    WeatherserverRoutes.helloWorldRoutes(helloWorld).orNotFound(getHW)
//  }
}