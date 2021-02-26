SpinalHDL AdderNet MNIST
============

更新：本项目为 [SpinalResNet](https://github.com/yportne13/SpinalResNet) 项目的前期探索过程，本项目的 commit 记录展示了 ResNet 项目起步阶段的发展历程，这是本项目保留的意义。后续请看 [SpinalResNet](https://github.com/yportne13/SpinalResNet)

这是一个瞎写的理论上可以部署在 FPGA 上的用 SpinalHDL 写的针对 MNIST 的 AdderNet，参考了华为的那篇论文，python代码调用了华为开源的那个代码。

网络结构瞎写的，一个四层的网络。训练也是瞎训练的，权重可能不太好，所以准确率很低，浮点精度 97% 多，量化后就更差了(9659/9998)。

FPGA 代码中 LayerCore 这个核心模块和它下属的模块用比较基础的写法写的，思路就是传统的写 rtl 的思路，可以理解为相当于是 verilog 翻译来的。它的外层 `Layer` `ANN` 等模块用了一些略微高级点的技巧。

就设计思路来说还有不少可优化之处，例如，前两层量化位宽压根不需要这么宽，可以节约大量的资源。再就是不同层计算的所用时间不一样，所以也存在大量资源浪费。

生成代码顶层是 `ANN.scala` 中的 `annTop`，仿真顶层是 `testANN.scala` 中的 `ANNSim` 。

大家看看就好，写这个的目的主要是展示 SpinalHDL 优秀的参数化的能力，和 AdderNet 在 FPGA 中的巨大潜力。

巨大潜力指的是，尽管我没好好做量化（整整 18 比特的量化位宽），但我依然在 xc7z020clg400-1 上用 28460 个 LUT（也就是一半），27 个 36K BRAM 就部署了442 个计算单元（等效于 cnn 中 442 个乘加单元），跑出一张图仅需 864 个 clk，在这种速率等级最低的 FPGA 上都能跑到 165Mhz 的时钟（当然以上所有数据都只是 vivado implement 出来的，没有实际在 FPGA 上测试过）。更新：修改了部分逻辑，现在允许使用两倍的 clk，低于一半的 LUT 实现同样的功能。更改了量化，现在各层的输入输出量化位宽可以单独设置。修改了关键路径。现在的其中一个结果是 864*2 clk，9440 LUT，16 BRAM，250Mhz时钟。

后续可能会抽些时间写一些更详细的东西，或者优化一下代码。

基于 BSD-3 开源
