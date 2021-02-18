import Net._

case class mnistBN() extends Net {
  var x = NetConfig()
  x = adder2d(x,1,16,3,2,0)
  x = BatchNorm(x,16)
  x = relu(x)
  x = adder2d(x,16,16,3,2,1)
  x = BatchNorm(x,16)
  x = relu(x)
  x = adder2d(x,16,32,3,2,1)
  x = BatchNorm(x,32)
  x = relu(x)
  x = adder2d(x,32,10,3,2,0)
}