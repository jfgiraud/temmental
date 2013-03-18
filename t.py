#!/bin/python

'''
~$property #msg~


~
$property 
$p1 
$p2 'lower #filter 
$p3 $op #filter 
3 #array 
#msg 
'upper #filter
~

~$property[$p1,$p2:'lower,$p3:$op]:'upper~
'''


s="~$property[$p1,$p2:'lower,$p3:$op,\"Et du texte\"]:'upper~"

def parse(input):
    stack=[]
    word=''
    for c in input:
        if c in ('[', ']', ',', ':', '~'):
            if word != '':
                stack.append(word)
                if len(stack)>1 and stack[-2] == '#filter':
                    stack.append('#swap')
            word=''
            if c == '[':
                stack.append('#[')
            if c == ']':
                stack.append('#]')
                stack.append('#msg')
            if c == ':':
                stack.append('#filter')
            continue
        word += c
    if word != '':
        stack.append(word)
    return stack

stack = parse(s)

i=0
c=0
for e in stack:
    if e == '#[':
        if c == 0:
            print(i) 
        c+=1
    elif e == '#]':
        c-=1
        if c == 0:
            print(i) 
    i+=1
        

print(stack)

