CC=gcc
CFLAGS= -Wall -lbluetooth
DEPS=

ROOT=./server/
BLDIR=$(ROOT)bluetooth/
INDIR=$(ROOT)input/
PTDIR=$(ROOT)protocol/

ROOTOBJS=$(ROOT)server.c
BLOBJS=$(BLDIR)bluetooth.c $(BLDIR)bluetooth_service.c
INOBJS=$(INDIR)input.c $(INDIR)input_service.c
PTOBJS=$(PTDIR)protocol.c

OBJS=$(ROOTOBJS) $(BLOBJS) $(INOBJS) $(PTOBJS)

BIN=server.o

all: service

service:
	$(CC) $(OBJS) -o $(BIN) $(CFLAGS) $(DEPS)
