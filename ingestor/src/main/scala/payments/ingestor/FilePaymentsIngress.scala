package payments.ingestor

import java.nio.file.{ Files, Paths }

import akka.stream.scaladsl._
import akka.util.ByteString
import cloudflow.akkastream._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

import scala.jdk.CollectionConverters.asScalaIterator
import payments.datamodel._

class FilePaymentsIngress extends AkkaStreamlet {
  val out                              = AvroOutlet[Transfer]("out")
  override def shape(): StreamletShape = StreamletShape.withOutlets(out)

  val CatalogConf  = StringConfigParameter("catalog", "directory of files with payments.")
  val MaskFileConf = StringConfigParameter("maskFile", "file mask.")

  override def configParameters = Vector(CatalogConf, MaskFileConf)

  final override def createLogic = new AkkaStreamletLogic() {
    val catalog  = CatalogConf.value
    val maskFile = MaskFileConf.value

    override def run(): Unit = {
      val files = Files
        .list(Paths.get(catalog))
        .filter(_.getFileName.toString.matches(maskFile))
        .iterator()

      Source
        .fromIterator(() => asScalaIterator(files))
        .flatMapConcat {
          FileIO.fromPath(_).via(Framing.delimiter(ByteString("\r\n"), 128, allowTruncation = true))
        }
        .map(byteString => Transfer(byteString.utf8String))
        .to(plainSink(out))
        .run()
    }
  }
}
