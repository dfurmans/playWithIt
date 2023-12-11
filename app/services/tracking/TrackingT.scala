package services.tracking

import services.tracking.model.Tracking

trait TrackingT[M[_]]{
  def findByRef(refNumber: String): M[Option[Tracking]]
  def createOrUpdate(aTracking: Tracking) : M[Tracking]
}
