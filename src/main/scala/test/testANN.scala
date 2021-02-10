import spinal.core._
import spinal.lib._
import spinal.sim._
import spinal.core.sim._

object ANNSim {
  def main(args : Array[String]) {
    val (mat,label) = LoadMNIST()
    SimConfig.withWave.doSim(new ANN(25)){dut =>
      //Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)
      for(idx <- 0 until 3000) {
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
        if(dut.l2.io.output.valid.toBoolean == true) {
          for(i <- 0 until 6) {
            print(dut.l2.io.output.payload(i).toLong + ",")
          }
          println()
        }
        if(dut.io.valid_out.toBoolean == true) {
          print(dut.io.data_out(0).toLong + ",")
          println()
        }
      }
    }
  }
}