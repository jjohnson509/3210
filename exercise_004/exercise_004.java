

switch(op){
    35:
        mem[sp + 2 + stackCount] = bp + 2 + a;
        stackCount++
        break;
    36:
        int index = mem[bp + 2 + b];
        mem[bp + 2 + a] = mem[index];
        break;

    37:
        int index = mem[bp + 2 + a];
        mem[index] = mem[bp + 2 + b]
        break;
        }