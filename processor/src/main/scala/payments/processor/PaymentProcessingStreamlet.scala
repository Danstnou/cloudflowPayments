package payments.processor

import cloudflow.flink._
import cloudflow.streamlets.{StreamletShape, StringConfigParameter}
import payments.processor.settings.MessageSettings

class PaymentProcessingStreamlet extends FlinkStreamlet with PaymentProcessingShape with MessageSettings {

  @transient val shape: StreamletShape =
    StreamletShape
      .withInlets(inInitialize, inChecking)
      .withOutlets(out)

  override def configParameters: Vector[StringConfigParameter] = messageParameters

  override def createLogic() = new PaymentProcessingLogic(this)
}
