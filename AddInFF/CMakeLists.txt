# Cmake script for project AddInFF of AddInFF
# autor: 1Ñ

project(AddInFF)
cmake_minimum_required(VERSION 2.8)

SET(AddInFF_SRC
    config.cpp
    plugin.cpp
    config.h
    )

if (${XUL_VERSION} STREQUAL "")
    message(FATAL_ERROR "Required version of xulrunner!")
endif (${XUL_VERSION} STREQUAL "")
    
if (NOT "${XUL_VERSION}" STREQUAL "")
    if (${XUL_VERSION} STREQUAL "31.0")
        set(XPCOM_DIR $ENV{XUL310_PATH} CACHE TYPE PATH)
    endif(${XUL_VERSION} STREQUAL "31.0")
    MESSAGE(STATUS "Found XUL_PATH for " ${XPCOM_DIR})
endif (NOT "${XUL_VERSION}" STREQUAL "")


IF("${XPCOM_DIR}" STREQUAL "")
    MESSAGE(FATAL_ERROR "XUL_PATH must have value!")
ENDIF("${XPCOM_DIR}" STREQUAL "")

if (NOT TARGET_PLATFORM_32)
    set(XPCOM_DIR ${XPCOM_DIR}-x64)
    MESSAGE(STATUS "Found x64 XUL_PATH for " ${XPCOM_DIR})
endif (NOT TARGET_PLATFORM_32)

if(WIN32)
    set(CMAKE_PREFIX_PATH ${CMAKE_PREFIX_PATH} ${XPCOM_DIR})
    set(CMAKE_LIBRARY_PATH ${CMAKE_LIBRARY_PATH} ${XPCOM_DIR}/lib ${CMAKE_SOURCE_DIR}/lib)
    include_directories(${XPCOM_DIR}/include ${XPCOM_DIR}/include/nspr)

    add_definitions(-DMOZILLA_STRICT_API -DXPCOM_GLUE_USE_NSPR -DXP_WIN -DMOZ_NO_MOZALLOC)
    
else(WIN32)

    include_directories(BEFORE ${XPCOM_DIR}/include ${XPCOM_DIR}/include/nspr)
    set(CMAKE_PREFIX_PATH ${CMAKE_PREFIX_PATH} ${XPCOM_DIR} ${XPCOM_DIR}/sdk ${XPCOM_DIR}/idl ${CMAKE_SOURCE_DIR}/../lib)
    
    add_definitions(-DMOZILLA_STRICT_API -DXPCOM_GLUE_USE_NSPR -DXP_UNIX -DMOZ_NO_MOZALLOC)
endif(WIN32)

SET (CMAKE_PREFIX_PATH ${CMAKE_PREFIX_PATH} ${CMAKE_SOURCE_DIR})

find_library(XPCOMGLUELIB xpcomglue_s_nomozalloc)
find_library(NSPR4 nspr4 )
find_library(PLDS4 plds4 )
find_library(PLC4 plc4 )
find_library(NSS nss3 )
find_library(XPCOMLIB xul)

find_library(1CEADNWEBFF "1CEAdnWebFF${MySuffix1}${MySuffix2}-${XUL_VERSION}" HINTS )

set(VAR_SUFFIX "-${XUL_VERSION}")

if (UNIX AND NOT APPLE)
    set_source_files_properties(${AddInFF_SRC}
                                PROPERTIES COMPILE_FLAGS
                                "-finput-charset=WINDOWS-1251 -fPIC -fshort-wchar -std=c++11 ")
else()
    add_definitions(-DWIN32 )
endif()

add_library(${PROJECT_NAME} SHARED ${AddInFF_SRC})

link_directories(${XPCOM_DIR}/lib ${XPCOM_DIR}/sdk/lib ${CMAKE_SOURCE_DIR}/../lib)
target_link_libraries(${PROJECT_NAME} ${NSPR4} ${PLDS4} ${PLC4} ${XPCOMLIB} ${NSS} ${1CEADNWEBFF} ${XPCOMGLUELIB})

if(WIN32)
    target_link_libraries(${PROJECT_NAME} ${XPCOM_JS})
endif(WIN32)

set_target_properties( ${PROJECT_NAME} PROPERTIES
    #change prefix
    PREFIX ""
   # allow creating static and shared libs without conflicts
   CLEAN_DIRECT_OUTPUT 1
   # avoid conflicts between library and binary target names
   OUTPUT_NAME ${PROJECT_NAME}${MySuffix1}${MySuffix2}${VAR_SUFFIX} 
   )

