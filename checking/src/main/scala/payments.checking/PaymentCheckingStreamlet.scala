package payments.checking

import cloudflow.flink.{ FlinkStreamlet, FlinkStreamletLogic }
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{ createTypeInformation, OutputTag }
import org.apache.flink.util.Collector
import payments.datamodel._

import scala.util.matching.Regex

class PaymentCheckingStreamlet extends FlinkStreamlet {

  @transient val in                    = AvroInlet[Transfer]("in")
  @transient val valid                 = AvroOutlet[Payment]("out-processor")
  @transient val invalid               = AvroOutlet[LogMessage]("out-logger")
  @transient val shape: StreamletShape = StreamletShape.withInlets(in).withOutlets(valid, invalid)

  val MaskPaymentConf = StringConfigParameter("maskPayment", "payment mask.")

  override def configParameters = Vector(MaskPaymentConf)

  override def createLogic() = new FlinkStreamletLogic {
    val processorTag = OutputTag[Payment](valid.name)
    val loggerTag    = OutputTag[LogMessage](invalid.name)

    val maskPayment: Regex = MaskPaymentConf.value.r

    override def buildExecutionGraph(): Unit = {
      val stream = readStream(in)
        .process { (transfer: Transfer, ctx: ProcessFunction[Transfer, LogMessage]#Context, _: Collector[LogMessage]) =>
          transfer match {
            case Transfer(maskPayment(from, _, to, _, amount)) =>
              ctx.output(processorTag, Payment(from, to, amount.toLong))

            case _ => ctx.output(loggerTag, LogMessage("некорректный платёж", transfer.toString, "warn"))
          }
        }

      writeStream(valid, stream.getSideOutput(processorTag))
      writeStream(invalid, stream.getSideOutput(loggerTag))
    }
  }
}
