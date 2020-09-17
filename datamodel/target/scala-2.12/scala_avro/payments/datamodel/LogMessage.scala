/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package payments.datamodel

import scala.annotation.switch

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
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"LogMessage\",\"namespace\":\"payments.datamodel\",\"fields\":[{\"name\":\"reason\",\"type\":\"string\"},{\"name\":\"message\",\"type\":\"string\"},{\"name\":\"level\",\"type\":\"string\"}]}")
}