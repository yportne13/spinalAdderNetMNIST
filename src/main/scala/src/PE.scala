import spinal.core._
import spinal.lib._

class PE(
  Chout : Int = 8,
  Wout : Int = 9,
  Qw  : Int = 12,
  Qfm : Int = 10,
  Qo  : Int = 32
) extends Component {
  val io = new Bundle {
    val clear = in Bool
    val W  = in Vec(SInt(Qw  bits),Chout)
    val FM = in Vec(SInt(Qfm bits),Wout)
    val oup = out Vec(Vec(UInt(Qo bits),Wout),Chout)
  }

  val oup = Vec(Vec(Reg(UInt(Qo bits)) init(0),Wout),Chout)
  val absOut = Vec(Vec(Reg(UInt(Array(Qfm,Qw).max bits)) init(0),Wout),Chout)
  for(i <- 0 until Chout) {
    for(j <- 0 until Wout) {
      absOut(i)(j) := (io.FM(j) -^ io.W(i)).abs.resize(Array(Qfm,Qw).max bits)
    }
  }
  
  when(Delay(io.clear,3)) {
    for(i <- 0 until Chout) {
      for(j <- 0 until Wout) {
        oup(i)(j) := absOut(i)(j)
      }
    }
  }.otherwise {
    for(i <- 0 until Chout) {
      for(j <- 0 until Wout) {
        oup(i)(j) := oup(i)(j) + absOut(i)(j)
      }
    }
  }
  io.oup := oup

}