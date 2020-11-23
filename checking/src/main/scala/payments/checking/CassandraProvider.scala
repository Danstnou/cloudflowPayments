package payments.checking

import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.cassandra.CassandraSink

trait CassandraProvider {

  def getSinkCassandraWithHostAndPort[T](dataStream: DataStream[T], host: String, port: Int): CassandraSink.CassandraSinkBuilder[T] =
    getSinkCassandra(dataStream).setHost(host, port)

  def getSinkCassandra[T](dataStream: DataStream[T]): CassandraSink.CassandraSinkBuilder[T] =
    CassandraSink.addSink(dataStream)

}
