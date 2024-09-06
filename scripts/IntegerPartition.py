from typing import List # For annotations

def GetPartitions (numset : List[int], biggernum : int) :

    size_numset = len(numset)
    dptable = [0] * (size_numset+1)

    for r in range (size_numset+1) :
        dptable[r] = [0] * (biggernum+1)

    # Number 0 can be obtained with 0 smaller numbers i.e 1 way
    dptable[0][0] = 1

    # Numbers greater that 0 cannot be obtained with number 0
    for c in range (1, biggernum+1) :
        dptable[0][c] = 0

        # Number 0 can be obtained with any number i.e we do not include any number from the set
    for r in range (1, size_numset+1) :
        dptable[r][0] = 1

    for r in range (1, size_numset+1) :
        for c in range (1, biggernum+1) :
            if (c >= numset[r-1]) :
                dptable[r][c] = dptable[r-1][c] + dptable[r][c-numset[r-1]]
            else :
                dptable[r][c] = dptable[r-1][c]

    return dptable[size_numset][biggernum]
num = 3
numset = list(range(1, num+1))
print(GetPartitions(numset, num))

num = 5
numset = list(range(1, num+1))
print(GetPartitions(numset, num))

num = 10
numset = list(range(1, num+1))
print(GetPartitions(numset, num))




