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
  def BatchNorm(inp : Array[Array[Array[Double]]], weight : Array[Double], bias : Array[Double]): Array[Array[Array[Double]]] = {
    val oup = inp.zipWithIndex.map{case (value,idx) => value.map(_.map(x => x * weight(idx) + bias(idx)).toArray).toArray}.toArray
    oup
  }
  def relu(inp : Array[Array[Array[Double]]]): Array[Array[Array[Double]]] = {
    inp.map(_.map(_.map(xi => if(xi>=0)xi else 0)))
  }
  def main(args : Array[String]) {
    val (mat,label) = LoadMNIST()

    val wList = LoadWeight("param_ann.bin",mnistNoBN().x.weightList)
    //println(w1(8))
    //println(mat(0)(0)(7)(7))

    var suc = 0
    for(i <- 0 until 100) {
      var l1 = conv2d(mat(i),1,16,wList(0),2,0)
      l1 = l1.map(_.map(_.map(_+10)))
      l1 = l1.map(_.map(_.map(_/4)))
      var r1 = relu(l1)
      r1 = r1.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l2 = conv2d(r1,16,32,wList(1),2,1)
      l2 = l2.map(_.map(_.map(_+130)))
      l2 = l2.map(_.map(_.map(_/8)))
      var r2 = relu(l2)
      r2 = r2.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l3 = conv2d(r2,32,16,wList(2),2,1)
      l3 = l3.map(_.map(_.map(_+280)))
      l3 = l3.map(_.map(_.map(_/16)))
      var r3 = relu(l3)
      r3 = r3.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      val l4 = conv2d(r3,16,10,wList(3),1,0)

      //for(k <- 0 until 1) {
      //  for(j <- 0 until 1) {//mat(0)(0)(k)(j)
      //    //print(-(l1(0)(k)(j)*4-10)*256 + ",")
      //    print(l4(0)(k)(j)*256 + ",")
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

  }/*
  def main(args : Array[String]) {
    val (mat,label) = LoadMNIST()

    val wList = LoadWeight("mnist.bin",List(16*9,16,16,16,16,1,16*16*9,16,16,16,16,1,16*32*9,32,32,32,32,1,32*10*9))
    //println(w1(8))
    //println(mat(0)(0)(7)(7))

    var suc = 0
    for(i <- 0 until 5) {
      var l1 = conv2d(mat(i),1,16,wList(0),2,0)
      var b1 = BatchNorm(l1,)
      var r1 = relu(b1)
      r1 = r1.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l2 = conv2d(r1,16,32,wList(1),2,1)
      l2 = l2.map(_.map(_.map(_+130)))
      l2 = l2.map(_.map(_.map(_/8)))
      var r2 = relu(l2)
      r2 = r2.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l3 = conv2d(r2,32,16,wList(2),2,1)
      l3 = l3.map(_.map(_.map(_+280)))
      l3 = l3.map(_.map(_.map(_/16)))
      var r3 = relu(l3)
      r3 = r3.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      val l4 = conv2d(r3,16,10,wList(3),1,0)

      //for(k <- 0 until 1) {
      //  for(j <- 0 until 1) {//mat(0)(0)(k)(j)
      //    //print(-(l1(0)(k)(j)*4-10)*256 + ",")
      //    print(l4(0)(k)(j)*256 + ",")
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

  }*/
}