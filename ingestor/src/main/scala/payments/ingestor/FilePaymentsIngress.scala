package payments.ingestor

import java.nio.file.{ Files, Paths }

import akka.stream.scaladsl._
import akka.util.ByteString
import cloudflow.akkastream._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

import scala.jdk.CollectionConverters.asScalaIterator
import payments.ingestor.FilePaymentsIngress._
import payments.datamodel._

object FilePaymentsIngress {
  val delimiter          = "\r\n"
  val maximumFrameLength = 128
}

class FilePaymentsIngress extends AkkaStreamlet {
  val out                              = AvroOutlet[Transfer]("out")
  override def shape(): StreamletShape = StreamletShape.withOutlets(out)

  val CatalogParameter  = StringConfigParameter("catalog")
  val MaskFileParameter = StringConfigParameter("maskFile")

  override def configParameters = Vector(CatalogParameter, MaskFileParameter)

  final override def createLogic = new AkkaStreamletLogic() {
    val catalog  = CatalogParameter.value
    val maskFile = MaskFileParameter.value

    override def run(): Unit = {
      val files = Files
        .list(Paths.get(catalog))
        .filter(_.getFileName.toString.matches(maskFile))
        .iterator()

      Source
        .fromIterator(() => asScalaIterator(files))
        .flatMapConcat {
          FileIO.fromPath(_).via(Framing.delimiter(ByteString(delimiter), maximumFrameLength, allowTruncation = true))
        }
        .map(byteString => Transfer(byteString.utf8String))
        .to(plainSink(out))
        .run()
    }
  }
}
