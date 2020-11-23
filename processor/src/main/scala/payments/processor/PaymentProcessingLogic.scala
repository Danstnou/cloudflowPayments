package payments.processor

import cloudflow.flink.{ FlinkStreamletContext, FlinkStreamletLogic }
import org.apache.flink.streaming.api.scala._
import payments.datamodel.LogMessage
import payments.processor.functions.PaymentProcessingFunction

class PaymentProcessingLogic(streamlet: PaymentProcessingStreamlet)(implicit context: FlinkStreamletContext) extends FlinkStreamletLogic {

  override def buildExecutionGraph: Unit = {
    val stream: DataStream[LogMessage] =
      readStream(streamlet.inInitialize)
        .connect(readStream(streamlet.inChecking))
        .keyBy(_.currency, _.currency)
        .process(
          new PaymentProcessingFunction(streamlet.ParticipantNotFoundMessageParameter.value,
                                        streamlet.SuccessfulPaymentMessageParameter.value,
                                        streamlet.LackFundsMessageParameter.value)
        )

    writeStream(streamlet.out, stream)
  }

}
