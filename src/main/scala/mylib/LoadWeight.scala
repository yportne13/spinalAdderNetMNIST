import java.io.{File, FileInputStream}

object LoadWeight {
  def apply():(Array[Int],Array[Int],Array[Int],Array[Int]) = {
    val filew = new File("param_ann.bin")
    val inw = new FileInputStream(filew)
    val bytesw = new Array[Byte](filew.length.toInt)
    inw.read(bytesw)
    //get the bytes(i*4)
    inw.close()

    val w1 = new Array[Int](16*9)
    val w2 = new Array[Int](32*16*9)
    val w3 = new Array[Int](32*16*9)
    val w4 = new Array[Int](16*10*9)
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
    for(i <- 0 until 16*9) {
      w1(i) = b2i(bytesw(4*i+0),bytesw(4*i+1))//bytesw(4*i+0)
    }
    for(i <- 0 until 32*16*9) {
      w2(i) = b2i(bytesw(4*(i+16*9)),bytesw(4*(i+16*9)+1))//bytesw(4*(i+16*9))
    }
    for(i <- 0 until 32*16*9) {
      w3(i) = b2i(bytesw(4*(i+16*9+32*16*9)),bytesw(4*(i+16*9+32*16*9)+1))//bytesw(4*(i+16*9+32*16*9))
    }
    for(i <- 0 until 16*10*9) {
      w4(i) = b2i(bytesw(4*(i+16*9+32*16*9*2)),bytesw(4*(i+16*9+32*16*9*2)+1))//bytesw(4*(i+16*9+32*16*9*2))
    }
    (w1,w2,w3,w4)
  }
}
