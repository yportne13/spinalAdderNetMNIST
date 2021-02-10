import spinal.core._
import spinal.lib._
import spinal.sim._
import spinal.core.sim._

object ANNSim {
  def main(args : Array[String]) {
    val (mat,label) = LoadMNIST()
    var oCnt = 0
    var successCnt = 0
    var delay = 900
    SimConfig.doSim(new ANN(18)){dut =>//.withWave
      //Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)
      for(idx <- 0 until 9000000) {
        if(idx%delay > 2 && idx%delay <= 30) {
          dut.io.valid_in #= true
          for(i <- 0 until 28) {
            dut.io.data_in(i) #= (mat(idx/delay)(0)(idx%delay - 3)(i)*256).toInt
          }
        }else {
          dut.io.valid_in #= false
          for(i <- 0 until 28) {
            dut.io.data_in(i) #= 0
          }
        }
        dut.clockDomain.waitRisingEdge()
        //if(dut.l2.io.output.valid.toBoolean == true) {
        //  for(i <- 0 until 6) {
        //    print(dut.l2.io.output.payload(i).toLong + ",")
        //  }
        //  println()
        //}
        if(dut.io.output.valid.toBoolean == true) {
          //print(dut.io.output.payload.toLong + ",")
          //print(label(oCnt))
          //println()
          if(label(oCnt) == dut.io.output.payload.toLong) {
            successCnt = successCnt + 1
          }
          oCnt = oCnt + 1
        }
        if(idx%(delay*100) == 100) {
          println(successCnt + ";" + oCnt)
        }
      }
    }
    println(successCnt + ";" + oCnt)
  }
}