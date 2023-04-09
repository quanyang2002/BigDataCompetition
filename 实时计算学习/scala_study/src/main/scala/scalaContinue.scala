import scala.util.control.Breaks._

object scalaContinue {

  def main(args: Array[String]): Unit = {

    for(i <- 1 to 10){
      breakable{
        if (i%3==0){
          break()
        }else{
          println(i)
        }
      }
    }
  }

}
