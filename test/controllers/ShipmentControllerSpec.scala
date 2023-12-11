package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc.{PlayBodyParsers, Result}
import play.api.test._
import play.api.test.Helpers.{status, _}
import services.shipment.model.{Parcel, Shipment}

import scala.concurrent.Future


class ShipmentControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  implicit lazy val fakeApp: Application = fakeApplication()

  private val parser = fakeApp.injector.instanceOf[PlayBodyParsers]
  val cache: SyncCacheApi = fakeApp.injector.instanceOf[SyncCacheApi]
  val aShipmentReferenceNumber = "ABCD123456"
  val aParcels: List[Parcel] = List(
    Parcel(
      weight = 1,
      width = 10,
      height = 10,
      length = 10
    ),
    Parcel(
      weight = 2,
      width = 20,
      height = 20,
      length = 20
    ),
  )

  private val aShipment = Shipment(reference = aShipmentReferenceNumber, parcels = aParcels)

  "ShipmentController " should{

    "register a shipment should persist and return a valid Shipment object" in {

      val controller = new ShipmentController(
        Helpers.stubControllerComponents(), cache, parser
      )

      val response = controller.register.apply(
        FakeRequest(POST,
          "/api/register",
          FakeHeaders(),
          Json.toJson(aShipment))
      )

      val result = contentAsJson(response).as[Shipment]

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      result.reference mustBe aShipmentReferenceNumber
      result.parcels.size must be(2)

    }

    "find should return a Shipment register object " in {
      import scala.concurrent.duration.Duration
      import scala.concurrent.duration
      val cacheIntervalTimeout = 24
      cache.set(
        Shipment.getClass.getSimpleName + aShipment.reference,
        aShipment,
        Duration(cacheIntervalTimeout, duration.DAYS)
      )
      val controller = new ShipmentController(
        Helpers.stubControllerComponents(), cache, parser
      )
      val response: Future[Result] = controller
        .find(aShipmentReferenceNumber)
        .apply(FakeRequest(GET,
          s"/api/shipment/find/$aShipmentReferenceNumber"
        ))

       status(response) mustBe OK
    }
  }
}
