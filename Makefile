
program: bootstrap_linux.o output.o
	gcc bootstrap_linux.o output.o -o program -no-pie

bootstrap_linux.o: bootstrap_linux.c
	gcc -c bootstrap_linux.c

output.o: output.asm
	nasm -f elf64 output.asm

output.asm: main.fwf
	java -jar fhidwfe.jar main.fwf output LINx64 0

clean:
	rm *.o output.asm program
