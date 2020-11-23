package payments.logger

import cloudflow.akkastream._
import cloudflow.streamlets._

class PaymentLoggerEgress extends AkkaStreamlet with PaymentLoggerShape {
  val shape: StreamletShape = StreamletShape(in)

  override protected def createLogic(): AkkaStreamletLogic = new PaymentLoggerLogic(this)

}
