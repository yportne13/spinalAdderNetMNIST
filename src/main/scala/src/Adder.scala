import spinal.core._
import spinal.lib._

class Adder(
  Qf : Int = 32,
  Wi : Int = 28
) extends Component {
  val io = new Bundle {
    val inp = in Vec(SInt(Qf bits),Wi)
    val oup = out SInt(Qf+log2Up(Wi) bits)
  }

  def sum(inp : Vec[SInt]): SInt = {
    val wide = B(inp).getWidth / inp(0).getWidth / 2
    if(wide == 1) {
      RegNext(inp(0)+^inp(1))
    }else {
      val ret = Vec(Reg(SInt(inp(0).getWidth + 1 bits)),wide)
      for(i <- 0 until wide) {
        ret(i) := inp(2*i) +^ inp(2*i+1)
      }
      sum(ret)
    }
  }

  val inp = Vec(SInt(Qf bits),(1 << log2Up(Wi)))
  for(i <- 0 until (1 << log2Up(Wi))) {
    if(i < Wi) {
      inp(i) := io.inp(i)
    }else {
      inp(i) := 0
    }
  }
  io.oup := sum(inp)

}