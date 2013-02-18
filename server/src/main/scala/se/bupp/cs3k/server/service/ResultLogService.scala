package se.bupp.cs3k.server.service

import org.springframework.stereotype.Service
import java.io._

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-01
 * Time: 00:27
 * To change this template use File | Settings | File Templates.
 */
@Service
class ResultLogService {

  def write(s:String) {
    var fos:FileOutputStream = null
    var writer:PrintWriter = null

    try {
      val scoreLog = new File("score.log")
      fos = new FileOutputStream(scoreLog,true)
      writer = new PrintWriter(fos)
      writer.write(s)
      writer.write("<br class=\"score-separator\">")
      writer.close()
      fos.close()
    } finally {
      if (writer != null) writer.close()
      if (fos != null) fos.close()
    }
  }
  def read() = {
    var bufferedReader:BufferedReader = null
    var reader:FileReader = null

    var res = ""
    try {
      val scoreLog = new File("score.log")
      if (scoreLog.exists()) {
        reader = new FileReader(scoreLog)
        bufferedReader = new BufferedReader(reader)
        var line = ""
        while( { line = bufferedReader.readLine() ; line != null} ) {
          res += line
        }
      }
    } finally {
      if (bufferedReader != null) bufferedReader.close()
      if (reader != null) reader.close()
    }
    res
  }
}
