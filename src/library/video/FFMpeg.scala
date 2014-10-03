package video

import java.io.File
import com.xuggle.xuggler.IRational
import akka.actor.ActorRefFactory
import org.reactivestreams.Publisher

/** Helper for dealing with FFMpeg data. */
object FFMpeg {
  /** Reads a given file and pushes its stream events out. 
    * Note: This will not prefetch any data, but only read when requested.
    */
  def readFile(file: File, system: ActorRefFactory): Publisher[Frame] = 
    video.file.FFMpegProducer(system, file)
  /**
   * Writes a stream of frames to the given file as an FFMpeg.
   */
//  def writeFile(file: File, system: ActorRefFactory, width: Int, height: Int, frameRate: IRational = IRational.make(3, 1)): Consumer[Frame] = 
//    video.file.FFMpegFileConsumerWorker(system, file, width, height, frameRate)
}