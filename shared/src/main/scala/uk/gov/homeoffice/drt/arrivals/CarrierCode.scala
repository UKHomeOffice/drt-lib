package uk.gov.homeoffice.drt.arrivals

import upickle.default.{ macroRW, ReadWriter }

case class CarrierCode(code: String) {
  override def toString: String = code
}

object CarrierCode {
  implicit val rw: ReadWriter[CarrierCode] = macroRW
}
