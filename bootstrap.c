#include <windows.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <fileapi.h>
#pragma warning(disable : 4996)
extern uint64_t Fwf_internal_main(void);

uint64_t Fwf_internal_malloc(uint64_t s)
{
	uint64_t * ret = (uint64_t*) malloc(sizeof(uint64_t) + s);
	*ret = s;
	return &ret[1];
}

uint64_t Fwf_internal_free(uint64_t ptr)
{
	free(((uint64_t*)ptr) - 1);
	return 0;
}

uint64_t Fwf_internal_sizeof(uint64_t ptr)
{
	return ((uint64_t*)ptr)[-1];
}

uint64_t Fwf_internal_realloc(uint64_t ptr, uint64_t newsize)
{
	newsize += sizeof(uint64_t);
	uint64_t* result = (uint64_t*) realloc(((uint64_t*)ptr)-1, newsize);
	result[0] = newsize;
	return (uint64_t)&result[1];
}



uint64_t Fwf_internal_putchar(uint64_t x) {
	putc((char)x,stdout);
	return 0;
}

FILE * files[63];
uint64_t location[63];
FILE* tof(uint64_t in) {
	if (in > 62) {
		return NULL;
	} else {
		return files[in];
	}
}

uint64_t Fwf_internal_fclose(uint64_t f) {
	fclose(tof(f));
	location[f] = 0;
	return 0;
}
uint64_t Fwf_internal_fread(uint64_t f) {
	location[f] ++;
	uint64_t result = fgetc(tof(f));
	return result;
}
uint64_t Fwf_internal_fwrite(uint64_t f, uint64_t cha) {
	fputc(cha, tof(f));
	return 0;
}
uint64_t Fwf_internal_fflush(uint64_t f) {
	fflush(tof(f));
	return 0;
}
uint64_t Fwf_internal_favail(uint64_t f) {
	return GetFileSize(tof(f),NULL)-location[f];
}
uint64_t Fwf_internal_getc() {
	return getchar();
}
uint64_t Fwf_internal_fopen(const char* filename) {
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
	
	uint64_t result = Fwf_internal_main();
	fflush(stdout);
	return result;
}
