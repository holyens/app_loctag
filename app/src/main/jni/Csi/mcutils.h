//
// Created by shwei on 2020/12/13.
//

#ifndef LOCTAG_MCUTILS_H
#define LOCTAG_MCUTILS_H
#ifdef __cplusplus
extern "C" {
#endif

extern int hexDump (const char * desc, const void * addr, const int len);
extern int readFile(uint8_t *buf, int size, const char* filepath, uint8_t file_type);
extern int writeFile(uint8_t *buf, int size, const char* filepath, uint8_t file_type);
#ifdef __cplusplus
}
#endif
#endif //LOCTAG_MCUTILS_H
