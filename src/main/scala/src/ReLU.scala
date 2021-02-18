import spinal.core._
import spinal.lib._

object ReLu2 {
  def apply(input : FM): FM = {
    val Qi = input.getQ
    val Wi = input.getW
    val ret = Reg(FM(input))
    ret.fm.valid.init(False)
    ret.setWeakName("ReLuOut")
    for(i <- 0 until Wi) {
      when(input.fm.payload(i)(Qi - 1) === False) {//inp > 0
        ret.fm.payload(i) := input.fm.payload(i)
      }.otherwise {
        ret.fm.payload(i) := 0
      }
    }
    ret.fm.valid := input.fm.valid//Delay(input.fm.valid,1,init = False)
    ret
  }
}

object ReLu {
  def apply(input : Vec[SInt]): Vec[SInt] = {
    val Qi = B(input(0)).getWidth
    val Wi = B(input).getWidth / Qi
    val ret = Vec(Reg(SInt(Qi bits)) init(0),Wi)
    ret.setWeakName("ReLuOut")
    for(i <- 0 until Wi) {
      when(input(i)(Qi - 1) === False) {//inp > 0
        ret(i) := input(i)
      }.otherwise {
        ret(i) := 0
      }
    }
    ret
  }
}
/*
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

}*/
