package payments.checking

import cloudflow.flink.{ FlinkStreamlet, FlinkStreamletLogic }
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import org.apache.flink.streaming.api.scala.{ createTypeInformation, OutputTag }
import payments.datamodel._

import scala.util.matching.Regex

class PaymentCheckingStreamlet extends FlinkStreamlet {
  @transient val in                    = AvroInlet[Transfer]("in")
  @transient val valid                 = AvroOutlet[Payment]("out-processor")
  @transient val invalid               = AvroOutlet[LogMessage]("out-logger")
  @transient val shape: StreamletShape = StreamletShape.withInlets(in).withOutlets(valid, invalid)

  val MaskPaymentParameter             = StringConfigParameter("maskPayment")
  val IncorrectPaymentMessageParameter = StringConfigParameter("incorrectPaymentMessage")

  override def configParameters = Vector(MaskPaymentParameter, IncorrectPaymentMessageParameter)

  override def createLogic() = new FlinkStreamletLogic {
    val loggerTag: OutputTag[LogMessage] = OutputTag[LogMessage](invalid.name)
    val maskPayment: Regex               = MaskPaymentParameter.value.r

    override def buildExecutionGraph(): Unit = {
      val stream = readStream(in)
        .process(new CheckingProcessFunction(maskPayment, loggerTag, IncorrectPaymentMessageParameter.value))

      writeStream(valid, stream)
      writeStream(invalid, stream.getSideOutput(loggerTag))
    }
  }
}
