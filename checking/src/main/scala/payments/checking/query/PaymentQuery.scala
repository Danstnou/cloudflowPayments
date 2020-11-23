package payments.checking.query

object PaymentQuery {

  // сделать более универсально
  def insert(keySpace: String): String =
    s"INSERT INTO $keySpace.payment(id, fromParticipant, toParticipant, value, currency) " +
        "values (?, ?, ?, ?, ?);"

}
