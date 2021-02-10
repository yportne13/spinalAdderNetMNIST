import spinal.core._
import spinal.lib._

class Ctrl(
  Chin : Int,
  high    : Int,
  Hin     : Int,
  stride : Int,
  padding : Int
) extends Component {
  val io = new Bundle {
    val start = in Bool
    val faddr = out UInt(log2Up((Hin+2*padding) * Chin) bits)
    val waddr = out UInt(log2Up(Chin * 9) bits)
    val clear = out Bool
    val shift = out Bool
    val valid = out Bool
  }

  val cnt1 = Reg(UInt(2 bits)) init(0)//shift
  val cntChin = Reg(UInt(log2Up(Chin) bits)) init(0)
  val cnt2 = Reg(UInt(2 bits)) init(0)
  val cntH = Reg(UInt(log2Up(high) bits)) init(0)

  val beforeEnd = Reg(Bool)
  beforeEnd := (cnt1 === 1) && (cntChin === Chin - 1) && (cnt2 === 2) && (cntH === high - 1)
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
  if(Chin > 1) {
    when(cnt1 === 2) {
      when(cntChin < Chin - 1) {
        cntChin := cntChin + 1
      }.otherwise {
        cntChin := 0
      }
    }
  }
  when(cnt1 === 2 && cntChin === Chin - 1) {
    when(cnt2 < 2) {
      cnt2 := cnt2 + 1
    }.otherwise {
      cnt2 := 0
    }
  }
  if(high > 1) {
    when(cnt1 === 2 && cntChin === Chin - 1 && cnt2 === 2) {
      when(cntH < high - 1) {
        cntH := cntH + 1
      }.otherwise {
        cntH := 0
      }
    }
  }

  val faddr = Reg(UInt(log2Up((Hin + 2*padding) * Chin) bits)) init(padding*Chin)
  if(high > 1) {
    faddr := cntChin + cnt2 * Chin + (cntH * Chin * stride)(log2Up(Hin * Chin) - 1 downto 0)
  }else {
    faddr := cntChin + (cnt2 * Chin)(log2Up(Hin * Chin) - 1 downto 0)
  }//TODO

  val waddr = Reg(UInt(log2Up(Chin * 9) bits)) init(0)
  waddr := cntChin * 9 + cnt2 * 3 + cnt1

  val clear = Reg(Bool) init(False)
  when(cnt1 === 0 && cntChin === 0 && cnt2 === 0) {
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

  val valid = Reg(Bool) init(False)
  valid := cnt1 === 2 && cnt2 === 2 && cntChin === Chin - 1

  io.faddr := faddr
  io.waddr := waddr
  io.clear := clear
  io.shift := shift
  io.valid := valid
}
