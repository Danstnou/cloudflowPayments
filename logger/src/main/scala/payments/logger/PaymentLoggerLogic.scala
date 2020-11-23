package payments.logger

import cloudflow.akkastream.{ AkkaStreamletContext, AkkaStreamletLogic }
import payments.datamodel.LogMessage

class PaymentLoggerLogic(streamlet: PaymentLoggerEgress)(implicit context: AkkaStreamletContext) extends AkkaStreamletLogic {
  override def run(): Unit =
    sourceWithCommittableContext(streamlet.in)
      .map {
        case LogMessage(reason, message, "info") => system.log.info(s"$reason: $message")
        case LogMessage(reason, message, "warn") => system.log.warning(s"$reason: $message")
        // посмотреть все варианты
      }
      .to(committableSink)
      .run
}
