import spinal.core._
import spinal.lib._

class Wrom(
  Qw : Int = 10,
  Chout : Int,
  layer : Int
) extends Component {

  val (w1,w2,w3,w4) = LoadWeight()
  var romDepth = 0
  var w : Array[Int] = Array(0)
  if(layer == 1) {
    w = w1
    romDepth = w1.length / Chout
  }else if(layer == 2) {
    w = w2
    romDepth = w2.length / Chout
  }else if(layer == 3) {
    w = w3
    romDepth = w3.length / Chout
  }else if(layer == 4) {
    w = w4
    romDepth = w4.length / Chout
  }

  val io = new Bundle {
    val addr = in UInt(log2Up(romDepth) bits)
    val w    = out Vec(SInt(Qw bits),Chout)
  }

  def Wdata = for(i <- 0 until romDepth) yield {
    val a = i%9
    //val a = b%3*3+b/3//TODO
    val chin = i/9
    Vec((0 until Chout).map(x => S(w(chin * 9 * Chout + x * 9 + a)*256/64,Qw bits)))
  }
  val rom = Mem(Vec(SInt(Qw bits),Chout),initialContent = Wdata)
  io.w := rom.readSync(io.addr)
}