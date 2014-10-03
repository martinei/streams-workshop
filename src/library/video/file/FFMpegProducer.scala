package video.file

import java.io.File
import com.xuggle.mediatool.ToolFactory
import com.xuggle.mediatool.MediaListenerAdapter
import com.xuggle.mediatool.event.ICloseEvent
import com.xuggle.mediatool.event.IVideoPictureEvent
import com.xuggle.xuggler.Utils
import com.xuggle.xuggler.IError
import akka.actor.{ActorRef, ActorRefFactory, Props}
import akka.stream.actor.ActorPublisher
import video.Frame
import akka.stream.actor.ActorPublisherMessage
import org.reactivestreams.Publisher


case class FFMpegError(raw: IError) extends Exception(raw.getDescription)

private[video] class FFMpegProducer(file: File) extends ActorPublisher[Frame] {
   /** Open the reader. */
  private val reader = ToolFactory.makeReader(file.getAbsolutePath)
  
  private var closed: Boolean = false
  private var frameCount: Long = 0L
  
  /** Register a listener that will forward all events down the Reactive Streams chain. */
  reader.addListener(new MediaListenerAdapter() {
    override def onVideoPicture(e: IVideoPictureEvent): Unit = {
      if(e.getMediaData.isComplete) {
        onNext(Frame(Utils.videoPictureToImage(e.getMediaData), e.getTimeStamp, e.getTimeUnit))
        frameCount += 1
      }
    }
  })
  
  override def receive: Receive = {
    case ActorPublisherMessage.Request(elements) => read(elements)
    case ActorPublisherMessage.Cancel =>
      reader.close()
      context stop self
  }


  /** Our actual behavior. */  
  // Reads the given number of frames, or bails on error.`
  // Note: we have to track frames via the listener we have on the reader.
  private def read(frames: Long): Unit = {
    val done = frameCount + frames
    // Close event should automatically occur.
    while(!closed && frameCount < done) {
      try (reader.readPacket match {
        case null => // Ignore
        case error =>
          // Ensure we're closed.
          closed = true
          if(error.getType == IError.Type.ERROR_EOF) onComplete()
          else onError(FFMpegError(error))
      }) catch {
        // Some sort of fatal read error.
        case e: Exception =>
          closed = true
          onError(e)
      }
    }
  }
}

object FFMpegProducer {

  def make(factory: ActorRefFactory, file: File): ActorRef =
    factory.actorOf(Props(new FFMpegProducer(file)))
  
  def apply(factory: ActorRefFactory, file: File): Publisher[Frame] = 
    ActorPublisher(make(factory, file))
}