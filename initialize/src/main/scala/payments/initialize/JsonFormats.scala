package payments.initialize

import spray.json._
import payments.datamodel._

object ParticipantJsonProtocol extends DefaultJsonProtocol {
  val idField       = "id"
  val balanceField  = "balance"
  val currencyField = "currency"

  implicit object ParticipantJsonFormat extends RootJsonFormat[Participant] {
    override def write(obj: Participant): JsValue =
      JsObject(idField -> JsString(obj.id), balanceField -> JsNumber(obj.balance), currencyField -> JsString(obj.currency))

    override def read(json: JsValue): Participant =
      json.asJsObject.getFields(idField, balanceField, currencyField) match {
        case Seq(JsString(id), JsNumber(balance), JsString(currency)) => Participant(id, balance.toLong, currency)
        case _                                                        => throw DeserializationException("Participant expected")
      }
  }
}
