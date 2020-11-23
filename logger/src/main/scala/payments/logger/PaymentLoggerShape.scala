package payments.logger

import cloudflow.streamlets.avro.AvroInlet
import payments.datamodel.LogMessage

trait PaymentLoggerShape {

  val in = AvroInlet[LogMessage]("in")

}
