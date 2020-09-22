package payments.processor

import org.apache.flink.api.common.state.{ MapState, MapStateDescriptor }
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.util.Collector
import payments.datamodel._

class PaymentProcessingFunction extends CoProcessFunction[Participant, Payment, LogMessage] {
  @transient var state: MapState[String, Participant] = _

  override def open(params: Configuration): Unit = {
    super.open(params)
    state = getRuntimeContext.getMapState(new MapStateDescriptor[String, Participant]("state", classOf[String], classOf[Participant]))
  }

  override def processElement1(participant: Participant,
                               ctx: CoProcessFunction[Participant, Payment, LogMessage]#Context,
                               out: Collector[LogMessage]): Unit =
    state.put(participant.id, participant)

  override def processElement2(payment: Payment,
                               ctx: CoProcessFunction[Participant, Payment, LogMessage]#Context,
                               out: Collector[LogMessage]): Unit = payment match {
    case Payment(from, _, _) if !state.contains(from) => out.collect(LogMessage("участник не найден", from, "warn"))

    case Payment(_, to, _) if !state.contains(to) => out.collect(LogMessage("участник не найден", to, "warn"))

    case Payment(from, to, value) =>
      val fromParticipant = state.get(from)

      val withdrawnBalance = fromParticipant.balance - value
      if (withdrawnBalance >= 0) {
        state.put(from, fromParticipant.copy(balance = withdrawnBalance))

        val toParticipant = state.get(to)
        state.put(to, toParticipant.copy(balance = toParticipant.balance + value))

        out.collect(LogMessage("переведено", payment.toString, "info"))
      } else
        out.collect(LogMessage("нехватка средств", payment.toString, "warn"))
  }
}
