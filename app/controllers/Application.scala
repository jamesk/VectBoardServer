package controllers

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import play.mvc._
import java.util._
import java.util.concurrent.Future
import org.apache.commons.io.IOUtils
import play.api.mvc.Action
import play.api.mvc.Controller
import org.apache.commons.io.FileUtils
import scala.sys.process._
import play.api.libs.json._
import scala.io.Source
import play.api._
import play.api.mvc._
import play.api.Play._

object ImageDetails {
  implicit val imageDetailsFormat = Json.format[ImageDetails]
}
case class ImageDetails(width: Int, height: Int)

object Application extends Controller {
  def index() = Action {
    Ok(views.html.Application.index.render())
  }
  
  def upload(qqfile: String, greyThreshold: Double, highpassFilter: Int, turd: Double) = Action { request =>
    //var moveTo: FileOutputStream = null

    Logger.info(s"Name of the file $qqfile")
    // Another way I used to grab the name of the file
    val filename = qqfile

    Logger.info(s"Absolute on where to send ${Play.getFile("").getAbsolutePath() + File.separator + "uploads" + File.separator}");
    //try {
      val data = request.body.asRaw.map(r => r.asFile).get //TODO: fix get

      val file = getFile(filename)
      
      FileUtils.copyFile(data, file)
      
      val imageData = boofThings(file)
      
      val outputPath = processImage(file, greyThreshold, highpassFilter, turd)
      /*
      moveTo = new FileOutputStream(file)

      IOUtils.copy(data, moveTo)
*/
      //conversion here using file 
      
    /*} catch {
      case ex: Throwable ⇒
        // catch file exception
        // catch IO Exception later on
        //renderJSON("{success: false}")
        
    }*/

    //renderJSON("{success: true}")
    Ok(Json.obj(
      "outputPath" -> JsString(routes.Application.getUpload(filename + ".svg").path),
      "data" -> JsString(Source.fromFile(outputPath).mkString),
      "imageDetails" -> Json.toJson(imageData)
    ))
  }

  def boofThings(file: File): ImageDetails = {
    val image = boofcv.io.image.UtilImageIO.loadImage(file.getAbsolutePath)
    
    ImageDetails(image.getWidth, image.getHeight)
  }
  
  private def getFile(filename: String) = 
    new File(Play.application.getFile("").getAbsolutePath() + File.separator + "uploads" + File.separator + filename)
  
  private def processImage(file: File, greyThreshold: Double = 0.48, highpassFilter: Int = 10, turd: Double = 1) = {
    val outputPath = s"${file.getAbsolutePath}.svg"
    
    val imageData = boofThings(file)
      
    val turdConstant = 0.00002
    val area = imageData.width * imageData.height
    val baseTurd = turdConstant * area
    
    val finalTurd = (turd * baseTurd).toInt
    
    //TODO: protect against injection attacks
    val pipelineCmd = s"convert ${file.getAbsolutePath} bmp:-" #| s"mkbitmap -b 1 -x -f $highpassFilter -t $greyThreshold -o -" #| s"potrace --svg --tight -t $finalTurd -o $outputPath"
    Logger.info(s"Running pipeline with command: $pipelineCmd")
    
    pipelineCmd.!!
    
    outputPath
  }
  
  def reprocess(filename: String, greyThreshold: Double, highpassFilter: Int, turd: Double) = Action {
    val file = getFile(filename)
    
    val outputPath = processImage(file, greyThreshold, highpassFilter, turd)
    
    Ok(Json.obj("data" -> JsString(Source.fromFile(outputPath).mkString)))
  }
  
  def getUpload(filename: String) = Action { request =>
    val fileToServe = getFile(filename)
    
    if (fileToServe.exists)
      Ok.sendFile(fileToServe, inline = true)
    else
      NotFound
  }
}
