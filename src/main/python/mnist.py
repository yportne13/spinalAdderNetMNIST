from __future__ import print_function
import argparse
import adder
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from torchvision import datasets, transforms
from torch.optim.lr_scheduler import StepLR
import math

def conv3x3(in_planes, out_planes, kernel_size , stride, padding):
    " 3x3 convolution with padding "
    #return nn.Conv2d(in_planes,out_planes,kernel_size,stride,padding,bias=False)
    return adder.adder2d(in_planes, out_planes, kernel_size=kernel_size, stride=stride, padding=padding, bias=False)

class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        #self.conv1 = nn.Conv2d(1, 16, 3, 2, 0)#12
        #self.conv2 = nn.Conv2d(16, 32, 3, 2, 1)#6
        #self.conv3 = nn.Conv2d(32,16,3,2,1)#3
        #self.conv4 = nn.Conv2d(16,10,3,1,0)
        self.conv1 = conv3x3(1, 16, 3, 2, 0)#12
        self.bn1 = nn.BatchNorm2d(16)
        self.conv2 = conv3x3(16, 32, 3, 2, 1)#6
        self.bn2 = nn.BatchNorm2d(32)
        self.conv3 = conv3x3(32,16,3,2,1)#3
        self.bn3 = nn.BatchNorm2d(16)
        self.conv4 = conv3x3(16,10,3,2,0)
        self.bn4 = nn.BatchNorm2d(10)
        #self.conv1 = conv3x3(1, 16)
        #self.conv2 = conv3x3(16, 32)
        #self.dropout1 = nn.Dropout(0.25)
        #self.dropout2 = nn.Dropout(0.5)
        #self.fc1 = nn.Linear(6272, 10)

    def forward(self, x):
        #x = (x*128+55).int().float()
        #x = (x*256).int().float()
        x = x - 0.5
        x = self.conv1(x)
        #x = self.bn1(x)
        x = x + 10
        x = x / 4
        #print(torch.max(x[0]))
        x = F.relu(x)
        x = self.conv2(x)
        #x = self.bn2(x)
        x = x + 130
        x = x / 8
        x = F.relu(x)
        x = self.conv3(x)
        #x = self.bn3(x)
        x = x + 280
        x = x / 16
        x = F.relu(x)
        x = self.conv4(x)
        # x = x + 160
        # x = x / 2

        #x = self.bn4(x)
        #x = F.relu(x)

        #x = F.max_pool2d(x, 2)
        #x = self.dropout1(x)
        x = torch.flatten(x, 1)
        #x = self.fc1(x)
        #x = F.relu(x)
        #x = self.dropout2(x)
        #x = self.fc2(x)
        output = F.log_softmax(x, dim=1)
        return output

def adjust_learning_rate(optimizer, epoch):
    """For resnet, the lr starts from 0.1, and is divided by 10 at 80 and 120 epochs"""
    lr = 0.05 * (1+math.cos(float(epoch)/400*math.pi))
    for param_group in optimizer.param_groups:
        param_group['lr'] = lr

def train(args, model, device, train_loader, optimizer, epoch):
    #adjust_learning_rate(optimizer, epoch)
    criterion = torch.nn.CrossEntropyLoss().cuda()
    model.train()
    for batch_idx, (data, target) in enumerate(train_loader):
        data, target = data.to(device), target.to(device)
        optimizer.zero_grad()
        output = model(data)
        loss = criterion(output, target)
        #loss = F.nll_loss(output, target)
        loss.backward()
        optimizer.step()
        if batch_idx % args.log_interval == 0:
            print('Train Epoch: {} [{}/{} ({:.0f}%)]\tLoss: {:.6f}'.format(
                epoch, batch_idx * len(data), len(train_loader.dataset),
                100. * batch_idx / len(train_loader), loss.item()))
            if args.dry_run:
                break


def test(model, device, test_loader):
    model.eval()
    test_loss = 0
    correct = 0
    with torch.no_grad():
        for data, target in test_loader:
            data, target = data.to(device), target.to(device)
            output = model(data)
            test_loss += F.nll_loss(output, target, reduction='sum').item()  # sum up batch loss
            pred = output.argmax(dim=1, keepdim=True)  # get the index of the max log-probability
            correct += pred.eq(target.view_as(pred)).sum().item()

    test_loss /= len(test_loader.dataset)

    print('\nTest set: Average loss: {:.4f}, Accuracy: {}/{} ({:.0f}%)\n'.format(
        test_loss, correct, len(test_loader.dataset),
        100. * correct / len(test_loader.dataset)))


