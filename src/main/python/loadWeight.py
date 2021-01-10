from __future__ import print_function
import argparse
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from torchvision import datasets, transforms
from torch.optim.lr_scheduler import StepLR

class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        self.conv1 = nn.Conv2d(1, 16, 3, 1, 1)
        self.conv2 = nn.Conv2d(16, 32, 3, 1, 1)
        self.dropout1 = nn.Dropout(0.25)
        self.dropout2 = nn.Dropout(0.5)
        self.fc1 = nn.Linear(6272, 10)
        #self.fc2 = nn.Linear(64, 10)

    def forward(self, x):
        x = (x*256).int().float()
        poem = open('./fm0.txt', 'w')
        poem.write(str((x[0]).int()))
        x = self.conv1(x)
        x = x.int().float()
        poem = open('./fm1.txt','w')
        poem.write(str(x[0][0].int()))
        x = F.relu(x)
        x = self.conv2(x)
        x = x.int().float()
        poem = open('./fm2.txt','w')
        poem.write(str(x[0][0].int()))
        x = F.relu(x)
        
        x = F.max_pool2d(x, 2)
        #x = self.dropout1(x)
        x = torch.flatten(x, 1)
        x = self.fc1(x)
        x = x.int().float()
        #x = F.relu(x)
        #x = self.dropout2(x)
        #x = self.fc2(x)
        #output = F.log_softmax(x, dim=1)
        output = x
        return output

device = torch.device("cpu")
transform=transforms.Compose([
        transforms.ToTensor()#,
        #transforms.Normalize((0.1307,), (0.3081,))
        ])
test_kwargs = {'batch_size': 10000}
dataset2 = datasets.MNIST('../data', train=False,
                       transform=transform)
test_loader = torch.utils.data.DataLoader(dataset2, **test_kwargs)

model = Net().to(device)
model.load_state_dict(torch.load('mnist_cnn.pt'))

for param in model.parameters():
    #new = torch.zeros_like(param.data)
    #param.data = int(param.data * 128)#torch.where(param.data>0, param.data, new)
    for i in range(7):
        param.data = param.data + param.data
    param.data = (param.data.int().float()/128)

correct = 0
for data, target in test_loader:
    data, target = data.to(device), target.to(device)
    output = model(data)
    print(output[0])
    pred = output.argmax(dim=1, keepdim=True)  # get the index of the max log-probability
    correct += pred.eq(target.view_as(pred)).sum().item()
print(correct)

#######
#model = torch.load('mnist_cnn.pt')
#print(model)
param_list=[]
for key, value in model.state_dict().items():
    flat_weight = value.contiguous().view(value.numel())
    param_list.extend(flat_weight.tolist())
idx = 0
for i in param_list:
    if idx < (160+32+32*16*9):
        param_list[idx] = int(float(i)*128)
    else:
        param_list[idx] = int(float(i)*128)
    idx = idx + 1

print(param_list[1])
print(len(param_list))

import struct
fp = open("param_v19best.bin",'wb')
s = struct.pack('i'*len(param_list), *param_list)
fp.write(s)