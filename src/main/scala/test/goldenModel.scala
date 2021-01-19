import java.io.{File, FileInputStream}
import breeze.linalg._
import breeze.plot._

object Golden {
  def conv2d(inp : Array[Array[Array[Double]]], ic : Int, oc : Int, w : Array[Int], stride : Int, padding : Int): Array[Array[Array[Double]]] = {
    val wide = inp(0).length + padding*2
    var tinp = Array.ofDim[Double](ic,wide,wide)
    if(padding == 1) {
      for(i <- 0 until ic) {
        for(j <- 0 until wide) {
          for(k <- 0 until wide) {
            if(j == 0 || j == wide-1 || k == 0 || k == wide-1) {
              tinp(i)(j)(k) = 0
            }else {
              tinp(i)(j)(k) = inp(i)(j-1)(k-1)
            }
          }
        }
      }
    }else {
      tinp = inp
    }
    val wideout = wide/stride - 2 + padding
    val oup = Array.ofDim[Double](oc,wideout,wideout)
    for(x <- 0 until wideout) {//calculate conv
      for(y <- 0 until wideout) {
        for(i <- 0 until oc) {
          for(j <- 0 until ic) {
            for(m <- 0 until 3) {
              for(n <- 0 until 3) {
                oup(i)(x)(y) = oup(i)(x)(y) - (tinp(j)(stride * x+m)(stride * y+n) - w(i*9*ic+j*9+m*3+n).toDouble / 64).abs
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

    val filew = new File("param_ann.bin")
    val inw = new FileInputStream(filew)
    val bytesw = new Array[Byte](filew.length.toInt)
    inw.read(bytesw)
    //get the bytes(i*4)
    inw.close()

    val w1 = new Array[Int](16*9)
    val w2 = new Array[Int](32*16*9)
    val w3 = new Array[Int](32*16*9)
    val w4 = new Array[Int](16*10*9)
    def b2i(i1 : Byte, i2 : Byte): Int = {
      val t1 : Int = i1
      val t2 : Int = i2
      if(t2 >= 0) {
        if(t1 < 0) {
          t2*256+256+t1
        } else {
          t2*256+t1
        }
      } else {
        if(t1 < 0) {
          256*(t2+1) + t1
        }else {
          256*(t2+1)-256+t1
        }
      }
    }
    for(i <- 0 until 16*9) {
      w1(i) = b2i(bytesw(4*i+0),bytesw(4*i+1))//bytesw(4*i+0)
    }
    for(i <- 0 until 32*16*9) {
      w2(i) = b2i(bytesw(4*(i+16*9)),bytesw(4*(i+16*9)+1))//bytesw(4*(i+16*9))
    }
    for(i <- 0 until 32*16*9) {
      w3(i) = b2i(bytesw(4*(i+16*9+32*16*9)),bytesw(4*(i+16*9+32*16*9)+1))//bytesw(4*(i+16*9+32*16*9))
    }
    for(i <- 0 until 16*10*9) {
      w4(i) = b2i(bytesw(4*(i+16*9+32*16*9*2)),bytesw(4*(i+16*9+32*16*9*2)+1))//bytesw(4*(i+16*9+32*16*9*2))
    }
    println(w4(160*9-1))

    var suc = 0
    for(i <- 0 until 10000) {
      var l1 = conv2d(mat(i),1,16,w1,2,0)
      l1 = l1.map(_.map(_.map(_+10)))
      l1 = l1.map(_.map(_.map(_/4)))
      val r1 = relu(l1)
      var l2 = conv2d(r1,16,32,w2,2,1)
      l2 = l2.map(_.map(_.map(_+130)))
      l2 = l2.map(_.map(_.map(_/8)))
      val r2 = relu(l2)
      var l3 = conv2d(r2,32,16,w3,2,1)
      l3 = l3.map(_.map(_.map(_+280)))
      l3 = l3.map(_.map(_.map(_/16)))
      val r3 = relu(l3)
      val l4 = conv2d(r3,16,10,w4,1,0)

      //for(k <- 0 until 1) {
      //  for(j <- 0 until 1) {//mat(0)(0)(k)(j)
      //    print(l4(0)(k)(j) + ",")
      //  }
      //  println()
      //}

      var max = l4(0)(0)(0)
      var index = 0
      for(j <- 0 until 10) {
        if(max < l4(j)(0)(0)) {
          max = l4(j)(0)(0)
          index = j
        }
      }
      //for(j <- 0 until 10) {
      //  print(l3(j)+",")
      //}
      //println()
      //println(index + "," + label(i))
      if(index == label(i)) {
        suc = suc + 1
      }
    }
    println(suc)

  }
}