import spinal.core._
import spinal.lib._

object BatchNorm {
  def apply(inp : FM, weight : Array[Int]): FM = {
    val w = (0 until inp.getChannel()).map(x => weight(x)).toList
    val b = (0 until inp.getChannel()).map(x => weight(x + inp.getChannel())).toList
    val bn = new BatchNorm(inp.getQ, inp.getW, Array(log2Up(w.max)+1,10).max, log2Up(b.max)+1, inp.getChannel, w, b)
    bn.io.inp := inp.fm
    val ret = FM(inp)
    ret.fm := bn.io.oup
    ret
  }
}

class BatchNorm(
  Q : Int,
  W : Int,
  Qw1 : Int,
  Qw2 : Int,
  Ch : Int,
  w : List[Int],
  b : List[Int]
) extends Component {
  val io = new Bundle {
    val inp = in (Flow(Vec(SInt(Q bits),W)))
    val oup = out (Flow(Vec(SInt(Q bits),W)))
  }

  val weight = Reg(Vec(SInt(Qw1 bits),Ch))
  weight.zipWithIndex.map{case (value,idx) => value.init(-w(idx))}
  val bias = Reg(Vec(SInt(Qw2 bits),Ch))
  bias.zipWithIndex.map{case (value,idx) => value.init(b(idx))}
  when(io.inp.valid) {
    weight(Ch - 1) := weight(0)
    bias(Ch - 1) := bias(0)
    for(i <- 0 until Ch - 1) {
      weight(i) := weight(i+1)
      bias(i) := bias(i+1)
    }
  }
  val oup = Vec(Reg(SInt(Q bits)) init(0),W)
  for(i <- 0 until W) {
    oup(i) := (weight(0) * io.inp.payload(i))(Q + 9 downto 10) + bias(0)
  }
  io.oup.payload := oup
  io.oup.valid := Delay(io.inp.valid,1,init = False)

}
