/*import spinal.core._
import spinal.lib._

class FM(
  Qi : Int = 10,
  Wi : Int = 28,
  Dep: Int
) extends Component {
  val io = new Bundle {
    val din = in Vec(SInt(Qi bits),Wi)
    val addw = in UInt(log2Up(Dep) bits)
    val we   = in Bool
    val addr = in UInt(log2Up(Dep) bits)
    val dout = out Vec(SInt(Qi bits),Wi)
  }

  val ram = Mem(Vec(SInt(Qi bits),Wi),wordCount = Dep)
  ram.write(
    data = io.din,
    address = io.addw,
    enable  = io.we
  )
  io.dout := ram.readSync(io.addr)
}

class Weight(
  DepW: Int,//weight rom depth
  DepB: Int//bias rom depth
) extends Component {
  import java.io.{File, FileInputStream}

  val file = new File("param_v19best.bin")
  val infile = new FileInputStream(file)
  val bytes = new Array[Byte](file.length.toInt)
  infile.read(bytes)

  val io = new Bundle {
    val waddr = in UInt(log2Up(DepW) bits)
    val baddr = in UInt(log2Up(DepB) bits)
    val W    = out SInt(8 bits)
    val bias = out SInt(8 bits)
  }

  val wdata = for(i <- 0 until DepW) yield {
    if(i < ) {
      Vec()
    }
  }
  val wrom = Mem(SInt(8 bits),initialContent = wdata)
  io.W := wrom.readSync(io.waddr)

  val bdata = for(i <- 0 until DepB) yield {

  }
  val brom = Mem(SInt(8 bits),initialContent = bdata)
  io.bias := brom.readSync(io.baddr)
}*/