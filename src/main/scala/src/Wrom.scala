import spinal.core._
import spinal.lib._

class Wrom(
  Qw : Int = 10,
  Chout : Int,
  ChoutDivHard : Int,
  layer : Int
) extends Component {

  val wList = LoadWeight("param_ann.bin",mnistNoBN().x.weightList)
  var romDepth = 0
  var w : Array[Int] = Array(0)
  w = wList(layer - 1)
  romDepth = w.length / Chout
  romDepth = romDepth * ChoutDivHard

  val io = new Bundle {
    val addr = in UInt(log2Up(romDepth) bits)
    val w    = out Vec(SInt(Qw bits),Chout / ChoutDivHard)
  }

  def Wdata = for(i <- 0 until romDepth) yield {
    val a = i % (romDepth / ChoutDivHard)
    Vec((0 until Chout / ChoutDivHard).map(x => S(w(romDepth / ChoutDivHard * (x + i/(romDepth/ChoutDivHard)*(Chout/ChoutDivHard)) + a)*256/64,Qw bits)))
  }
  val rom = Mem(Vec(SInt(Qw bits),Chout / ChoutDivHard),initialContent = Wdata)
  io.w := rom.readSync(io.addr)
}