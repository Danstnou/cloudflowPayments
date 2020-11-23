package payments.checking

import cloudflow.flink.{ FlinkStreamletContext, FlinkStreamletLogic }
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.OutputTag
import payments.checking.functions.CheckingProcessFunction
import payments.checking.query.PaymentQuery
import payments.datamodel.LogMessage

import scala.util.matching.Regex

class PaymentCheckingLogic(streamlet: PaymentCheckingStreamlet)(implicit context: FlinkStreamletContext)
    extends FlinkStreamletLogic
    with CassandraProvider {
  val loggerTag: OutputTag[LogMessage] = OutputTag[LogMessage](streamlet.invalid.name)
  val maskPayment: Regex               = streamlet.MaskPaymentParameter.value.r

  override def buildExecutionGraph(): Unit = {
    val stream = readStream(streamlet.in)
      .process(new CheckingProcessFunction(maskPayment, loggerTag, streamlet.IncorrectPaymentMessageParameter.value))

    getSinkCassandraWithHostAndPort(stream, "127.0.0.1", 9042)
      .setQuery(PaymentQuery.insert("keyspace_payments"))
      .build()

    writeStream(streamlet.valid, stream)
    writeStream(streamlet.invalid, stream.getSideOutput(loggerTag))
  }

}
