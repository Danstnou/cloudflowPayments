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
          .process(new PaymentProcessingFunction())

      writeStream(out, stream)
    }
  }
}
