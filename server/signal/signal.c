#include "signal_headers.h"

void inthand(int signum) {
    stop = 1;
}