import spinal.core._
import spinal.lib._

class ReLU(
  Qi : Int = 32,
  Wi : Int = 28
) extends Component {
  val io = new Bundle {
    val inp = in Vec(SInt(Qi bits),Wi)
    val oup = out Vec(SInt(Qi bits),Wi)
  }

  val oup = Vec(Reg(SInt(Qi bits)),Wi)
  for(i <- 0 until Wi) {
    when(io.inp(i)(Qi - 1) === False) {//inp > 0
      oup(i) := io.inp(i)
    }.otherwise {
      oup(i) := 0
    }
  }
  io.oup := oup

}