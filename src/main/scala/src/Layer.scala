import spinal.core._
import spinal.lib._

class Ctrl(
  channel : Int,
  high    : Int,
  Hin     : Int,
  stride : Int,
  padding : Int
) extends Component {
  val io = new Bundle {
    val start = in Bool
    val faddr = out UInt(log2Up(Hin * channel) bits)
    val clear = out Bool
    val shift = out Bool
    val valid = out Bool
  }

  val cnt1 = Reg(UInt(2 bits)) init(0)//shift
  val cntChannel = Reg(UInt(log2Up(channel) bits)) init(0)
  val cnt2 = Reg(UInt(2 bits)) init(0)
  val cntH = Reg(UInt(log2Up(high) bits)) init(0)

  val beforeEnd = Reg(Bool)
  beforeEnd := (cnt1 === 1) && (cntChannel === channel - 1) && (cnt2 === 2) && (cntH === high - 1)
  val en = Reg(Bool) init(False)
  when(io.start) {
    en := True
  }.elsewhen(beforeEnd) {
    en := False
  }

  when(en) {
    when(cnt1 < 2) {
      cnt1 := cnt1 + 1
    }.otherwise {
      cnt1 := 0
    }
  }
  when(cnt1 === 2) {
    when(cntChannel < channel - 1) {
      cntChannel := cntChannel + 1
    }.otherwise {
      cntChannel := 0
    }
  }
  when(cnt1 === 2 && cntChannel === channel - 1) {
    when(cnt2 < 2) {
      cnt2 := cnt2 + 1
    }.otherwise {
      cnt2 := 0
    }
  }
  when(cnt1 === 2 && cntChannel === channel - 1 && cnt2 === 2) {
    when(cntH < high - 1) {
      cntH := cntH + 1
    }.otherwise {
      cntH := 0
    }
  }

  val faddr = Reg(UInt(log2Up(Hin * channel) bits)) init(0)
  faddr := cntChannel + cnt2 * channel + ((cntH - padding) * channel * stride)(log2Up(Hin * channel) - 1 downto 0)

  val clear = Reg(Bool) init(False)
  when(cnt1 === 0 && cntChannel === 0) {
    clear := True
  }.otherwise {
    clear := False
  }

  val shift = Reg(Bool) init(False)
  when(cnt1 > 0) {
    shift := True
  }.otherwise {
    shift := False
  }

  io.faddr := faddr
  io.clear := clear
  io.shift := shift
}

class Layer(
  Chin  : Int,
  Chout : Int,
  stride : Int,
  padding : Int,
  Win        : Int,
  Hin        : Int,
  Wout       : Int,
  Hout       : Int,
  Q          : Int
) extends Component {
  val io = new Bundle {
    val valid_in = in Bool
    val data_in  = in Vec(SInt(Q bits),Win)
    val valid_out = out Bool
    val data_out = out Vec(UInt(Q bits),Wout)
  }

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

  val ctrl = new Ctrl(channel = Chin, high = Hout, Hin = Hin, stride = stride, padding = padding)
  ctrl.io.start := start

  val fifoOut = Vec(SInt(Q bits),Win)
  fifoOut := fifo.readSync(ctrl.io.faddr)
  val peFM = Vec(Reg(SInt(Q bits)) init(0),Win + 2*padding)
  when(ctrl.io.shift) {
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

  val pe = new PE(Wout = Wout, Chout = Chout, Qfm = Q)
  pe.io.clear := ctrl.io.clear
  for(i <- 0 until Wout) {
    pe.io.FM(i) := peFM(i * stride)
  }
  //or to write as:  pe.io.FM := Vec()
  pe.io.W  := Vec((0 until Chout).map(x => S(0)))//toDo

  io.valid_out := False
  io.data_out := pe.io.oup(0)

}

object laye {
  def main(args : Array[String]) {
    SpinalVerilog(new Layer(16,32,2,0,28,28,12,12,32))
  }
}