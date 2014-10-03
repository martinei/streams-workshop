package sample.stream

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import video.Frame
import java.io.File
import akka.stream.scaladsl.Flow
import org.reactivestreams.Publisher
import akka.stream.MaterializerSettings

object FrameCount {

  /**
   * run:
   *    ./activator 'runMain sample.stream.FrameCount file.mp4'
   *
   */
  
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = FlowMaterializer()
    val videoStream: Publisher[Frame] = video.FFMpeg.readFile(new File("goose.mp4"), system)
    Flow(videoStream).fold(0) { (count, frame) =>
      val nextCount = count + 1
      System.out.print(f"\rFRAME ${nextCount}%05d")
      nextCount
    }.onComplete {
        case _ => system.shutdown()    
    }
  }
}