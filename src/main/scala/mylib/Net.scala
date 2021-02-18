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