#ifndef SIGNAL_HEADERS_H
#define SIGNAL_HEADERS_H

#include "../common/common.h"

volatile sig_atomic_t stop;

__sighandler_t init_signal();

#endif