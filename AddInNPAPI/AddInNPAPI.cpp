#include "NPAPILib.h"

AddInComponentType typeComponent = eAddInNative;
#ifdef _WINDOWS
const char *nameFilePrj = "AddInNPAPI.dll";
const char *nameFileComponent = "AddInNative.dll";
#elif __linux__
const char *nameFilePrj = "AddInNPAPI.so";
const char *nameFileComponent = "AddInNative.so";
#endif
const char *mimeType = "application/component-example-1";
