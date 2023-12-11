package events

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import services.shipment.model.{Parcel, Shipment}
import services.tracking.model.{TrackingBusinessModel, TrackingStatus}

class EventDispatcherSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  object StaticsFromBusinessReq{
    val aShipmentRef = "ABC123321"

    lazy val eventExpected: TrackingEvent => EventIssue => Boolean = tr => {
      case EmptyEvent          => false
      case TrackingEvent(r, s) => r == tr.reference && s == tr.status
    }
  }

  "EventDispatcher - Business Cases" should {

    """
       C0 :: Given the provided shipment When
       => shipment reference should be equal to tracking reference
       => shipment parcel number should be equal to tracking parcel number
       => shipment total weight should be less than tracking weight.
       => tracking status should be DELIVERED
       MUST RETURN TrackingCase.CONCILLIATION_REQUEST """ in {
      val aShipment = Shipment(
        reference = StaticsFromBusinessReq.aShipmentRef,
        parcels = List(
          Parcel(
            weight = 1,
            width = 12,
            height = 12,
            length = 12)
        )
      )
      val aTracking = TrackingBusinessModel(
        status = TrackingStatus.DELIVERED,
        parcels = Some(1),
        weight = Some(12),
        reference = aShipment.reference)

      val expectedEventForSuchaConditions =
        TrackingEvent(reference = aTracking.reference, status = TrackingCase.CONCILLIATION_REQUEST)
      val eventExpectedCurry = StaticsFromBusinessReq.eventExpected(expectedEventForSuchaConditions)
      val anEvents: Seq[EventIssue] = EventDispatcher.BusinessRules
        .standardEventIssueDispatcherStrategy
        .performCriterionEvaluation(aShipment, aTracking)

      val maybeResult: Seq[EventIssue] = anEvents.filter(eventExpectedCurry)
      maybeResult.head mustBe expectedEventForSuchaConditions
    }

    """
       C1 :: Given the provided shipment When
        => shipment reference should be equal to tracking reference.
        => shipment parcel number should be equal to tracking parcel number.
        => shipment total weight should be greater or equal than tracking weight.
        tracking status should be DELIVERED.
        MUST RETURN TrackingCase.NOT_NEEDED""" in {
      val aShipment = Shipment(
        reference = StaticsFromBusinessReq.aShipmentRef,
        parcels = List(
          Parcel(
            weight = 20,
            width = 12,
            height = 12,
            length = 12)
        )
      )
      val aTracking = TrackingBusinessModel(
        status = TrackingStatus.DELIVERED,
        parcels = Some(1),
        weight = Some(12),
        reference = aShipment.reference)

      val expectedEventForSuchaConditions =
        TrackingEvent(reference = aTracking.reference, status = TrackingCase.NOT_NEEDED)
      val eventExpectedCurry = StaticsFromBusinessReq.eventExpected(expectedEventForSuchaConditions)
      val anEvents: Seq[EventIssue] = EventDispatcher.BusinessRules
        .standardEventIssueDispatcherStrategy
        .performCriterionEvaluation(aShipment, aTracking)

      val maybeResult: Seq[EventIssue] = anEvents.filter(eventExpectedCurry)
      maybeResult.head mustBe expectedEventForSuchaConditions
    }

    """
      C2 :: Given the provided shipment When
       => shipment reference should be equal to tracking reference
       => tracking status is not DELIVERED
       MUST RETURN TrackingCase.INCOMPLETE""" in {
      val aShipment = Shipment(
        reference = StaticsFromBusinessReq.aShipmentRef,
        parcels = List(
          Parcel(
            weight = 20,
            width = 12,
            height = 12,
            length = 12)
        )
      )
      val aTracking = TrackingBusinessModel(
        status = TrackingStatus.WAITING_IN_HUB,
        parcels = Some(1),
        weight = Some(12),
        reference = aShipment.reference)

      val expectedEventForSuchaConditions =
        TrackingEvent(reference = aTracking.reference, status = TrackingCase.INCOMPLETE)
      val eventExpectedCurry = StaticsFromBusinessReq.eventExpected(expectedEventForSuchaConditions)
      val anEvents: Seq[EventIssue] = EventDispatcher.BusinessRules
        .standardEventIssueDispatcherStrategy
        .performCriterionEvaluation(aShipment, aTracking)

      val maybeResult: Seq[EventIssue] = anEvents.filter(eventExpectedCurry)
      maybeResult.head mustBe expectedEventForSuchaConditions
    }

    """
      C3 :: Given the provided shipment When
       => shipment reference should be equal to tracking reference
       => any other tracking field is null
       MUST RETURN TrackingCase.INCOMPLETE
    """ in {
      val aShipment = Shipment(
        reference = StaticsFromBusinessReq.aShipmentRef,
        parcels = List(
          Parcel(
            weight = 20,
            width = 12,
            height = 12,
            length = 12)
        )
      )
      val aTracking = TrackingBusinessModel(
        status = TrackingStatus.WAITING_IN_HUB,
        parcels = None,
        weight = Some(12),
        reference = aShipment.reference)

      val expectedEventForSuchaConditions =
        TrackingEvent(reference = aTracking.reference, status = TrackingCase.INCOMPLETE)
      val eventExpectedCurry = StaticsFromBusinessReq.eventExpected(expectedEventForSuchaConditions)
      val anEvents: Seq[EventIssue] = EventDispatcher.BusinessRules
        .standardEventIssueDispatcherStrategy
        .performCriterionEvaluation(aShipment, aTracking)

      val maybeResult: Seq[EventIssue] = anEvents.filter(eventExpectedCurry)
      maybeResult.head mustBe expectedEventForSuchaConditions
    }

    """
      C4 :: Given the provided shipment When
       => tracking reference is not equal toshipment reference
       MUST RETURN TrackingCase.NOT_FOUND
    """ in {
      val aShipment = Shipment(
        reference = StaticsFromBusinessReq.aShipmentRef,
        parcels = List(
          Parcel(
            weight = 20,
            width = 12,
            height = 12,
            length = 12)
        )
      )
      val aDifferentTrackingRefNo = "EFGH123456"
      val aTracking = TrackingBusinessModel(
        status = TrackingStatus.WAITING_IN_HUB,
        parcels = Some(12),
        weight = Some(12),
        reference = aDifferentTrackingRefNo)

      val expectedEventForSuchaConditions =
        TrackingEvent(reference = aTracking.reference, status = TrackingCase.NOT_FOUND)
      val eventExpectedCurry = StaticsFromBusinessReq.eventExpected(expectedEventForSuchaConditions)
      val anEvents: Seq[EventIssue] = EventDispatcher.BusinessRules
        .standardEventIssueDispatcherStrategy
        .performCriterionEvaluation(aShipment, aTracking)

      val maybeResult: Seq[EventIssue] = anEvents.filter(eventExpectedCurry)
      maybeResult.head mustBe expectedEventForSuchaConditions
    }
  }
}
