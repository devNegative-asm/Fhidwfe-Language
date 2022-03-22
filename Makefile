
program: bootstrap_linux.o output.o
	gcc bootstrap_linux.o output.o -o program -no-pie

bootstrap_linux.o: bootstrap_linux.c
	gcc -c bootstrap_linux.c

output.o: output.asm
	nasm -f elf64 output.asm

output.asm: main.fwf fhidwfe.jar
	java -jar fhidwfe.jar main.fwf output LINx64 0

fhidwfe.jar: src/*/*.java
	javac -d bin -cp src src/interfaceCore/Main.java
	cd bin; jar -cfe ../fhidwfe.jar interfaceCore.Main *; cd ..

clean:
	rm *.o output.asm program output.vm

clean-compiler:
	rm -rf bin
	rm fhidwfe.jar
