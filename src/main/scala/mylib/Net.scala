package Net

case class NetConfig() {
  var weightList = List[List[Int]]()
}

trait Net {
  def conv2d(x : NetConfig, input_channel : Int, output_channel : Int, kernel_size : Int, stride : Int = 1, padding : Int = 0, bias: Boolean = false): NetConfig = {
    x.weightList = x.weightList :+ List((input_channel * output_channel * kernel_size * kernel_size))
    x
  }
  def adder2d(x : NetConfig, input_channel : Int, output_channel : Int, kernel_size : Int, stride : Int = 1, padding : Int = 0, bias: Boolean = false): NetConfig = {
    x.weightList = x.weightList :+ List((input_channel * output_channel * kernel_size * kernel_size))
    x
  }
  def BatchNorm(x : NetConfig, input_channel : Int): NetConfig = {
    x.weightList = x.weightList :+ List(input_channel,input_channel,input_channel,input_channel,1)
    x
  }
  def relu(x : NetConfig): NetConfig = {
    x
  }
}

object Golden {
  def adder2d(inp : Array[Array[Array[Double]]], ic : Int, oc : Int, w : Array[Double], stride : Int, padding : Int): Array[Array[Array[Double]]] = {
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
                oup(i)(x)(y) = oup(i)(x)(y) - (tinp(j)(stride * x+m)(stride * y+n) - w(i*9*ic+j*9+m*3+n)).abs
              }
            }
          }
        }
      }
    }
    oup
  }
  def BatchNorm(inp : Array[Array[Array[Double]]], weight : Array[Double]): Array[Array[Array[Double]]] = {
    val oup = inp.zipWithIndex.map{case (value,idx) => value.map(_.map(x => x * weight(idx) + weight(inp.length + idx)).toArray).toArray}.toArray
    oup
  }
  def relu(inp : Array[Array[Array[Double]]]): Array[Array[Array[Double]]] = {
    inp.map(_.map(_.map(xi => if(xi>=0)xi else 0)))
  }
}