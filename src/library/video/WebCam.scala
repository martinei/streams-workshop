package video

import com.github.sarxos.webcam.{Webcam=>WC}
import java.util.concurrent.TimeUnit
import akka.actor.Props
import collection.JavaConverters._
import akka.actor.ActorSystem
import akka.actor.ActorRefFactory
import org.reactivestreams.Publisher
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage

object WebCam {

  def default(system: ActorRefFactory): Publisher[Frame] = 
    cameraStream(system)(WC.getDefault)

  def cameraStreams(system: ActorSystem): Seq[Publisher[Frame]] =
    WC.getWebcams.asScala map cameraStream(system)
    
  private def cameraStream(system: ActorRefFactory)(cam: WC): Publisher[Frame] =
    ActorPublisher(system.actorOf(WebCamProducer.props(cam)))
}

object WebCamProducer {
  def props(cam: WC): Props = Props(new WebCamProducer(cam))
}

/** An actor which reads the given file on demand. */
private[video] class WebCamProducer(cam: WC) extends ActorPublisher[Frame] {
    /** Our actual behavior. */
  override def receive: Receive = {
    case ActorPublisherMessage.Request(elements) => 
      while(totalDemand > 0) onNext(snap())
    case ActorPublisherMessage.Cancel => cam.close()
        context stop self
  }
  
  // Grab a webcam snapshot.
  def snap(): Frame = {
     if(!cam.isOpen) cam.open()
     Frame(cam.getImage, System.nanoTime, TimeUnit.NANOSECONDS)
  }
}
