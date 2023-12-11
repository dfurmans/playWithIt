package events

import services.shipment.model.Shipment
import services.tracking.model.{TrackingBusinessModel, TrackingStatus}

import scala.collection.immutable.Queue

object EventDispatcher {

  object BusinessRules {

    val standardEventIssueDispatcherStrategy: EventIssueDispatchCriterion = new EventIssueDispatchCriterion {}

    trait EventIssueDispatchCriterion {

      import TrackingStatus._

      type CriteriaType = (Shipment, TrackingBusinessModel) => Queue[EventIssue]
      val criterions: CriteriaType =
        (aShipment, aTrackingBusiness) => {
          val C0 = {
            aTrackingBusiness.status match {
              case DELIVERED if aShipment.reference != aTrackingBusiness.reference =>
                TrackingEvent(aTrackingBusiness.reference, TrackingCase.NOT_FOUND)
              case DELIVERED if aShipment.reference == aTrackingBusiness.reference &&
                aShipment.parcels.length == aTrackingBusiness.parcels.getOrElse(0) &&
                aShipment.parcels.foldRight(0)((x, y) => x.weight + y) < aTrackingBusiness.weight.getOrElse(0) => TrackingEvent(aTrackingBusiness.reference, TrackingCase.CONCILLIATION_REQUEST)
              case _ => EmptyEvent
            }
          }
          val C1 = {
            aTrackingBusiness.status match {
              case DELIVERED if aShipment.reference == aTrackingBusiness.reference &&
                aShipment.parcels.length == aTrackingBusiness.parcels.getOrElse(0) &&
                aShipment.parcels.foldRight(0)((x, y) => x.weight + y) >= aTrackingBusiness.weight.getOrElse(0) => TrackingEvent(aTrackingBusiness.reference, TrackingCase.NOT_NEEDED)
              case _ => EmptyEvent
            }
          }
          val C2 = {
            aTrackingBusiness.status match {
              case WAITING_IN_HUB | UNKNOWN if aShipment.reference == aTrackingBusiness.reference =>
                aTrackingBusiness match {
                  case TrackingBusinessModel(_, None, _, _) => TrackingEvent(aTrackingBusiness.reference, TrackingCase.INCOMPLETE)
                  case TrackingBusinessModel(_, _, None, _) => TrackingEvent(aTrackingBusiness.reference, TrackingCase.INCOMPLETE)
                  case TrackingBusinessModel(_, _, _, _)    => TrackingEvent(aTrackingBusiness.reference, TrackingCase.INCOMPLETE)
                  case null                                 => EmptyEvent
                }
              case _ => EmptyEvent
            }
          }
          val C3 = {
            aTrackingBusiness.status match {
              case WAITING_IN_HUB | UNKNOWN |DELIVERED if aTrackingBusiness.reference != aShipment.reference =>
                TrackingEvent(aTrackingBusiness.reference, TrackingCase.NOT_FOUND)
              case _ => EmptyEvent
            }
          }
          // in any other case - aka the Business rule
          val C4 = EmptyEvent
          Queue(C0, C1, C2, C3, C4)
        }

      private def evaluateCriterionDefinition(a: Shipment, t: TrackingBusinessModel): CriteriaType => Queue[EventIssue] = listOfCriterion => {
        listOfCriterion.apply(a, t)
      }

      def performCriterionEvaluation(a: Shipment, t: TrackingBusinessModel): Queue[EventIssue] = {
        this.evaluateCriterionDefinition(a, t)(this.criterions)
      }

    }

  }

}
