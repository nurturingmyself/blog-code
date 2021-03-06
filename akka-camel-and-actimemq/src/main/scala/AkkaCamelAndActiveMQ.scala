import akka.actor._
import akka.camel._
import org.apache.activemq.camel.component.ActiveMQComponent
import org.apache.activemq.ScheduledMessage._

case class Message(body: String)

class SimpleProducer() extends Actor with Producer with Oneway {
  def endpointUri: String = "activemq:foo.bar"
}

class SimpleConsumer() extends Actor with Consumer {
  def endpointUri: String = "activemq:foo.bar"

  def receive = {
    case msg: CamelMessage => println(msg)
  }
}


object AkkaCamelAndActiveMQ extends App {

  val actorSystem = ActorSystem("CamelTesting")
  val system = CamelExtension(actorSystem)

  val amqUrl = s"nio://localhost:61616"
  system.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))

  val simpleConsumer = actorSystem.actorOf(Props[SimpleConsumer])
  val simpleProducer = actorSystem.actorOf(Props[SimpleProducer])

  Thread.sleep(100) // wait for setup

  simpleProducer ! Message("first")
  simpleProducer ! Message("second")
  simpleProducer ! Message("third")

  val delayedMessage = CamelMessage(Message("delayed fourth"), Map(AMQ_SCHEDULED_DELAY -> 3000))
  simpleProducer ! delayedMessage

  Thread.sleep(5000) // wait for messages
  actorSystem.shutdown()
}
