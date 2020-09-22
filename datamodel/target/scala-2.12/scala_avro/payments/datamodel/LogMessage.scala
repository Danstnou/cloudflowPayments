/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package payments.datamodel

import scala.annotation.switch

/**
 * @param reason полезная информация
 * @param message сообщение из stream'а (событие)
 * @param level уровень логирования
 */
case class LogMessage(var reason: String, var message: String, var level: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this("", "", "")
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        reason
      }.asInstanceOf[AnyRef]
      case 1 => {
        message
      }.asInstanceOf[AnyRef]
      case 2 => {
        level
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.reason = {
        value.toString
      }.asInstanceOf[String]
      case 1 => this.message = {
        value.toString
      }.asInstanceOf[String]
      case 2 => this.level = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = LogMessage.SCHEMA$
}

object LogMessage {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"LogMessage\",\"namespace\":\"payments.datamodel\",\"fields\":[{\"name\":\"reason\",\"type\":\"string\",\"doc\":\"полезная информация\"},{\"name\":\"message\",\"type\":\"string\",\"doc\":\"сообщение из stream\'а (событие)\"},{\"name\":\"level\",\"type\":\"string\",\"doc\":\"уровень логирования\"}]}")
}