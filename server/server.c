#include <stdlib.h>
#include "bluetooth/bluetooth_headers.h"
#include "input/input_headers.h"

int main(int argc, char const *argv[])
{
    //errors
    int bt_error;

    //config bluetooth
    
    Bluetooth_config * bt_config = malloc(sizeof(Bluetooth_config));
    bt_error = init_bluetooth(bt_config);
    if(bt_error)
    {
        printf("Failed to configure Bluetooth module: %d\n", bt_error);
        return -1;
    }
    

    //config input
    init_input();   

    return 0;
}
