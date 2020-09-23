package payments.processor

import org.apache.flink.api.common.state.{ MapState, MapStateDescriptor }
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.util.Collector
import payments.processor.PaymentProcessingFunction._
import payments.datamodel._

object PaymentProcessingFunction {
  val nameState              = "state"
  val errorPaymentLevel      = "warn"
  val successfulPaymentLevel = "info"
}

class PaymentProcessingFunction(participantNotFoundMessage: String, successfulPaymentMessage: String, lackFundsMessage: String)
    extends CoProcessFunction[Participant, Payment, LogMessage] {
  @transient var state: MapState[String, Participant] = _

  override def open(params: Configuration): Unit = {
    super.open(params)
    state = getRuntimeContext.getMapState(new MapStateDescriptor[String, Participant](nameState, classOf[String], classOf[Participant]))
  }

  override def processElement1(participant: Participant,
                               ctx: CoProcessFunction[Participant, Payment, LogMessage]#Context,
                               out: Collector[LogMessage]): Unit = state.put(participant.id, participant)

  override def processElement2(payment: Payment,
                               ctx: CoProcessFunction[Participant, Payment, LogMessage]#Context,
                               out: Collector[LogMessage]): Unit = payment match {
    case Payment(from, _, _) if !state.contains(from) => out.collect(LogMessage(participantNotFoundMessage, from, errorPaymentLevel))

    case Payment(_, to, _) if !state.contains(to) => out.collect(LogMessage(participantNotFoundMessage, to, errorPaymentLevel))

    case Payment(from, to, value) =>
      val fromParticipant = state.get(from)

      val withdrawnBalance = fromParticipant.balance - value
      if (withdrawnBalance >= 0) {
        state.put(from, fromParticipant.copy(balance = withdrawnBalance))

        val toParticipant = state.get(to)
        state.put(to, toParticipant.copy(balance = toParticipant.balance + value))

        out.collect(LogMessage(successfulPaymentMessage, payment.toString, successfulPaymentLevel))
      } else
        out.collect(LogMessage(lackFundsMessage, payment.toString, errorPaymentLevel))
  }
}
