import java.io.{File, FileInputStream}
import breeze.linalg._
import breeze.plot._

object tb {
  def main(args : Array[String]) {
    val mat = Array.ofDim[Double](10000,1,28,28)//new DenseMatrix[Double](10000,28,28)
    val label = new Array[Int](10000)

    val fil = new File("../data/MNIST/raw/t10k-labels-idx1-ubyte")
    val inn = new FileInputStream(fil)
    val bytl = new Array[Byte](fil.length.toInt)
    inn.read(bytl)
    for(k <- 0 until 10000) {
      label(k) = bytl(k+8).toInt
    }

    val file = new File("../data/MNIST/raw/t10k-images-idx3-ubyte")
    val in = new FileInputStream(file)
    val bytes = new Array[Byte](file.length.toInt)
    in.read(bytes)
    for(k <- 0 until 10000) {
      for(i <- 0 until 28) {
        for(j <- 0 until 28) {
          val t = i*28+j+16+784*k
          if(bytes(t)<0) {
            mat(k)(0)(i)(j) = 256 + bytes(t).toInt
          }else {
            mat(k)(0)(i)(j) = bytes(t).toInt
          }
        }
      }
    }
    //val f2 = Figure()
    //f2.subplot(0) += image(mat)
    in.close()

    val filew = new File("param_v19best.bin")
    val inw = new FileInputStream(filew)
    val bytesw = new Array[Byte](filew.length.toInt)
    inw.read(bytesw)
    //get the bytes(i*4)
    inw.close()

    val w1 = new Array[Int](16*9)
    val b1 = new Array[Int](16)
    val w2 = new Array[Int](32*16*9)
    val b2 = new Array[Int](32)
    val w3 = new Array[Int](6272*10)
    val b3 = new Array[Int](10)
    for(i <- 0 until 16*9) {
      w1(i) = bytesw(4*i)
    }
    for(i <- 0 until 16) {
      b1(i) = bytesw(4*(i+16*9))
    }
    for(i <- 0 until 32*16*9) {
      w2(i) = bytesw(4*(i+160))
    }
    for(i <- 0 until 32) {
      b2(i) = bytesw(4*(i+160+32*16*9))
    }
    for(i <- 0 until 62720) {
      w3(i) = bytesw(4*(i+160+32*16*9+32))
    }
    for(i <- 0 until 10) {
      b3(i) = bytesw(4*(i+62720+160+32*16*9+32))
    }
    println(w3.max)

    var suc = 0
    for(i <- 0 until 1000) {
      

      //var max = l3(0)
      //var index = 0
      //for(j <- 0 until 10) {
      //  if(max < l3(j)) {
      //    max = l3(j)
      //    index = j
      //  }
      //}
      //for(j <- 0 until 10) {
      //  print(l3(j)+",")
      //}
      //println()
      //println(index + "," + label(i))
      //if(index == label(i)) {
      //  suc = suc + 1
      //}
    }
    println(suc)

  }
}