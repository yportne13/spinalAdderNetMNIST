import spinal.core._
import spinal.lib._

class ANN(
  Q : Int
) extends Component {
  val io = new Bundle {
    val valid_in = in Bool
    val data_in  = in Vec(SInt(Q bits),28)
    val output = out (Flow(UInt(4 bits)))
  }

  val l1 = new Layer(1,16,2,0,28,28,Q,1,SubNum = 10*256, DivNum = 2)
  l1.io.input.valid := io.valid_in
  l1.io.input.payload  := io.data_in

  val l2 = new Layer(16,32,2,1,12,12,Q,2,SubNum = 130*256, DivNum = 3)
  l2.io.input := l1.io.output

  val l3 = new Layer(32,16,2,1,6,6,Q,3,SubNum = 280*256, DivNum = 4)
  l3.io.input := l2.io.output

  val l4 = new Layer(16,10,1,0,3,3,Q,4,SubNum = 0, DivNum = 0, noReLu = true)
  l4.io.input := l3.io.output

  io.output := FindMax(l4.io.output)
}

object annTop {
  def main(args : Array[String]) {
    SpinalVerilog(new ANN(24))
  }
}
