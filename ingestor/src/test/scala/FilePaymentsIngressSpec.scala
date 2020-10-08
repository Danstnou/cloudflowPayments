import java.io.{ File, PrintWriter }
import java.nio.file.Paths

import FilePaymentsIngressSpec._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import cloudflow.akkastream.testkit.scaladsl.{ AkkaStreamletTestKit, Completed, ConfigParameterValue }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import payments.ingestor.FilePaymentsIngress
import payments.datamodel._

object FilePaymentsIngressSpec {
  private val testCatalog  = "C:\\Users\\danel\\Music\\files"
  private val testMaskFile = "file[0-9]+.txt"
  private val nameTestFile = "file1.txt"
  private val testFile     = Paths.get(testCatalog, nameTestFile).toString
}

class FilePaymentsIngressSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {
  private implicit val system = ActorSystem("AkkaStreamletSpec")
  private implicit val mat    = ActorMaterializer()

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  def writeTestFile(path: String, strings: String*) = {
    val outFile = new PrintWriter(path)
    for (str <- strings) outFile.println(str)
    outFile.close()
  }

  def deleteFile(path: String) = new File(path).delete()

  "A FilePaymentsIngress" should {
    val ingress = new FilePaymentsIngress()

    val testkit = AkkaStreamletTestKit(system)
      .withConfigParameterValues(
        ConfigParameterValue(ingress.CatalogParameter, s""""$testCatalog"""".replace("""\""", """\\""")),
        ConfigParameterValue(ingress.MaskFileParameter, s""""$testMaskFile"""")
      )

    "test on create Transfer" in {
      val transfer     = "a1 -> b1 : 5 RUB"
      val expectedData = Vector(Transfer(transfer))
      val out          = testkit.outletAsTap(ingress.out)

      testkit.run(
        ingress,
        out,
        () => {
          writeTestFile(testFile, transfer)
          out.probe.receiveN(1) mustBe expectedData.map(t => ingress.out.partitioner(t) -> t)
          deleteFile(testFile)
        }
      )

      out.probe.expectMsg(Completed)
    }
  }
}
