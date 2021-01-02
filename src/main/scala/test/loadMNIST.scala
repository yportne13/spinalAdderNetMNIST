import scala.io.Source
import java.io.{File, FileInputStream}
import breeze.linalg._
import breeze.plot._

object loadMNIST{
  def main(args : Array[String]) {

    val mat = new DenseMatrix[Double](28,28)

    val file = new File("../data/MNIST/raw/t10k-images-idx3-ubyte")
    val in = new FileInputStream(file)
    val bytes = new Array[Byte](file.length.toInt)
    in.read(bytes)
    for(i <- 0 until 28) {
      for(j <- 0 until 28) {
        val t = i*28+j+16+784*4
        if(bytes(t)<0) {
          mat(27-i,j) = 128 - bytes(t).toInt
        }else {
          mat(27-i,j) = bytes(t).toInt
        }
      }
    }
    val f2 = Figure()
    f2.subplot(0) += image(mat)
    //println(bytes)
    in.close()
    //val source = Source.fromFile("../data/MNIST/raw/t10k-images-idx3-ubyte")
    //println(source)
  }
}
