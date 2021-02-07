import spinal.core._
import spinal.lib._

class Layer(
  Chin  : Int,
  Chout : Int,
  stride : Int,
  padding : Int,
  Win        : Int,
  Hin        : Int,
  Wout       : Int,
  Hout       : Int,
  Q          : Int,
  layer      : Int
) extends Component {
  val io = new Bundle {
    val valid_in = in Bool
    val data_in  = in Vec(SInt(Q bits),Win)
    val valid_out = out Bool
    val data_out = out Vec(UInt(Q bits),Wout)
  }

  //layer fifo
  val fifo = Mem(Vec(SInt(Q bits),Win),wordCount = Hin*Chin)
  val faddw = Reg(UInt(log2Up(Hin*Chin) bits)) init(0)
  when(io.valid_in) {
    when(faddw < Hin*Chin - 1) {
      faddw := faddw + 1
    }.otherwise {
      faddw := 0
    }
  }
  fifo.write(
    address = faddw,
    data    = io.data_in,
    enable  = io.valid_in
  )

  val start = Reg(Bool) init(False)
  when(faddw === 0 && io.valid_in) {
    start := True
  }.otherwise {
    start := False
  }

  //ctrl
  val ctrl = new Ctrl(Chin = Chin, high = Hout, Hin = Hin, stride = stride, padding = padding)
  ctrl.io.start := start

  //feature map
  val fifoOut = Vec(SInt(Q bits),Win)
  fifoOut := fifo.readSync(ctrl.io.faddr)
  val peFM = Vec(Reg(SInt(Q bits)) init(0),Win + 2*padding)
  when(Delay(ctrl.io.shift,1)) {
    for(i <- 0 until Win + 2 * padding - 1) {
      peFM(i) := peFM(i + 1)
    }
  }.otherwise {
    if(padding == 1) {
      peFM(0) := 0
      peFM(Win + padding) := 0
      for(i <- 0 until Win) {
        peFM(i+1) := fifoOut(i)
      }
    }else {
      for(i <- 0 until Win) {
        peFM(i) := fifoOut(i)
      }
    }
  }

  //weight
  val wrom = new Wrom(Qw = 12, Chout = Chout, layer = layer)
  wrom.io.addr := Delay(ctrl.io.waddr,1)

  val pe = new PE(Wout = Wout, Chout = Chout, Qfm = Q)
  pe.io.clear := ctrl.io.clear
  for(i <- 0 until Wout) {
    pe.io.FM(i) := peFM(i * stride)
  }
  //or to write as:  pe.io.FM := Vec()
  pe.io.W  := wrom.io.w

  val peOut = Vec(Vec(Reg(UInt(Q bits)) init(0),Wout),Chout)
  when(Delay(ctrl.io.valid,4)) {
    peOut := pe.io.oup
  }.otherwise {
    for(i <- 0 until Chout - 1) {
      peOut(i) := peOut(i+1)
    }
  }

  io.valid_out := Delay(ctrl.io.valid,5,init = False)//False TODO
  io.data_out := peOut(0)

}

object laye {
  def main(args : Array[String]) {
    SpinalVerilog(new Layer(1,16,2,0,28,28,12,12,32,1))
  }
}