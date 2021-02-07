import java.io.{File, FileInputStream}

object LoadMNIST {
  def apply():(Array[Array[Array[Array[Double]]]],Array[Int]) = {
    val mat = Array.ofDim[Double](10000,1,28,28)//new DenseMatrix[Double](10000,28,28)
    val label = new Array[Int](10000)

    val fil = new File("../data/MNIST/raw/t10k-labels-idx1-ubyte")
    val inn = new FileInputStream(fil)
    val bytl = new Array[Byte](fil.length.toInt)
    inn.read(bytl)
    for(k <- 0 until 10000) {
      label(k) = bytl(k+8).toInt
    }
    inn.close()

    val file = new File("../data/MNIST/raw/t10k-images-idx3-ubyte")
    val in = new FileInputStream(file)
    val bytes = new Array[Byte](file.length.toInt)
    in.read(bytes)
    for(k <- 0 until 10000) {
      for(i <- 0 until 28) {
        for(j <- 0 until 28) {
          val t = i*28+j+16+784*k
          if(bytes(t)<0) {
            mat(k)(0)(i)(j) = 256 + bytes(t).toInt.toDouble
          }else {
            mat(k)(0)(i)(j) = bytes(t).toInt.toDouble
          }
          mat(k)(0)(i)(j) = mat(k)(0)(i)(j).toDouble / 256.0 - 0.5
        }
      }
    }
    //val f2 = Figure()
    //f2.subplot(0) += image(mat)
    in.close()
    (mat,label)
  }
}