#include "config.h"

AddInComponentType typeComponent = eAddInNative;
#ifdef WIN32
const char *nameFilePrj = "AddInChr.exe";
const char *nameFileComponent = "AddInNative.dll";
#elif __LP64__
const char *nameFilePrj = "AddInChrLin64";
const char *nameFileComponent = "libAddInNativeLin64.so";
#elif __linux__
const char *nameFilePrj = "AddInChrLin32";
const char *nameFileComponent = "libAddInNativeLin32.so";
#endif
