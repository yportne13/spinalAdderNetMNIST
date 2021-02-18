import java.io.{File, FileInputStream}

object LoadWeight {
  def apply(FileName : String, config : List[List[Int]]):List[Array[Int]] = {
    val filew = new File(FileName)
    val inw = new FileInputStream(filew)
    val bytesw = new Array[Byte](filew.length.toInt)
    inw.read(bytesw)
    //get the bytes(i*4)
    inw.close()

    def b2i(i1 : Byte, i2 : Byte): Int = {
      val t1 : Int = i1
      val t2 : Int = i2
      if(t2 >= 0) {
        if(t1 < 0) {
          t2*256+256+t1
        } else {
          t2*256+t1
        }
      } else {
        if(t1 < 0) {
          256*(t2+1) + t1
        }else {
          256*(t2+1)-256+t1
        }
      }
    }

    var idx = 0
    val w = config.map{x => 
      if(x.length == 1) {
        (0 until x(0)).map{y => 
        val ret = b2i(bytesw(4*idx),bytesw(4*idx+1))
        idx = idx + 1 
        ret}.toArray
      }else {
        val w = (0 until x(0)).map{y => 
                val ret = b2i(bytesw(4*idx),bytesw(4*idx+1))
                idx = idx + 1
                ret}.toArray
        val b = (0 until x(0)).map{y => 
                val ret = b2i(bytesw(4*idx),bytesw(4*idx+1))
                idx = idx + 1
                ret}.toArray
        val mean = (0 until x(0)).map{y => 
                val ret = b2i(bytesw(4*idx),bytesw(4*idx+1))
                idx = idx + 1
                ret}.toArray
        val varX = (0 until x(0)).map{y => 
                val ret = b2i(bytesw(4*idx),bytesw(4*idx+1))
                idx = idx + 1
                ret}.toArray
        val weight = (0 until x(0)).map{y => 
                val ret = ( w(y) / scala.math.sqrt(varX(y)) ).toInt
                ret}.toList
        val bias = (0 until x(0)).map{y => 
                val ret = ( b(y) - w(y) * mean(y) / scala.math.sqrt(varX(y)) ).toInt
                ret}.toList
        (weight ::: bias).toArray
      }
    }

    w
  }
}
