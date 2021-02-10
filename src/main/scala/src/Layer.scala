import spinal.core._
import spinal.lib._

class Layer(
  Chin  : Int,
  Chout : Int,
  stride : Int,
  padding : Int,
  Win        : Int,
  Hin        : Int,
  Q          : Int,
  layer      : Int,
  SubNum     : Int,
  DivNum     : Int,
  noReLu     : Boolean = false
) extends Component {

  val Wout = (Win + 2 * padding) / stride + padding - 2
  val Hout = (Hin + 2 * padding) / stride + padding - 2

  val io = new Bundle {
    val input = in (Flow(Vec(SInt(Q bits),Win)))
    val output = out (Flow(Vec(SInt(Q bits),Wout)))
  }

  val lcore = new LayerCore(Chin,Chout,stride,padding,Win,Hin,Q,layer)
  lcore.io.valid_in := io.input.valid
  lcore.io.data_in  := io.input.payload

  val lcOut = Flow(Vec(UInt(Q bits),Wout))
  if(9*Chin < Chout) {
    val wide = scala.math.ceil(Chout / 9.0*Chin).toInt
    val lOut = Vec(Vec(Reg(UInt(Q bits)) init(0),Wout),Chout)
    when(lcore.io.valid_out) {
      lOut := lcore.io.data_out
    }.otherwise {
      for(i <- 0 until Chout/wide - 1) {
        (0 until wide).map(x => lOut(i * wide + x) := lOut(i * wide + x + wide))
      }
    }
    val fifoWen = Reg(Bool) init(False)
    when(lcore.io.valid_out) {
      fifoWen := True
    }.elsewhen(Delay(lcore.io.valid_out,Chout * Hout / wide,init = False)) {
      fifoWen := False
    }
    val fifoAddw = Reg(UInt(log2Up(Chout * Hout / wide) bits)) init(0)
    when(fifoWen) {
      when(fifoAddw < Chout * Hout / wide - 1) {
        fifoAddw := fifoAddw + 1
      }.otherwise {
        fifoAddw := 0
      }
    }
    val fifo = Mem(Vec(Vec(UInt(Q bits),Wout),wide),wordCount = Hout * Chout / wide)
    fifo.write (
      address = fifoAddw,
      enable  = fifoWen,
      data    = Vec((0 until wide).map(x => lOut(x)))
    )
    val fifoRen = Reg(Bool) init(False)
    val faddr1 = Reg(UInt(log2Up(wide) bits)) init(0)
    val faddr2 = Reg(UInt(log2Up(Hout * Chout / wide) bits)) init(0)
    when(fifoWen) {
      fifoRen := True
    }.elsewhen(faddr1 === wide - 1 && faddr2 === Hout * Chout / wide - 1) {
      fifoRen := False
    }
    when(fifoRen) {
      when(faddr1 < wide - 1) {
        faddr1 := faddr1 + 1
      }.otherwise {
        faddr1 := 0
        when(faddr2 < Hout * Chout / wide - 1) {
          faddr2 := faddr2 + 1
        }.otherwise {
          faddr2 := 0
        }
      }
    }
    val fifoOut = Vec(Vec(UInt(Q bits),Wout),wide)
    fifoOut := fifo.readSync(faddr2)
    val lcOutShift = Vec(Vec(Reg(UInt(Q bits)) init(0),Wout),wide)
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
    val lOut = Vec(Vec(Reg(UInt(Q bits)) init(0),Wout),Chout)
    when(lcore.io.valid_out) {
      lOut := lcore.io.data_out
    }.otherwise {
      for(i <- 0 until Chout - 1) {
        lOut(i) := lOut(i+1)
      }
    }
    lcOut.payload := lOut(0)
    val lcOutValid = Reg(Bool) init(False)
    when(lcore.io.valid_out) {
      lcOutValid := True
    }.elsewhen(Delay(lcore.io.valid_out,Chout)) {
      lcOutValid := False
    }
    lcOut.valid   := lcOutValid
  }

  if(noReLu) {
    io.output.payload := Vec(lcOut.payload.map(x => S(x)))
    io.output.valid   := lcOut.valid
  } else {
    val lcSub = Vec(lcOut.payload.map(x => S(x) - SubNum).map(x => RegNext(-x(x.getWidth - 1 downto DivNum)).resize(x.getWidth bits)))
    io.output.payload := ReLu(lcSub)
    io.output.valid   := Delay(lcOut.valid,2,init = False)
  }

}
