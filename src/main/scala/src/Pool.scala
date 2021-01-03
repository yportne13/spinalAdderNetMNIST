/*import spinal.core._
import spinal.lib._

class Pool(
  Qf : Int = 32,
  Wi : Int = 28
) extends Component {
  val io = new Bundle {
    val inp = in Vec(SInt(Qf bits),Wi)
    val en  = in Bits(2 bits)//0:no input,1:get oup(0 to Wi/2),2:get the first line max,3:get oup(Wi/2 to end)
    val oup = out Vec(SInt(Qf bits),Wi)
  }

  def max(inp1 : SInt, inp2 : SInt, inp3 : SInt): SInt = {
    when(inp1 >= inp2 && inp1 >= inp3) {
      inp1
    }.elsewhen(inp2 >= inp1 && inp2 >= inp3) {
      inp2
    }.otherwise {
      inp3
    }
  }

  val buff = Vec(Reg(SInt(Qf bits)) init(0),Wi/2)
  val oup  = Vec(Reg(SInt(Qf bits)) init(0),Wi)
  val maxin = Vec(Reg(SInt(Qf bits)) init(0),Wi/2)
  val temp = Vec(SInt(Qf bits),Wi/2)
  
  for(i <- 0 until Wi/2) {
    temp(i) := max(io.inp(2*i),io.inp(2*i+1),maxin(i))
  }

  when(io.en(0) === True) {
    for(i <- 0 until Wi/2) {
      maxin(i) := 0
    }
  }.elsewhen(io.en === 2) {
    maxin := temp
  }

  when(io.en =/= 0) {
    buff := temp
  }

  when(RegNext(io.en).init(0) =/= 0) {
    when(RegNext(io.en(1)).init(False) === False) {
      for(i <- 0 until Wi/2) {
        oup(i) := buff(i)
      }
    }.otherwise {
      for(i <- 0 untl Wi/2) {
        oup(i+Wi/2) := buff(i)
      }
    }
  }
  io.oup := oup

}*/