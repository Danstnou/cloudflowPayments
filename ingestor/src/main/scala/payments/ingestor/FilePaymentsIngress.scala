package payments.ingestor

import cloudflow.akkastream._
import cloudflow.streamlets._
import payments.ingestor.settings.Settings

object FilePaymentsIngress {
  val delimiter          = "\r\n"
  val maximumFrameLength = 128
}

class FilePaymentsIngress extends AkkaStreamlet with FilePaymentsShape with Settings {
  override def shape(): StreamletShape =
    StreamletShape
      .withOutlets(out)

  override def configParameters: Vector[StringConfigParameter] = settings

  final override def createLogic: AkkaStreamletLogic = new FilePaymentsLogic(this)
}
