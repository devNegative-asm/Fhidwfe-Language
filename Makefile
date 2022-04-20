program: bootstrap_linux.o output.o
	gcc bootstrap_linux.o output.o -o program -no-pie

repl: fhidwfe.jar
	java -jar fhidwfe.jar --mode repl

emu: fhidwfe.jar tniasm.exe
	java -jar fhidwfe.jar compile main.fwf --o tiasm --target z80Emulator --heap-size 15000

bootstrap_linux.o: bootstrap_linux.c
	gcc -c bootstrap_linux.c

output.o: output.asm
	nasm -f elf64 output.asm

output.asm: main.fwf fhidwfe.jar library/*.fwf liblin64/*.fwf
	java -jar fhidwfe.jar compile main.fwf --o output --target LINx64 --heap-size 0

fhidwfe.jar: src/*/*.java
	javac -d bin -cp src src/interfaceCore/Main.java
	cd bin; jar -cfe ../fhidwfe.jar interfaceCore.Main *; cd ..

clean:
	rm *.o output.asm program output.vm tiasm.* last_mem.bin tniasm.sym tniasm.tmp

clean-compiler:
	rm -rf bin
	rm fhidwfe.jar
