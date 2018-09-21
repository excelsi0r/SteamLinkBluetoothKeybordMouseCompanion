#include "../common/common.h"

volatile sig_atomic_t stop;

void inthand(int signum);