package payments.processor

import cloudflow.flink._
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro._
import org.apache.flink.api.common.state.{ MapState, MapStateDescriptor }
import org.apache.flink.streaming.api.functions.co._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import payments.datamodel._

class PaymentProcessingStreamlet extends FlinkStreamlet {
  @transient val inInitialize = AvroInlet[Participant]("in-postman")
  @transient val inChecking   = AvroInlet[Payment]("in-checking")
  @transient val out          = AvroOutlet[LogMessage]("out")

  @transient val shape: StreamletShape = StreamletShape.withInlets(inInitialize, inChecking).withOutlets(out)

  override def createLogic() = new FlinkStreamletLogic {
    override def buildExecutionGraph: Unit = {
      val stream: DataStream[LogMessage] =
        readStream(inInitialize)
          .connect(readStream(inChecking))
          .keyBy(_ => "myBank", _ => "myBank")
          .flatMap(new EnrichmentFunction())

      writeStream(out, stream)
    }
  }

  import org.apache.flink.configuration.Configuration
  class EnrichmentFunction extends RichCoFlatMapFunction[Participant, Payment, LogMessage] {
    @transient var state: MapState[String, Participant] = _

    override def open(params: Configuration): Unit = {
      super.open(params)
      state = getRuntimeContext.getMapState(new MapStateDescriptor[String, Participant]("state", classOf[String], classOf[Participant]))
    }

    override def flatMap1(participant: Participant, out: Collector[LogMessage]) =
      state.put(participant.id, participant)

    override def flatMap2(payment: Payment, out: Collector[LogMessage]) = payment match {
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
}

//case class Transfer(transfer: String)
//case class Payment(from: String, to: String, value: Long)
//case class Participant(id: String, balance: Long)
//case class LogMessage(reason: String, message: String, level: String)
