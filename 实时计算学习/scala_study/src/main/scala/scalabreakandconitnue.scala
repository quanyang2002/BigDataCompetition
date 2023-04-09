import scala.util.control.Breaks._

object scalabreakandconitnue {

  def main(args: Array[String]): Unit = {
    breakable{
      for (i <- 1 to 10){
        if (i==5){
          break()
        }else{
          println(i)
        }
      }
    }

  }

}
