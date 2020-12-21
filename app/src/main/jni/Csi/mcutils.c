//
// Created by shwei on 2020/12/13.
//
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "mcutils.h"

int readFile(uint8_t *buf, int max_size, const char* filepath, uint8_t file_type)
{
    int32_t fd = open(filepath, O_RDONLY);
    if(fd < 0){
        fprintf(stderr, "<main loop> open file \"%s\" failed!", filepath);
        return -1;
    }
    int read_size = read(fd, buf, max_size);
    if(read_size<0){
        fprintf(stderr, "<main> read file \"%s\" failed!", filepath);
        return -2;
    }
    close(fd);
    return read_size;
}

int writeFile(uint8_t *buf, int size, const char* filepath, uint8_t file_type)
{
    int32_t fd = open(filepath, O_WRONLY|O_CREAT);
    if(fd < 0) {
        fprintf(stderr, "<main loop> write file \"%s\" failed!", filepath);
        return -1;
    }
    int write_size = write(fd, buf, size);
    if(write_size<0) {
        fprintf(stderr, "<main> write file \"%s\" failed!", filepath);
        return -2;
    }
    close(fd);
    return write_size;
}

int hexDump (const char * desc, const void * addr, const int len) {
    int i;
    unsigned char buff[17];
    const unsigned char * pc = (const unsigned char *)addr;
    // Output description if given.
    if (desc != NULL)
        printf ("%s (%d bytes):\n", desc, len);
    // Length checks.
    if (len == 0) {
        printf("  ZERO LENGTH\n");
        return -1;
    }
    else if (len < 0) {
        printf("  NEGATIVE LENGTH: %d\n", len);
        return -2;
    }
    for (i = 0; i < len; i++) {
        if ((i % 16) == 0) {
            if (i != 0)
                printf ("  %s\n", buff);
            printf ("  %04x ", i);
        }
        printf (" %02x", pc[i]);
        if ((pc[i] < 0x20) || (pc[i] > 0x7e))
            buff[i % 16] = '.';
        else
            buff[i % 16] = pc[i];
        buff[(i % 16) + 1] = '\0';
    }
    while ((i % 16) != 0) {
        printf ("   ");
        i++;
    }
    printf ("  %s\n", buff);
    return 0;
}