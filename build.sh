nasm -f elf64 output.asm
gcc -c bootstrap_linux.c
gcc bootstrap_linux.o output.o -o a.out -no-pie
