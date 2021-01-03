/*import spinal.core._
import spinal.lib._

class Ctrl(
  
) extends Component {
  val io = new Bundle {
    val start = in Bool
    val faddw  = out UInt( bits)
    val faddr  = out UInt( bits)
    val waddr  = out UInt( bits)
    val baddr  = out UInt( bits)
    val we     = out Bool
  }

  val (sIdle,sLoad,sCal,sOut) = (Bits(0,2 bits),Bits(1,2 bits),Bits(2,2 bits),Bits(3,2 bits))
  val state = RegInit(sIdle)
  val shift = Reg(UInt(2 bits)) init(0)
  val addShift = Reg(UInt(2 bits)) init(0)
  val cnt = Reg(UInt(5 bits)) init(1)
  val cnt2 = Reg(UInt(5 bits)) init(0)
  val cnt3 = Reg(UInt(5 bits)) init(0)
  val layer = Reg(UInt(2 bits)) init(0)
  switch(state) {
    is(sIdle) {
      when(io.start) {
        state := sLoad
      }
    }
    is(sLoad) {
      when(cnt < 28) {
        cnt := cnt + 1
      }.otherwise {
        cnt := 1
        state := sCal
      }
    }
    is(sCal) {
      switch(layer) {
        is(0) {
          when(shift < 2) {
            shift := shift + 1
          }.otherwise {
            shift := 0
            when(addShift < 2) {
              addShift := addShift + 1
            }.otherwise {
              addShift := 0
              when(cnt < 28) {
                cnt := cnt + 1
              }.otherwise {
                cnt := 1
                when(cnt3 < 15) {
                  cnt3 := cnt3 + 1
                }.otherwise {
                  cnt3 := 0
                  layer := layer  + 1
                }
              }
            }
          }
        }
        is(1) {
          when(shift < 2) {
            shift := shift + 1
          }.otherwise {
            shift := 0
            when(addShift < 2) {
              addShift := addShift + 1
            }.otherwise {
              addShift := 0
              when(cnt2 < 15) {
                cnt2 := cnt2 + 1
              }.otherwise {
                cnt2 := 0
                when(cnt < 28) {
                  cnt := cnt + 1
                }.otherwise {
                  cnt := 1
                  when(cnt3 < 31) {
                    cnt3 := cnt3 + 1
                  }.otherwise {
                    cnt3 := 0
                    layer := layer + 1
                  }
                }
              }
            }
          }
        }
        is(2) {
          when(cnt2 < 31) {
            cnt2 := cnt2 + 1
          }.otherwise {
            cnt2 := 0
            when(cnt < 14) {
              cnt := cnt + 1
            }.otherwise {
              cnt := 1
              when(cnt3 < 9) {
                cnt3 := cnt3 + 1
              }.otherwise {
                cnt3 := 0
                state := sOut
                layer := 0
              }
            }
          }
        }
      }
    }
    is(sOut) {
      state := sIdle
    }
  }

  val faddr = Reg(UInt(11 bits)) init(0)
  faddr(10 downto 1) := addShift + cnt + cnt2 * 30 - 1
  faddr(0) := layer(0)
  val faddw = Reg(UInt(11 bits)) init(0)
  faddw(10 downto 1) := cnt + cnt3 * 30
  faddr(0) := !layer(0)

  val waddr = Reg(UInt( bits)) init(0)
  waddr := 
  val baddr = Reg(UInt( bits)) init(0)
  baddr := 


}*/