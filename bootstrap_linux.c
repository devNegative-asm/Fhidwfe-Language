#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>


extern uint64_t __main(void);

uint64_t __malloc(uint64_t s)
{
	uint64_t * ret = (uint64_t*) malloc(sizeof(uint64_t) + s);
	*ret = s;
	return (uint64_t) &ret[1];
}

uint64_t __free(uint64_t ptr)
{
	free(((uint64_t*)ptr) - 1);
	return 0;
}

uint64_t __sizeof(uint64_t ptr)
{
	return ((uint64_t*)ptr)[-1];
}

uint64_t __realloc(uint64_t ptr, uint64_t newsize)
{
	newsize += sizeof(uint64_t);
	uint64_t* result = (uint64_t*) realloc(((uint64_t*)ptr)-1, newsize);
	result[0] = newsize;
	return (uint64_t)&result[1];
}



uint64_t __putchar(uint64_t x) {
	putc((char)x,stdout);
	return 0;
}

FILE * files[63];
uint64_t location[63];
FILE* __tof(uint64_t in) {
	if (in > 62) {
		return NULL;
	} else {
		return files[in];
	}
}

uint64_t __fclose(uint64_t f) {
	fclose(__tof(f));
	location[f] = 0;
	return 0;
}
uint64_t __fread(uint64_t f) {
	location[f] ++;
	uint64_t result = fgetc(__tof(f));
	return result;
}
uint64_t __fwrite(uint64_t f, uint64_t cha) {
	fputc(cha, __tof(f));
	return 0;
}
uint64_t __fflush(uint64_t f) {
	fflush(__tof(f));
	return 0;
}
uint64_t __getc() {
	return getchar();
}
uint64_t __fopen(const char* filename) {
	for (int q = 0; q < 63; q++) {
		if (files[q] == NULL)
		{
			files[q] = fopen(filename, "r+");
			location[q] = 0;
			return q;
		}
	}
	return 255;
}

int main()
{
	for (int q = 0; q < 63; q++) {
		files[q] = NULL;
	}
	
	uint64_t result = __main();
	fflush(stdout);
	return result;
}
