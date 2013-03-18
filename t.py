#!/bin/python

s="~$property[$p1,$p2:'lower,$p3:$op,\"Et du texte\"]:'upper~"

stack=[]
word=''
for c in s:
    if c in ('[', ']', ',', ':'):
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
print(stack)
