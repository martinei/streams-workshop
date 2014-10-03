package sample.stream

import akka.actor.ActorSystem
import akka.stream.{FlowMaterializer, MaterializerSettings}
import java.io.File
import akka.stream.scaladsl.Flow
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import video._

object ManipulateVideo {

  /**
   * run:
   *   ./activator 'runMain sample.stream.ManipulateVideo goose.mp4'
   *
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = FlowMaterializer()
    
    val fileProducer: Publisher[Frame] = video.FFMpeg.readFile(new File(args(0)), system)
    val flow = Flow(fileProducer)
    val videoConsumer: Subscriber[Frame] = video.Display.create(system)
    flow.map(frame => Frame(ConvertImage.addWaterMark(frame.image), frame.timeStamp, frame.timeUnit))
          .map(frame => Frame(ConvertImage.invertImage(frame.image), frame.timeStamp, frame.timeUnit))
          .produceTo(videoConsumer)
  }
}