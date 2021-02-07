import spinal.core._
import spinal.lib._
import spinal.sim._
import spinal.core.sim._

object LayerSim {
  def main(args : Array[String]) {
    val (mat,label) = LoadMNIST()
    SimConfig.withWave.doSim(new Layer(1,16,2,0,28,28,12,12,32,1)){dut =>
      //Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)
      for(idx <- 0 until 1000) {
        if(idx > 2 && idx <= 30) {
          dut.io.valid_in #= true
          for(i <- 0 until 28) {
            dut.io.data_in(i) #= (mat(0)(0)(idx - 3)(i)*256).toInt
          }
        }else {
          dut.io.valid_in #= false
          for(i <- 0 until 28) {
            dut.io.data_in(i) #= 0
          }
        }
        dut.clockDomain.waitRisingEdge()
        if(dut.io.valid_out.toBoolean == true) {
          for(i <- 0 until 12) {
            print(dut.io.data_out(i).toLong + ",")
          }
          println()
        }
      }
    }
  }
}