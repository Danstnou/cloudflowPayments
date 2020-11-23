package payments.ingestor

import java.nio.file.FileSystems

import akka.stream.alpakka.file.DirectoryChange
import akka.stream.alpakka.file.scaladsl.DirectoryChangesSource
import akka.stream.scaladsl.{ FileIO, Framing }
import akka.util.ByteString
import cloudflow.akkastream.{ AkkaStreamletContext, AkkaStreamletLogic }
import payments.datamodel.Transfer
import payments.ingestor.FilePaymentsIngress.{ delimiter, maximumFrameLength }

import scala.concurrent.duration.DurationInt

class FilePaymentsLogic(streamlet: FilePaymentsIngress)(implicit context: AkkaStreamletContext) extends AkkaStreamletLogic {
  val catalog  = streamlet.CatalogParameter.value
  val maskFile = streamlet.MaskFileParameter.value

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
      .to(plainSink(streamlet.out))
      .run()
  }
}
