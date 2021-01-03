/*import spinal.core._
import spinal.lib._

class MAC(
  Qf : Int = 10,//quantization of feature map
  Qw : Int = 8,//quantization of weight
  Wi : Int = 28//parrel width
) extends Component {
  val io = new Bundle {
    val fm     = in Vec(SInt(Qf bits),Wi+2)
    val weight = in SInt(Qw bits)
    val bias   = in SInt(Qw bits)
    val clear  = in Bool
    val shifUp = in Bool
    val shifDown = in Bool
    val fmout  = out Vec(SInt(Qf bits),Wi)
  }

  val fmout = Vec(Reg(SInt(Qf bits)),Wi)
  val buff  = Vec(Reg(SInt(Qf bits)),Wi)
  val temp  = Vec(SInt(Qf bits),Wi)

  when(io.clear) {
    buff := io.bias.resize(Qf bits)
  }.otherwise {
    buff := temp
  }

  val fm = Vec(Reg(SInt(Qf bits)),Wi)
  when(io.shifUp) {
    for(i <- 0 until Wi) {
      fm(i) := io.fm(i)
    }
  }.elsewhen(io.shifDown) {
    for(i <- 0 until Wi) {
      fm(i) := io.fm(i+2)
    }
  }.otherwise {
    for(i <- 0 until Wi) {
      fm(i) := io.fm(i+1)
    }
  }

  temp := (fm*RegNext(io.weight)+buff*(1 << (Qw-1)))(Qf+Qw-1 downto Qw)

  fmout := temp

  io.fmout := fmout

}*/