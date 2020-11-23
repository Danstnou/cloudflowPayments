package payments.processor

import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import payments.datamodel.{ LogMessage, Participant, Payment }

trait PaymentProcessingShape {

  @transient val inInitialize: AvroInlet[Participant] =
    AvroInlet[Participant]("in-initialize")
  @transient val inChecking: AvroInlet[Payment] =
    AvroInlet[Payment]("in-checking")
  @transient val out: AvroOutlet[LogMessage] =
    AvroOutlet[LogMessage]("out")

}
