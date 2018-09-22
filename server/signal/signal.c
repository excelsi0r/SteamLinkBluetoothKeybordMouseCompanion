#include "signal_headers.h"

void inthand(int signum) {
    stop = 1;
}

__sighandler_t init_signal()
{
    return signal(SIGINT, inthand);
}