def main():
    # Training settings
    parser = argparse.ArgumentParser(description='PyTorch MNIST Example')
    parser.add_argument('--batch-size', type=int, default=64, metavar='N',
                        help='input batch size for training (default: 64)')
    parser.add_argument('--test-batch-size', type=int, default=64, metavar='N',
                        help='input batch size for testing (default: 1000)')
    parser.add_argument('--epochs', type=int, default=54, metavar='N',
                        help='number of epochs to train (default: 14)')
    parser.add_argument('--lr', type=float, default=1.0, metavar='LR',
                        help='learning rate (default: 1.0)')
    parser.add_argument('--gamma', type=float, default=0.7, metavar='M',
                        help='Learning rate step gamma (default: 0.7)')
    parser.add_argument('--no-cuda', action='store_true', default=False,
                        help='disables CUDA training')
    parser.add_argument('--dry-run', action='store_true', default=False,
                        help='quickly check a single pass')
    parser.add_argument('--seed', type=int, default=1, metavar='S',
                        help='random seed (default: 1)')
    parser.add_argument('--log-interval', type=int, default=100, metavar='N',
                        help='how many batches to wait before logging training status')
    parser.add_argument('--save-model', action='store_true', default=False,
                        help='For Saving the current Model')
    args = parser.parse_args()
    use_cuda = False#not args.no_cuda and torch.cuda.is_available()

    torch.manual_seed(args.seed)

    device = torch.device("cuda" if use_cuda else "cpu")

    train_kwargs = {'batch_size': args.batch_size}
    test_kwargs = {'batch_size': args.test_batch_size}
    if use_cuda:
        cuda_kwargs = {'num_workers': 1,
                       'pin_memory': True,
                       'shuffle': True}
        train_kwargs.update(cuda_kwargs)
        test_kwargs.update(cuda_kwargs)

    transform=transforms.Compose([
        transforms.ToTensor()#,
        #transforms.Normalize((0.1307,), (0.3081,))
        ])
    dataset1 = datasets.MNIST('../data', train=True, download=True,
                       transform=transform)
    dataset2 = datasets.MNIST('../data', train=False,
                       transform=transform)
    train_loader = torch.utils.data.DataLoader(dataset1,**train_kwargs)
    test_loader = torch.utils.data.DataLoader(dataset2, **test_kwargs)

    model = Net().to(device)
    optimizer = optim.Adadelta(model.parameters(), lr=args.lr)
    model.load_state_dict(torch.load('mnist_cnn.pt'))####
    #print(model.parameters())
    cnt = 0
    param_list=[]
    for param in model.parameters():
        cnt = cnt + 1
        
        if cnt == 12 or cnt == 9 or cnt == 6 or cnt == 3 or cnt == 11 or cnt == 2 or cnt == 8 or cnt == 5:
            param.data = param.data - param.data
        elif cnt == 1 or cnt == 4 or cnt == 7 or cnt == 10:
            param.data = param.data * 1024 / 16
            
            param.data = param.data.int().float() / 1024 * 16
            
        print(cnt)
        print(torch.max(param))
        print(torch.min(param))
    test(model, device, test_loader)####
    cnt = 0
    for key, value in model.state_dict().items():
        cnt = cnt + 1
        if cnt == 1 or cnt == 7 or cnt == 13 or cnt == 19:
            print(key)
            flat_weight = value.contiguous().view(value.numel())
            param_list.extend(flat_weight.tolist())
    idx = 0
    
    for i in param_list:
        param_list[idx] = int(i/16*1024)
        idx = idx + 1
    print(param_list[-1])
    import struct
    fp = open("param_v19best.bin",'wb')
    s = struct.pack('i'*len(param_list), *param_list)
    fp.write(s)
    #scheduler = StepLR(optimizer, step_size=1, gamma=args.gamma)
    #for epoch in range(1, args.epochs + 1):
    #    
    #    train(args, model, device, train_loader, optimizer, epoch)
    #    test(model, device, test_loader)####
    #    scheduler.step()
    #    torch.save(model.state_dict(), "mnist_cnn.pt")


if __name__ == '__main__':
    main()