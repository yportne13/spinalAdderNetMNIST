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
    val faddr = out UInt(log2Up(Hin * Chin) bits)
    val waddr = out UInt(log2Up(Chin * 9) bits)
    val clear = out Bool
    val shift = out Bool
    val valid = out Bool
  }

  val cnt1 = Reg(UInt(2 bits)) init(0)//shift
  val cntChannel = Reg(UInt(log2Up(Chin) bits)) init(0)
  val cnt2 = Reg(UInt(2 bits)) init(0)
  val cntH = Reg(UInt(log2Up(high) bits)) init(0)

  val beforeEnd = Reg(Bool)
  beforeEnd := (cnt1 === 1) && (cntChannel === Chin - 1) && (cnt2 === 2) && (cntH === high - 1)
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
      when(cntChannel < Chin - 1) {
        cntChannel := cntChannel + 1
      }.otherwise {
        cntChannel := 0
      }
    }
  }
  when(cnt1 === 2 && cntChannel === Chin - 1) {
    when(cnt2 < 2) {
      cnt2 := cnt2 + 1
    }.otherwise {
      cnt2 := 0
    }
  }
  when(cnt1 === 2 && cntChannel === Chin - 1 && cnt2 === 2) {
    when(cntH < high - 1) {
      cntH := cntH + 1
    }.otherwise {
      cntH := 0
    }
  }

  val faddr = Reg(UInt(log2Up(Hin * Chin) bits)) init(0)
  faddr := cntChannel + cnt2 * Chin + ((cntH - padding) * Chin * stride)(log2Up(Hin * Chin) - 1 downto 0)

  val waddr = Reg(UInt(log2Up(Chin * 9) bits)) init(0)
  waddr := cntChannel * 9 + cnt2 * 3 + cnt1

  val clear = Reg(Bool) init(False)
  when(cnt1 === 0 && cntChannel === 0 && cnt2 === 0) {
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
  valid := cnt1 === 2 && cnt2 === 2 && cntChannel === Chin - 1

  io.faddr := faddr
  io.waddr := waddr
  io.clear := clear
  io.shift := shift
  io.valid := valid
}
