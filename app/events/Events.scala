package events

sealed trait TrackingCase
object TrackingCase{
  case object CONCILLIATION_REQUEST extends TrackingCase
  case object NOT_NEEDED extends TrackingCase
  case object INCOMPLETE extends TrackingCase
  case object NOT_FOUND extends TrackingCase
}
sealed trait EventIssue
case object EmptyEvent extends EventIssue
final case class TrackingEvent(reference: String, status: TrackingCase) extends EventIssue
object TrackingEvent{
  def renderEventAsString(anEvent: EventIssue) : String = {
    def print(anEvent: TrackingEvent): String = {
      s"""
      {
       "reference": "${anEvent.reference}"
       "status": "${anEvent.status.toString.toUpperCase}"
      }
      """.stripMargin
    }
    anEvent match {
      case EmptyEvent                            => ""
      case wholeEvent@TrackingEvent(ref, status) => print(wholeEvent)
    }

  }
}

