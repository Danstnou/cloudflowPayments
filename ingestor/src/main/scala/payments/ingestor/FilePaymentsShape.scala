package payments.ingestor

import cloudflow.streamlets.avro.AvroOutlet
import payments.datamodel.Transfer

trait FilePaymentsShape {

  val out: AvroOutlet[Transfer] =
    AvroOutlet[Transfer]("out")

}
