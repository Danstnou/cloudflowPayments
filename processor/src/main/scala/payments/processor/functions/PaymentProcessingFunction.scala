package payments.processor.functions

import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import payments.datamodel._
import payments.processor.functions.PaymentProcessingFunction.{errorPaymentLevel, nameState, successfulPaymentLevel}

object PaymentProcessingFunction {
  val nameState              = "state"
  val errorPaymentLevel      = "warn"
  val successfulPaymentLevel = "info"
}

class PaymentProcessingFunction(participantNotFoundMessage: String, successfulPaymentMessage: String, lackFundsMessage: String)
    extends KeyedCoProcessFunction[String, Participant, Payment, LogMessage] {
  @transient var state: MapState[String, Participant] = _
  val log: Logger                                     = LoggerFactory.getLogger(getClass)

  override def open(params: Configuration): Unit = {
    super.open(params)
    state = getRuntimeContext.getMapState(new MapStateDescriptor[String, Participant](nameState, classOf[String], classOf[Participant]))
  }

  override def processElement1(participant: Participant,
                               ctx: KeyedCoProcessFunction[String, Participant, Payment, LogMessage]#Context,
                               out: Collector[LogMessage]): Unit = {
    state.put(participant.id, participant)
    out.collect(LogMessage("Добавлен участник: ", s"${participant.id}", successfulPaymentLevel))
  }

  override def processElement2(payment: Payment,
                               ctx: KeyedCoProcessFunction[String, Participant, Payment, LogMessage]#Context,
                               out: Collector[LogMessage]): Unit = payment match {
    case Payment(_, from, _, _, currency) if !state.contains(from) =>
      out.collect(LogMessage(participantNotFoundMessage, s"$from - [$currency]", errorPaymentLevel))

    case Payment(_, _, to, _, currency) if !state.contains(to) =>
      out.collect(LogMessage(participantNotFoundMessage, s"$to - [$currency]", errorPaymentLevel))

    case Payment(_, from, to, value, _) =>
      val fromParticipant = state.get(from)

      if (fromParticipant.balance >= value) {
        state.put(from, fromParticipant.copy(balance = fromParticipant.balance - value))

        val toParticipant = state.get(to)
        state.put(to, toParticipant.copy(balance = toParticipant.balance + value))

        out.collect(LogMessage(successfulPaymentMessage, payment.toString, successfulPaymentLevel))
      } else
        out.collect(LogMessage(lackFundsMessage, payment.toString, errorPaymentLevel))
  }
}
