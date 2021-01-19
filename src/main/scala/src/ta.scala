import spinal.core._
import spinal.lib._

class ta extends Component {
  val io = new Bundle {
    val inp = in Vec(SInt(8 bits),2048)
    val w   = in Vec(SInt(8 bits),2048)
    val oup = out Vec(SInt(8 bits),2048)
  }

  def abs(inp : SInt): SInt = {
    val ret = SInt(inp.getWidth bits)
    when(inp(inp.getWidth - 1) === False) {
      ret := inp
    }.otherwise {
      ret := -inp
    }
    ret
  }

  val oup = Vec(Reg(SInt(8 bits)) init(0),2048)
  for(i <- 0 until 2048) {
    oup(i) := abs(io.inp(i) + io.w(i)) + oup(i)
  }
  io.oup := oup

}

object tatop {
  def main(args : Array[String]) {
    SpinalVerilog(new ta)
  }
}