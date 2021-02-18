import spinal.core._
import spinal.lib._

class ANN2(
  Q : Int
) extends Component {
  val io = new Bundle {
    val valid_in = in Bool
    val data_in  = in Vec(SInt(8 bits),14)
    val output = out (Flow(UInt(4 bits)))
  }

  val l1in = Vec(Reg(SInt(8 bits)) init(0),28)
  when(io.valid_in) {
    for(i <- 0 until 14) {
      l1in(i+14) := io.data_in(i).resize(8 bits)
      l1in(i) := l1in(i+14)
    }
  }
  val flip = Reg(Bool) init(False)
  when(io.valid_in) {
    flip := !flip
  }.otherwise {
    flip := False
  }

  val inp = FM(8,28,28,1)
  inp.fm.valid := Delay(flip,1,init = False)
  inp.fm.payload := l1in

  val weight = LoadWeight("mnist.bin",mnistBN().x.weightList)

  val l1 = adder2d(inp,1,16,3,2,0,false,13,1, ChoutDivHard = 4)
  val b1 = BatchNorm(l1,weight(1))
  val r1 = ReLu2(b1)

  val l2 = adder2d(r1,16,16,3,2,1,false,17,3, ChoutDivHard = 2)
  val b2 = BatchNorm(l2,weight(3))
  val r2 = ReLu2(b2)

  val l3 = adder2d(r2,16,32,3,2,1,false,Q,5, ChoutDivHard = 2)
  val b3 = BatchNorm(l3,weight(5))
  val r3 = ReLu2(b3)

  val l4 = adder2d(r3,32,10,3,1,0,false,Q,7, ChoutDivHard = 1)

  io.output := FindMax(l4.fm)
}

object annTop2 {
  def main(args : Array[String]) {
    SpinalVerilog(new ANN2(18))
  }
}
