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


s="~$property[$p1,$p2?:'lower,$p3[$p1,$p2:'upper]?:$op?,\"Et du texte\"]:'upper:'lower~"

def parse(input):
    stack=[]
    word=''
    for c in input:
        if c in ('[', ']', ',', ':', '~', '?'):
            if word != '':
                stack.append(word)
                if len(stack)>1 and stack[-2] == '#filter':
                    stack.append('#swap')
            word=''
            if c == '[':
                stack.append('#[')
            elif c == ']':
                stack.append('#]')
                stack.append('#msg')
            elif c == '?':
                stack.append("#?")
            elif c == ':':
                stack.append('#filter')
            continue
        word += c
    if word != '':
        stack.append(word)
    return stack

stack = parse(s)

class RpnStack:
    
    def __init__(self, transforms={}):
        self.__stack = []
        self.__transforms = transforms

    def push(self, word):
        if word == '#?':
            if self.__stack[-1].startswith('#'):
                a=self.pop()
                a+='?'
                self.push(a)
        elif word == '#swap':
            a=self.pop()
            b=self.pop()
            self.push(a)
            self.push(b)
        elif word.startswith('#filter'):
            optional='?' in word
            f=self.pop()
            print(f[1:])
            if f.startswith("'"):
                self.push((f[1:], self.__transforms[f[1:]]))
                #raise Exception('Not supported')
        else:
            self.__stack.append(word)

    def pop(self):
        return self.__stack.pop()

    def __getitem__(self, f, t=None):
        if t is None:
            return self.__stack[f]
        else:
            return self.__stack[f:t]

print(s)
#stack=remove_swap(stack)
#stack=remove('#[', '#]', stack)
#print(stack)

stk=RpnStack({'upper': str.upper, 'lower': str.lower })
print(stack)
for e in stack:
    stk.push(e)
print(stk[:])
