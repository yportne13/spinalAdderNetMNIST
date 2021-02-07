/*import spinal.core._
import spinal.lib._

object MultiCore {
  def apply[T1 <: Data, T2 <: Data](SingIn : Flow[T1], MultiIn : Flow[Vec[T2]], frameLength : Int):(Flow[Vec[T1]], Flow[T2]) = {
    val coreNum = B(MultiIn).getWidth / B(MultiIn(0)).getWidth
    val MultiOut = Flow(Vec(,coreNum))
    val SingOut  = Flow()
    val coreCnt  = Reg(UInt(log2Up(coreNum) bits)) init(0)
    val lenCnt   = Reg(UInt(log2Up(frameLength) bits)) init(0)
    when(SingIn.valid) {
      when(lenCnt < frameLength - 1) {
        lenCnt := lenCnt + 1
      }.otherwise {
        lenCnt := 0
        when(coreCnt < coreNum - 1) {
          coreCnt := coreCnt + 1
        }.otherwise {
          coreCnt := coreCnt + 1
        }
      }
    }
  }
}*/