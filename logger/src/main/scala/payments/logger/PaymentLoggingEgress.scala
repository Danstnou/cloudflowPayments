package payments.logger

import cloudflow.akkastream._
import cloudflow.akkastream.util.scaladsl.Merger
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import payments.datamodel._

class PaymentLoggingEgress extends AkkaStreamlet {
  val in                    = AvroInlet[LogMessage]("in")
  val shape: StreamletShape = StreamletShape(in)

  override protected def createLogic(): AkkaStreamletLogic = new AkkaStreamletLogic() {
    override def run(): Unit =
      sourceWithCommittableContext(in)
        .map {
          case LogMessage(reason, message, "info") => system.log.info(s"$reason: $message")
          case LogMessage(reason, message, "warn") => system.log.warning(s"$reason: $message")
        }
        .to(committableSink)
        .run
  }
}
