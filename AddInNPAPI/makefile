CC=g++
CFLAGS=-c -Wall -fPIC -m32 -I../../include
LDFLAGS=-shared -m32 '-Wl,--whole-archive' -L../../lib -l1CEAdnWebNPAPILin32 '-Wl,--no-whole-archive'
SOURCES=AddInNPAPI.cpp
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=../bin/AddInNPAPI.so

all: $(SOURCES) $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS) 
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@
