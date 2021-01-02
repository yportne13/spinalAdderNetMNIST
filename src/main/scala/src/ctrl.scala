/*import spinal.core._
import spinal.lib._

class Ctrl(

) extends Component {
  val io = new Bundle {
    val start = in Bool
    val addw  = out UInt( bits)
    val addr  = out UInt( bits)
    val wen   = out Bool
  }

  val (sIdle,sLoad,sCal,sOut) = (Bits(0,2 bits),Bits(1,2 bits),Bits(2,2 bits),Bits(3,2 bits))
  val state = RegInit(sIdle)
  val cnt = Reg(UInt( bits)) init(0)
  val layer = Reg(UInt( bits)) init(0)
  switch(state) {
    is(sIdle) {
      when(io.start) {
        state := sLoad
      }
    }
    is(sLoad) {
      when(cnt < 28) {
        cnt := cnt + 1
      }.otherwise {
        cnt := 0
        state := sCal
      }
    }
    is(sCal) {
      when() {

      }.otherwise {

      }
    }
  }

}*/