package payments.ingestor

import java.nio.file.FileSystems

import akka.stream.alpakka.file.DirectoryChange
import akka.stream.alpakka.file.scaladsl.DirectoryChangesSource
import akka.stream.scaladsl._
import akka.util.ByteString
import cloudflow.akkastream._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import payments.ingestor.FilePaymentsIngress._
import payments.datamodel._

import scala.concurrent.duration.DurationInt

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
      val fs = FileSystems.getDefault
      val changes = DirectoryChangesSource(fs.getPath(catalog), pollInterval = 100.millisecond, maxBufferSize = 1000)
        .filter(pair => pair._2 == DirectoryChange.Creation && pair._2 != DirectoryChange.Deletion)
        .map(_._1)

      changes
        .filter(_.getFileName.toString.matches(maskFile))
        .flatMapConcat {
          FileIO.fromPath(_).via(Framing.delimiter(ByteString(delimiter), maximumFrameLength, allowTruncation = true))
        }
        .map(byteString => Transfer(byteString.utf8String))
        .to(plainSink(out))
        .run()
    }
  }
}
