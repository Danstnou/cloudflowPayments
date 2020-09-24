package payments.checking

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector
import payments.checking.CheckingProcessFunction._
import payments.datamodel._

import scala.util.matching.Regex

object CheckingProcessFunction {
  val incorrectPaymentLevel = "warn"
}

class CheckingProcessFunction(maskPayment: Regex, loggerTag: OutputTag[LogMessage], incorrectPaymentMessage: String)
    extends ProcessFunction[Transfer, Payment] {
  override def processElement(transfer: Transfer, ctx: ProcessFunction[Transfer, Payment]#Context, out: Collector[Payment]): Unit =
    transfer match {
      case Transfer(maskPayment(from, _, to, _, amount, currency)) => out.collect(Payment(from, to, amount.toLong, currency))
      case _                                                       => ctx.output(loggerTag, LogMessage(incorrectPaymentMessage, transfer.toString, incorrectPaymentLevel))
    }
}
