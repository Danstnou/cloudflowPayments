import cloudflow.flink.testkit._
import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.flink.streaming.api.scala._
import org.scalatest._
import payments.checking.PaymentCheckingStreamlet
import payments.datamodel._

import scala.collection.immutable.Seq

class PaymentCheckingStreamletSpec extends FlinkTestkit with WordSpecLike with Matchers with BeforeAndAfterAll {
  val conf = ConfigFactory.parseString(
    """
       cloudflow.streamlets.testFlinkStreamlet.maskPayment = "([A-Za-z0-9]+) (->) ([A-Za-z0-9]+) (:) ([0-9]+) ([A-Z]{3})",
       cloudflow.streamlets.testFlinkStreamlet.incorrectPaymentMessage = "некорректный платёж"
      """
  )

  override def config: Config = ConfigFactory.load(conf)

  @transient lazy val env = StreamExecutionEnvironment.getExecutionEnvironment

  "PaymentCheckingStreamlet" should {
    "test on correct Transfer" in {
      val checkingStreamlet = new PaymentCheckingStreamlet

      val correctTransfer   = Transfer("a1 -> b1 : 5 RUB")
      val incorrectTransfer = Transfer("a2  -> b2 : 10 RUB")
      val data              = Vector(correctTransfer, incorrectTransfer)

      val in      = inletAsTap[Transfer](checkingStreamlet.in, env.addSource(FlinkSource.CollectionSourceFunction(data)))
      val valid   = outletAsTap[Payment](checkingStreamlet.valid)
      val invalid = outletAsTap[LogMessage](checkingStreamlet.invalid)

      run(checkingStreamlet, Seq(in), Seq(valid, invalid), env)

      TestFlinkStreamletContext.result should (
        contain(Payment("a1", "b1", 5L, "RUB").toString)
          and not(contain(Payment("a2", "b2", 10L, "RUB").toString))
      )
    }
  }
}
