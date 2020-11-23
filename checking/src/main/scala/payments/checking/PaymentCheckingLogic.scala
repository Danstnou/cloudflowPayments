package payments.checking

import cloudflow.flink.{ FlinkStreamletContext, FlinkStreamletLogic }
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.streaming.connectors.cassandra.CassandraSink
import payments.checking.functions.CheckingProcessFunction
import payments.datamodel.LogMessage

import scala.util.matching.Regex

class PaymentCheckingLogic(streamlet: PaymentCheckingStreamlet)(implicit context: FlinkStreamletContext) extends FlinkStreamletLogic {
  val loggerTag: OutputTag[LogMessage] = OutputTag[LogMessage](streamlet.invalid.name)
  val maskPayment: Regex               = streamlet.MaskPaymentParameter.value.r

  override def buildExecutionGraph(): Unit = {
    val stream = readStream(streamlet.in)
      .process(new CheckingProcessFunction(maskPayment, loggerTag, streamlet.IncorrectPaymentMessageParameter.value))

    CassandraSink
      .addSink(stream)
      .setHost("127.0.0.1", 9042)
      .setQuery(
        "INSERT INTO keyspace_payments.payment(id, fromParticipant, toParticipant, value, currency) " +
            "values (?, ?, ?, ?, ?);"
      )
      .build()

    writeStream(streamlet.valid, stream)
    writeStream(streamlet.invalid, stream.getSideOutput(loggerTag))
  }
}
