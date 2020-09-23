package payments.initialize

import spray.json._
import payments.datamodel._

object ParticipantJsonProtocol extends DefaultJsonProtocol {
  val idField      = "id"
  val balanceField = "balance"

  implicit object ParticipantJsonFormat extends RootJsonFormat[Participant] {
    override def write(obj: Participant): JsValue = JsObject(idField -> JsString(obj.id), balanceField -> JsNumber(obj.balance))

    override def read(json: JsValue): Participant =
      json.asJsObject.getFields("id", "balance") match {
        case Seq(JsString(id), JsNumber(balance)) => Participant(id, balance.toLong)
        case _                                    => throw DeserializationException("Participant expected")
      }
  }
}
