#include "bluetooth/bluetooth_headers.h"
#include "input/input_headers.h"
#include "signal/signal_headers.h"

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
    
    //TODO config input
    init_input();

    //config signal handling
    signal(SIGINT, inthand);
    while(!stop)
    {

    }

    //CYCLES HERE


    //close bluetooth
    close_bluetooth(bt_config);


    printf("Exiting...\n");
    return 0;
}
