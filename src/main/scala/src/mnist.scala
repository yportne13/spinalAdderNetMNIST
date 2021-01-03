/*import spinal.core._
import spinal.lib._

class mnist(
  Qi : Int = 8,
  Wi : Int = 28,
  Qf : Int = 10,
  Qo : Int = 10
) extends Component {
  val io = new Bundle {
    val image_in = in Vec(UInt(Qi bits),Wi)
    val valid_in = in Bool
    val answer    = out Bits(Qo bits)
    val valid_out = out Bool
  }

  val ctrl = new Ctrl
  val fm = new FM
  val weight = new Weight
  val mac = new MAC
  val relu = new ReLU
  val pool = new Pool
  ctrl.io.start := io.valid_in
  weight.io.waddr := ctrl.io.waddr
  weight.io.baddr := ctrl.io.baddr
  fm.io.addr := ctrl.io.faddr
  fm.io.addw := ctrl.io.faddw
  fm.io.we   := ctrl.io.we

  mac.io.fm := fm.io.dout
  mac.io.weight := weight.io.W
  mac.io.bias   := weight.io.bias
  mac.io.clear  := 
  
  val fmin = Vec(Reg(SInt(Qf bits)),28)
  when() {
    fmin := relu.io.oup
  }.elsewhen() {
    fmin := pool.io.oup
  }.otherwise {
    fmin := io.image_in
  }
  fm.io.din := fmin

}*/