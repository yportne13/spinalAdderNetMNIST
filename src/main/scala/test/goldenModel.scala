import java.io.{File, FileInputStream}
import breeze.linalg._
import breeze.plot._
import Net._

object GoldenModel {
  def main(args : Array[String]) {
    val (mat,label) = LoadMNIST()

    val wList = LoadWeight("param_ann.bin",mnistNoBN().x.weightList)
    //println(w1(8))
    //println(mat(0)(0)(7)(7))

    var suc = 0
    for(i <- 0 until 100) {
      //var l1 = conv2d(mat(i),1,16,wList(0),2,0)
      var l1 = Golden.adder2d(mat(i),1,16,wList(0).map(x => x.toDouble / 64),2,0)
      l1 = l1.map(_.map(_.map(_+10)))
      l1 = l1.map(_.map(_.map(_/4)))
      var r1 = Golden.relu(l1)
      r1 = r1.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l2 = Golden.adder2d(r1,16,32,wList(1).map(x => x.toDouble / 64),2,1)
      l2 = l2.map(_.map(_.map(_+130)))
      l2 = l2.map(_.map(_.map(_/8)))
      var r2 = Golden.relu(l2)
      r2 = r2.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l3 = Golden.adder2d(r2,32,16,wList(2).map(x => x.toDouble / 64),2,1)
      l3 = l3.map(_.map(_.map(_+280)))
      l3 = l3.map(_.map(_.map(_/16)))
      var r3 = Golden.relu(l3)
      r3 = r3.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      val l4 = Golden.adder2d(r3,16,10,wList(3).map(x => x.toDouble / 64),1,0)

      //for(k <- 0 until 1) {
      //  for(j <- 0 until 1) {//mat(0)(0)(k)(j)
      //    //print(-(l1(0)(k)(j)*4-10)*256 + ",")
      //    print(l4(0)(k)(j)*256 + ",")
      //  }
      //  println()
      //}

      var max = l4(0)(0)(0)
      var index = 0
      for(j <- 0 until 10) {
        if(max < l4(j)(0)(0)) {
          max = l4(j)(0)(0)
          index = j
        }
      }
      //for(j <- 0 until 10) {
      //  print(l3(j)+",")
      //}
      //println()
      //println(index + "," + label(i))
      if(index == label(i)) {
        suc = suc + 1
      }
    }
    println(suc)

  }
}

object GoldenBN {
  def main(args : Array[String]) {
    val (mat,label) = LoadMNIST()

    val wList = LoadWeight("mnist.bin",mnistNoBN().x.weightList)
    //println(w1(8))
    //println(mat(0)(0)(7)(7))

    var suc = 0
    for(i <- 0 until 5) {
      var l1 = Golden.adder2d(mat(i),1,16,wList(0).map(x => x.toDouble / 1024),2,0)
      var b1 = Golden.BatchNorm(l1,wList(1).map(x => x.toDouble))
      var r1 = Golden.relu(b1)
      r1 = r1.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l2 = Golden.adder2d(r1,16,32,wList(2).map(x => x.toDouble / 1024),2,1)
      var r2 = Golden.relu(l2)
      r2 = r2.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      var l3 = Golden.adder2d(r2,32,16,wList(2).map(x => x.toDouble / 1024),2,1)
      var r3 = Golden.relu(l3)
      r3 = r3.map(x => x.map(x => x.map(x => scala.math.ceil(x*256)/256)))
      val l4 = Golden.adder2d(r3,16,10,wList(3).map(x => x.toDouble / 1024),1,0)

      //for(k <- 0 until 1) {
      //  for(j <- 0 until 1) {//mat(0)(0)(k)(j)
      //    //print(-(l1(0)(k)(j)*4-10)*256 + ",")
      //    print(l4(0)(k)(j)*256 + ",")
      //  }
      //  println()
      //}

      var max = l4(0)(0)(0)
      var index = 0
      for(j <- 0 until 10) {
        if(max < l4(j)(0)(0)) {
          max = l4(j)(0)(0)
          index = j
        }
      }
      //for(j <- 0 until 10) {
      //  print(l3(j)+",")
      //}
      //println()
      //println(index + "," + label(i))
      if(index == label(i)) {
        suc = suc + 1
      }
    }
    println(suc)

  }
}