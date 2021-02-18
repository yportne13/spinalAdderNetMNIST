import java.io.{File, FileInputStream}

object LoadWeight {
  def apply(FileName : String, config : List[Int]):List[Array[Int]] = {
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
    val w = config.map(x => (0 until x).map{y => 
      val ret = b2i(bytesw(4*idx),bytesw(4*idx+1))
      idx = idx + 1 
      ret}.toArray)
      
    w
  }
}
