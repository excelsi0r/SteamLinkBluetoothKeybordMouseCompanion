#include "../common/common.h"

volatile sig_atomic_t stop;

__sighandler_t init_signal();