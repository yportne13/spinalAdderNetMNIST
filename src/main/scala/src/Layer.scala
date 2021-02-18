import spinal.core._
import spinal.lib._
import spinal.sim._
import spinal.core.sim._

object adder2d {
  def apply(inp : FM, input_channel : Int, output_channel : Int, kernel_size : Int, stride : Int = 1, padding : Int = 0, bias: Boolean = false, Qo : Int, layer : Int, ChoutDivHard : Int): FM = {
    val Win = inp.getW
    val Hin = inp.getH
    val Wout = (Win + 2 * padding) / stride + padding - 2
    val Hout = (Hin + 2 * padding) / stride + padding - 2
    val l = new Layer(input_channel,output_channel,stride,padding,Win = inp.getW, Hin = inp.getH, Qi = inp.getQ, Qo = Qo, layer = layer, SubNum = 0, DivNum = 0, ChoutDivHard = ChoutDivHard, noReLu = true)
    l.io.input := inp.fm
    val oup = FM(Qo,Wout,Hout,output_channel)
    oup.fm := l.io.output
    oup
  }
}

class Layer(
  Chin  : Int,
  Chout : Int,
  stride : Int,
  padding : Int,
  Win        : Int,
  Hin        : Int,
  Qi         : Int,
  Qo         : Int,
  layer      : Int,
  SubNum     : Int,
  DivNum     : Int,
  ChoutDivHard : Int = 1,
  noReLu     : Boolean = false
) extends Component {

  val Wout = (Win + 2 * padding) / stride + padding - 2
  val Hout = (Hin + 2 * padding) / stride + padding - 2

  val io = new Bundle {
    val input = in (Flow(Vec(SInt(Qi bits),Win)))
    val output = out (Flow(Vec(SInt(Qo bits),Wout)))
  } simPublic()

  val lcore = new LayerCore(Chin,Chout,ChoutDivHard,stride,padding,Win,Hin,Qi,Qo,layer)
  lcore.io.valid_in := io.input.valid
  lcore.io.data_in  := io.input.payload

  val lcOut = Flow(Vec(UInt(Qo bits),Wout))
  if(9*Chin < Chout / ChoutDivHard) {
    var ChoutD = Chout / ChoutDivHard
    val wide = scala.math.ceil(ChoutD / 9.0*Chin).toInt
    val lOut = Vec(Vec(Reg(UInt(Qo bits)) init(0),Wout),Chout)
    when(lcore.io.valid_out) {
      lOut := lcore.io.data_out
    }.otherwise {
      for(i <- 0 until ChoutD/wide - 1) {
        (0 until wide).map(x => lOut(i * wide + x) := lOut(i * wide + x + wide))
      }
    }
    val fifoWen = Reg(Bool) init(False)
    fifoWen.setWeakName("fifoWen")
    when(lcore.io.valid_out) {
      fifoWen := True
    }.elsewhen(Delay(lcore.io.valid_out, ChoutD / wide, init = False)) {
      fifoWen := False
    }
    val faddw = Reg(UInt(log2Up(ChoutD * Hout / wide) bits)) init(0)
    faddw.setWeakName("faddw")
    when(fifoWen) {
      when(faddw < ChoutD * Hout / wide - 1) {
        faddw := faddw + 1
      }.otherwise {
        faddw := 0
      }
    }
    val fifo = Mem(Vec(Vec(UInt(Qo bits),Wout),wide),wordCount = Hout * ChoutD / wide)
    fifo.write (
      address = faddw,
      enable  = fifoWen,
      data    = Vec((0 until wide).map(x => lOut(x)))
    )
    val fifoRen = Reg(Bool) init(False)
    fifoRen.setWeakName("fifoRen")
    val faddr1 = Reg(UInt(log2Up(wide) bits)) init(0)
    faddr1.setWeakName("faddr1")
    val faddr2 = Reg(UInt(log2Up(Hout * ChoutD / wide) bits)) init(0)
    faddr2.setWeakName("faddr2")
    when(fifoWen) {
      fifoRen := True
    }.elsewhen(faddr1 === wide - 1 && faddr2 === Hout * ChoutD / wide - 1) {
      fifoRen := False
    }
    when(fifoRen) {
      when(faddr1 < wide - 1) {
        faddr1 := faddr1 + 1
      }.otherwise {
        faddr1 := 0
        when(faddr2 < Hout * ChoutD / wide - 1) {
          faddr2 := faddr2 + 1
        }.otherwise {
          faddr2 := 0
        }
      }
    }
    val fifoOut = Vec(Vec(UInt(Qo bits),Wout),wide)
    fifoOut := fifo.readSync(faddr2)
    val lcOutShift = Vec(Vec(Reg(UInt(Qo bits)) init(0),Wout),wide)
    when(Delay(faddr1 === 0,1,init = False)) {
      lcOutShift := fifoOut
    }.otherwise {
      for(i <- 0 until wide - 1) {
        lcOutShift(i) := lcOutShift(i+1)
      }
    }
    lcOut.payload := lcOutShift(0)
    lcOut.valid   := Delay(fifoRen,2,init = False)

  } else {
    val lOut = Vec(Vec(Reg(UInt(Qo bits)) init(0),Wout),Chout / ChoutDivHard)
    when(lcore.io.valid_out) {
      lOut := lcore.io.data_out
    }.otherwise {
      for(i <- 0 until Chout / ChoutDivHard - 1) {
        lOut(i) := lOut(i+1)
      }
    }
    lcOut.payload := lOut(0)
    val lcOutValid = Reg(Bool) init(False)
    when(lcore.io.valid_out) {
      lcOutValid := True
    }.elsewhen(Delay(lcore.io.valid_out,Chout / ChoutDivHard)) {
      lcOutValid := False
    }
    lcOut.valid   := lcOutValid
  }

  if(noReLu) {
    io.output.payload := Vec(lcOut.payload.map(x => S(x)))
    io.output.valid   := lcOut.valid
  } else {
    val lcSub = Vec(lcOut.payload.map(x => S(x) - SubNum).map(x => RegNext(-x(x.getWidth - 1 downto DivNum)).resize(x.getWidth bits)))
    lcSub.setWeakName("lcSub")
    io.output.payload := ReLu(lcSub)
    io.output.valid   := Delay(lcOut.valid,2,init = False)
  }

}
