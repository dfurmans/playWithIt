package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.cache.SyncCacheApi
import play.api.libs.json.{Json}
import play.api.test._
import play.api.test.Helpers.{status, _}
import services.tracking.model.Tracking

class TrackingControllerSpec  extends PlaySpec with GuiceOneAppPerTest with Injecting {

  implicit lazy val fakeApp: Application = fakeApplication()

  val cache: SyncCacheApi = fakeApp.injector.instanceOf[SyncCacheApi]
  val aShipmentReferenceNumber = "ABCD123456"
  val fileName = "tracking"
  val fileExtension = ".json"
  val suffixes = Seq('A', 'B', 'C', 'D', 'E','F', 'G', 'H')
  val trackingFiles: Seq[String] = for (aSuffix <- suffixes)
    yield fileName+aSuffix.toString+fileExtension

  val aTrackingA = Tracking(
      status = "WAITING_IN_HUB",
      parcels = Some(2),
      weight = None,
      reference = "ABCD123456"
  )
  val aTrackingB = Tracking(
    status="WAITING_IN_HUB",
    parcels=Some(2),
    weight=Some(2),
    reference="ABCD123456"
  )
  val aTrackingC = Tracking(
      status="WAITING_IN_HUB",
      parcels=Some(1),
      weight=Some(15),
      reference="ABCD123456"
    )
  val aTrackingD = Tracking(
      status="WAITING_IN_HUB",
      parcels=Some(2),
      weight=Some(30),
      reference="ABCD123456"
    )
  val aTrackingE = Tracking(
      status="DELIVERED",
      parcels=Some(2),
      weight=Some(2),
      reference="ABCD123456"
    )
  val aTrackingF = Tracking(
      status="DELIVERED",
      parcels=Some(2),
      weight=Some(30),
      reference="ABCD123456"
    )
  val aTrackingG = Tracking(
      status="DELIVERED",
      parcels=Some(2),
      weight=Some(30),
      reference="EFGH123456"
    )
  val aTrackingH = Tracking(
    status="DELIVERED",
    parcels=None,
    weight=Some(30),
    reference="ABCD123456"
  )
  val listOfAllTracking: List[Tracking] = List(aTrackingA, aTrackingB, aTrackingC, aTrackingD, aTrackingE, aTrackingF, aTrackingG, aTrackingH)

  "TrackingController " should {

    "PUT a new Tracking event should EITHER save OP update the object " in {
      val controller = new TrackingController(cache, Helpers.stubControllerComponents())
      //val trackingData = HelpersForSpec.FileHelper.loadTestFileContent(trackingFiles.head)

      val response = controller.tracking.apply(
        FakeRequest(Helpers.PUT,
          "/api/push",
          FakeHeaders(),
          Json.toJson(listOfAllTracking.head))
      )

      val result = contentAsJson(response).as[Tracking]
      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      result.reference mustBe aShipmentReferenceNumber

    }
  }

}
