package payments.checking

import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import payments.datamodel.{ LogMessage, Payment, Transfer }

trait PaymentCheckingShape {

  @transient val in: AvroInlet[Transfer] =
    AvroInlet[Transfer]("in")
  @transient val valid: AvroOutlet[Payment] =
    AvroOutlet[Payment]("out-processor")
  @transient val invalid: AvroOutlet[LogMessage] =
    AvroOutlet[LogMessage]("out-logger")

}
