#ifndef __CONFIG_H__
#define __CONFIG_H__

#include <mozilla/ModuleUtils.h>
#include <nsIClassInfoImpl.h>

enum AddInComponentType
{
    eAddInCom = 1,
    eAddInNative,
    eAddInvalid = -1
};

extern const char ADDINSERVICEEX_CONTRACTID[];
extern const nsIID ADDINSERVICEEX_CID;
extern const char ADDINSERVICEEX_CLASSNAME[];

extern const char ADDINSITE_CONTRACTID[];
extern const nsIID ADDINSITE_CID;
extern const char ADDINSITE_CLASSNAME[];

extern const char ADAPTERNATIVE_CONTRACTID[];
extern const nsIID ADAPTERNATIVE_CID;
extern const char ADAPTERNATIVE_CLASSNAME[];

extern const char ADAPTERCOM_CONTRACTID[];
extern const nsIID ADAPTERCOM_CID;
extern const char ADAPTERCOM_CLASSNAME[];

extern const char module[];
extern const char nameFilePrj[];
extern const char nameFileComponent[];
extern const AddInComponentType typeComponent;

extern mozilla::Module kAddInModule;
#ifndef __linux__
#include <windows.h>
extern __declspec(dllexport) BOOL WINAPI DllMain( HMODULE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved );
#else
extern void init() __attribute__((constructor(101)));
#endif //__linux__
#endif //__CONFIG_H__

