import spinal.core._
import spinal.lib._

class mnist(
  Qi : Int = 8,
  Wi : Int = 16,
  Qo : Int = 10
) extends Component {
  val io = new Bundle {
    val image_in = in Vec(UInt(Qi bits),Wi)
    val valid_in = in Bool
    val answer    = out Bits(Qo bits)
    val valid_out = out Bool
  }



}