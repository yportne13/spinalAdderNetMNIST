import java.io.{File, FileInputStream}
import breeze.linalg._
import breeze.plot._

object Golden {
  def conv2d(inp : Array[Array[Array[Double]]], ic : Int, oc : Int, w : Array[Int], b : Array[Int]): Array[Array[Array[Double]]] = {//DenseMatrix[Double] = {
    val tinp = Array.ofDim[Double](ic,30,30)//new DenseMatrix[Double](ic,30,30)
    for(i <- 0 until ic) {
      for(j <- 0 until 30) {
        for(k <- 0 until 30) {
          if(j == 0 || j == 29 || k == 0 || k == 29) {
            tinp(i)(j)(k) = 0
          }else {
            tinp(i)(j)(k) = inp(i)(j-1)(k-1)
          }
        }
      }
    }
    val oup = Array.ofDim[Double](oc,28,28)//new DenseMatrix[Double](oc,28,28)
    for(x <- 0 until 28) {//init with bias
      for(y <- 0 until 28) {
        for(k <- 0 until oc) {
          oup(k)(x)(y) = b(k) / 128
        }
      }
    }
    for(x <- 0 until 28) {//calculate conv
      for(y <- 0 until 28) {
        for(i <- 0 until oc) {
          for(j <- 0 until ic) {
            for(m <- 0 until 3) {
              for(n <- 0 until 3) {
                oup(i)(x)(y) = oup(i)(x)(y) + (tinp(j)(x+m)(y+n) * w(i*9*ic+j*9+m*3+n) / 128)//.toInt
              }
            }
          }
        }
      }
    }
    oup
  }
  def relu(inp : Array[Array[Array[Double]]]): Array[Array[Array[Double]]] = {
    inp.map(_.map(_.map(xi => if(xi>=0)xi else 0)))
  }
  def pool(inp : Array[Array[Array[Double]]]): Array[Array[Array[Double]]] = {
    val oup = Array.ofDim[Double](32,14,14)//new DenseMatrix[Double](32,14,14)
    for(i <- 0 until 32) {
      for(j <- 0 until 14) {
        for(k <- 0 until 14) {
          oup(i)(j)(k) = Array(inp(i)(2*j)(2*k),inp(i)(2*j+1)(2*k),inp(i)(2*j)(2*k+1),inp(i)(2*j+1)(2*k+1)).max
        }
      }
    }
    oup
  }
  def fc(inp : Array[Array[Array[Double]]], w : Array[Int], b : Array[Int]): Array[Double] = {
    val oup = new Array[Double](10)
    for(i <- 0 until 10) {
      oup(i) = b(i) / 128
    }
    for(i <- 0 until 10) {
      for(j <- 0 until 32) {
        for(m <- 0 until 14) {
          for(n <- 0 until 14) {
            oup(i) = oup(i) + (inp(j)(m)(n) * w(i*6272 + j*14*14+m*14+n) / 128)//.toInt
          }
        }
      }
    }
    oup
  }
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

    for(i <- 0 until 10) {
      val l1 = conv2d(mat(i),1,16,w1,b1)
      
      val r1 = relu(l1)
      val l2 = conv2d(r1,16,32,w2,b2)
      val r2 = relu(l2)
      val m2 = pool(r2)
      val l3 = fc(m2,w3,b3)

      //for(k <- 0 until 28) {
      //  for(j <- 0 until 28) {//mat(0)(0)(k)(j)
      //    print(l2(0)(k)(j).toInt + ",")
      //  }
      //  println()
      //}

      var max = l3(0)
      var index = 0
      for(j <- 0 until 10) {
        if(max < l3(j)) {
          max = l3(j)
          index = j
        }
      }
      //for(j <- 0 until 10) {
      //  print(l3(j)+",")
      //}
      println()
      println(index + "," + label(i))
      //println(l3)
    }

  }
}