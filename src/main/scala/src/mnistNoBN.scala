import Net._

case class mnistNoBN() extends Net {
  var x = NetConfig()
  x = adder2d(x,1,16,3,2,0)
  x = relu(x)
  x = adder2d(x,16,32,3,2,1)
  x = relu(x)
  x = adder2d(x,32,16,3,2,1)
  x = relu(x)
  x = adder2d(x,16,10,3,2,0)
}
