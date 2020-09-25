package payments.initialize

import cloudflow.akkastream._
import cloudflow.akkastream.util.scaladsl._
import cloudflow.streamlets.avro._
import cloudflow.streamlets._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.common.EntityStreamingSupport
import ParticipantJsonProtocol._
import payments.datamodel._

class ParticipantInitializeIngress extends AkkaServerStreamlet {
  val out   = AvroOutlet[Participant]("out", RoundRobinPartitioner)
  def shape = StreamletShape.withOutlets(out)

  implicit val entityStreamingSupport = EntityStreamingSupport.json()
  override def createLogic            = HttpServerLogic.defaultStreaming(this, out)
}
