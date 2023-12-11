package services.tracking.model

trait Converter[A, B] {
  def convert: A => B
}

object TrackingImplicits {
  implicit val convertTracking: Converter[Tracking, TrackingBusinessModel] = new Converter[Tracking, TrackingBusinessModel] {
    def convert: Tracking => TrackingBusinessModel = aTracking => {
      TrackingBusinessModel.apply(aTracking)
    }
  }
}