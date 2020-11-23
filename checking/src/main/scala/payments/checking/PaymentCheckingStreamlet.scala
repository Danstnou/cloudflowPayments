package payments.checking

import cloudflow.flink.FlinkStreamlet
import cloudflow.streamlets._
import payments.checking.settings.Settings

class PaymentCheckingStreamlet extends FlinkStreamlet with PaymentCheckingShape with Settings {
  @transient val shape: StreamletShape =
    StreamletShape
      .withInlets(in)
      .withOutlets(valid, invalid)

  override def configParameters = settings

  override def createLogic() = new PaymentCheckingLogic(this)
}
