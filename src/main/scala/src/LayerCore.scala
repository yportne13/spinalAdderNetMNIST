import spinal.core._
import spinal.lib._
import spinal.sim._
import spinal.core.sim._

class LayerCore(
  Chin  : Int,
  Chout : Int,
  ChoutDivHard : Int,
  stride : Int,
  padding : Int,
  Win        : Int,
  Hin        : Int,
  Qi         : Int,
  Qo         : Int,
  layer      : Int,
  highFreq   : Int = 1
) extends Component {

  val Wout = (Win + 2 * padding) / stride + padding - 2
  val Hout = (Hin + 2 * padding) / stride + padding - 2
  val io = new Bundle {
    val valid_in = in Bool
    val data_in  = in Vec(SInt(Qi bits),Win)
    val valid_out = out Bool
    val data_out = out Vec(Vec(UInt(Qo bits),Wout),Chout / ChoutDivHard)//Vec(UInt(Q bits),Wout)
  } simPublic()

  //layer ram
  val FMram = Mem(Vec(SInt(Qi bits),Win),wordCount = (Hin+2*padding)*Chin*2)
  FMram.init(Vec((0 until (Hin+2*padding)*Chin*2).map(x => Vec((0 until Win).map(x => S(0,Qi bits))))))//init the ram with all 0
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

  val start = Reg(Bool) init(False)
  when(faddw === (Hin+padding)*Chin - 1 && io.valid_in) {
    start := True
    pingpongR := !pingpongR
    pingpongW := !pingpongW
  }.otherwise {
    start := False
  }

  //ctrl
  val ctrl = new Ctrl(Chin = Chin, ChoutDivHard = ChoutDivHard, high = Hout, Hin = Hin, stride = stride, padding = padding)
  ctrl.io.start := start

  //feature map
  val FMramOut = Vec(SInt(Qi bits),Win)
  FMramOut := FMram.readSync(U(ctrl.io.faddr ## pingpongR))
  val peFM = Vec(Reg(SInt(Qi bits)) init(0),Win + 2*padding)
  when(Delay(ctrl.io.shift,1)) {
    for(i <- 0 until Win + 2 * padding - 1) {
      peFM(i) := peFM(i + 1)
    }
  }.otherwise {
    if(padding == 1) {
      peFM(0) := 0
      peFM(Win + padding) := 0
      for(i <- 0 until Win) {
        peFM(i+1) := FMramOut(i)
      }
    }else {
      for(i <- 0 until Win) {
        peFM(i) := FMramOut(i)
      }
    }
  }

  //weight
  val wrom = new Wrom(Qw = 12, Chout = Chout, layer = layer, ChoutDivHard = ChoutDivHard)
  wrom.io.addr := Delay(ctrl.io.waddr,1)

  val pe = new PE(Wout = Wout, Chout = Chout / ChoutDivHard, Qfm = Qi, Qo = Qo, highFreq = highFreq)
  pe.io.clear := ctrl.io.clear
  for(i <- 0 until Wout) {
    pe.io.FM(i) := peFM(i * stride)
  }
  //or to write as:  pe.io.FM := Vec()
  pe.io.W  := wrom.io.w

  io.valid_out := Delay(ctrl.io.valid,4+highFreq,init = False)
  io.data_out := pe.io.oup//peOut(0)

}

//object laye {
//  def main(args : Array[String]) {
//    SpinalVerilog(new LayerCore(1,16,1,2,0,28,28,16,1))
//  }
//}