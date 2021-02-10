/*import spinal.core._
import spinal.lib._

class ANN(
  Q : Int
) extends Component {
  val io = new Bundle {
    val valid_in = in Bool
    val data_in  = in Vec(SInt(Q bits),28)
  }

  val l1 = new Layer(1,16,2,0,28,28,Q,1)
  l1.io.valid_in := io.valid_in
  l1.io.data_in  := io.data_in
  val l1Out = Vec(Vec(Reg(UInt(Q bits)) init(0),12),16)
  when(l1.io.valid_out) {
    l1Out := l1.io.data_out
  }.otherwise {
    for(i <- 0 until 8 - 1) {
      l1Out(i) := l1Out(i+2)
      l1Out(i+1) := l1Out(i+3)
    }
  }
  val l1fifo = Mem(Vec(Vec(UInt(Q bits),12),2),wordCount = )
  l1fifo.write(
    enable = ,
    address = ,
    data = Vec(l1Out(0),l1Out(1))
  )

  val l2 = new Layer(16,32,2,1,12,12,Q,2)



  val l3 = new Layer(32,16,2,1,6,6,Q,3)



  val l4 = new Layer(16,10,2,0,3,3,Q,4)
}*/