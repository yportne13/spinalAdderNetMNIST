/*import spinal.core._
import spinal.lib._

class Lfifo(
  Q : Int,
  W : Int,
  C : Int,
  Depth : Int
) extends Component {
  val io = new Bundle {
    val valid_in = in Bool
    val data_in = in Vec(Vec(SInt(Q bits),W),C)
    val valid_out = out Bool
    val data_out = out Vec(SInt(Q bits),W)
  }

  val lOut = Vec(Vec(Reg(UInt(Q bits)) init(0),12),16)
  when(io.valid_in) {
    lOut := io.data_in
  }.otherwise {
    for(i <- 0 until C - 1) {
      lOut(i) := lOut(i+1)
    }
  }
  val wen = Reg(Bool) init(False)
  val wCnt = Reg(UInt(log2Up(C) bits)) init(0)
  when(io.valid_in) {
    wen := True
  }.elsewhen() {
    wen := False
  }
  when(wen) {
    when(wCnt < C - 1) {
      wCnt := wCnt + 1
    }.otherwise {
      wCnt := 0
    }
  }

  val addw = Reg(UInt(log2Up(Depth) bits)) init(0)
  when(wen) {
    when(addw < Depth - 1) {
      addw := addw + 1
    }.otherwise {
      addw := 0
    }
  }

  val lmem = Mem(Vec(SInt(Q bits),W),wordCount = Depth)
  lmem.write(
    address = addw,
    data    = lOut(0),
    enable  = wen
  )

  val valid_out = Reg(Bool) init(False)
  val addr = Reg(UInt(log2Up(Depth) bits)) init(0)
  when() {
    valid_out := True
  }.elsewhen() {
    valid_out := False
  }
  when() {
    when(addr < Depth - 1) {
      addr := addr + 1
    }.otherwise {
      addr := 0
    }
  }
  io.valid_out := 
  io.data_out := lem.readSync(addr)


}*/
