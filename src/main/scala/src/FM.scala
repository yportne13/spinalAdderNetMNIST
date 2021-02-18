import spinal.core._
import spinal.lib._

object FM {
  def apply(inp : FM): FM = {
    val ret = FM(inp.getQ, inp.getW, inp.getH, inp.getChannel)
    ret
  }
}

case class FM(
  Q : Int,
  W : Int,
  H : Int,
  Channel : Int
) extends Bundle {

  val fm = Flow(Vec(SInt(Q bits),W))

  def getQ(): Int = {
    Q
  }

  def getW(): Int = {
    W
  }

  def getH(): Int = {
    H
  }
  def getChannel(): Int = {
    Channel
  }

}

/*
case class FM(inp : Flow[Vec[SInt]], H : Int, Chout : Int) {
  val FMram = Mem(Vec(SInt(Q bits),Win),wordCount = (Hin+2*padding)*Chin*2)
  FMram.init(Vec((0 until (Hin+2*padding)*Chin*2).map(x => Vec((0 until Win).map(x => S(0,Q bits))))))//init the ram with all 0
  val pingpongW = Reg(Bool) init(False)
  val pingpongR = Reg(Bool) init(True)
  val faddw = Reg(UInt(log2Up((Hin+2*padding)*Chin) bits)) init(padding * Chin)
  when(io.valid_in) {
    when(faddw < (Hin+padding)*Chin - 1) {
      faddw := faddw + 1
    }.otherwise {
      faddw := padding * Chin
    }
  }
  FMram.write(
    address = U(faddw ## pingpongW),
    data    = io.data_in,
    enable  = io.valid_in
  )
}
*/