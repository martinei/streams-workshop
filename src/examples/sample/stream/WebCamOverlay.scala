package sample.stream

import java.io.File

import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.stream.{MaterializerSettings, FlowMaterializer}
import video.imageUtils.ImageOverlay


object WebcamOverlay {

  /**
   * run:
   *    ./activator 'runMain sample.stream.WebcamOverlay'
   *
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = FlowMaterializer()
    val overlay = new ImageOverlay(new File("crosshairs-overlay.jpg"))
    val webcam = video.WebCam.cameraStreams(system).last
    val render = video.Display.create(system)
    Flow(webcam).map { frame =>
      overlay.overlayOnto(frame.image)
      frame
    }.produceTo(render)
  }
}