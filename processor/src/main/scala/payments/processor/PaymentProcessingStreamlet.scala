package payments.processor

import cloudflow.flink._
import cloudflow.streamlets.avro._
import cloudflow.streamlets.{ StreamletShape, StringConfigParameter }
import org.apache.flink.streaming.api.scala._
import payments.datamodel._

class PaymentProcessingStreamlet extends FlinkStreamlet {
  @transient val inInitialize          = AvroInlet[Participant]("in-initialize")
  @transient val inChecking            = AvroInlet[Payment]("in-checking")
  @transient val out                   = AvroOutlet[LogMessage]("out")
  @transient val shape: StreamletShape = StreamletShape.withInlets(inInitialize, inChecking).withOutlets(out)

  val ParticipantNotFoundMessageParameter = StringConfigParameter("participantNotFoundMessage")
  val SuccessfulPaymentMessageParameter   = StringConfigParameter("successfulPaymentMessage")
  val LackFundsMessageParameter           = StringConfigParameter("lackFundsMessage")

  override def configParameters = Vector(ParticipantNotFoundMessageParameter, SuccessfulPaymentMessageParameter, LackFundsMessageParameter)

  override def createLogic() = new FlinkStreamletLogic {
    override def buildExecutionGraph: Unit = {
      val stream: DataStream[LogMessage] =
        readStream(inInitialize)
          .connect(readStream(inChecking))
          .keyBy(_.currency, _.currency)
          .process(
            new PaymentProcessingFunction(ParticipantNotFoundMessageParameter.value,
                                          SuccessfulPaymentMessageParameter.value,
                                          LackFundsMessageParameter.value)
          )

      writeStream(out, stream)
    }
  }
}
