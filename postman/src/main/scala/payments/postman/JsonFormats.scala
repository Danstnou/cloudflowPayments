package payments.postman

import java.util.UUID

import spray.json._
import payments.datamodel._

object ParticipantJsonProtocol extends DefaultJsonProtocol {
  implicit object ParticipantJsonFormat extends RootJsonFormat[Participant] {
    override def write(obj: Participant): JsValue = JsObject("id" -> JsString(obj.id), "balance" -> JsNumber(obj.balance))

    override def read(json: JsValue): Participant =
      json.asJsObject.getFields("id", "balance") match {
        case Seq(JsString(id), JsNumber(balance)) => Participant(id, balance.toLong)
        case _                                    => throw DeserializationException("TaxiFare expected")
      }
  }
}
