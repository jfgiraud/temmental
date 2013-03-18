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


s="~$property[$p1,$p2:'lower,$p3[$p1,$p2:'upper]:$op,\"Et du texte\"]:'upper:'lower~"

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


def search(b, e, stack):
    i=0
    c=0
    x=y=None
    for e in stack[::]:
        if e == '#[':
            x=i 
            c+=1
        elif e == '#]':
            c-=1
            y=i
        i+=1
        if y is not None:
            return(x, y)
    return (x,y)

def remove(b, e, stack):
    print stack
    (x,y) = search(b, e, stack)
    if (x is not None) ^ (y is not None):
        raise Exception('Invalid syntax.')
    if x is not None:
        stack.insert(y+1, y-x-1)
        stack.insert(y+2, '#array')
        del stack[y]
        del stack[x]
        return remove(b, e, stack)
    else:
        return stack

stack=remove('#[', '#]', stack)
print(stack)
