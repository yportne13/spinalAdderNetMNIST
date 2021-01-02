from __future__ import print_function
import argparse
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from torchvision import datasets, transforms
from torch.optim.lr_scheduler import StepLR

#model = eval()
model = torch.load('mnist_cnn.pt')
#print(model)

param_list=[]
for key, value in model.items():
    flat_weight = value.contiguous().view(value.numel())
    param_list.extend(flat_weight.tolist())
idx = 0
for i in param_list:
    param_list[idx] = int(float(i)*128)
    idx = idx + 1

print(param_list[1])
print(len(param_list))

import struct
fp = open("param_v19best.bin",'wb')
s = struct.pack('i'*len(param_list), *param_list)
fp.write(s)