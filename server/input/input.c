#include "input_headers.h"

int init_input(Input_config * in_config)
{
    int fd = in_config->file = open("/dev/uinput", O_WRONLY | O_NONBLOCK);

    if(fd < 0) return fd;

    config_group_key_1(fd);
    config_group_btn_2(fd);
    config_group_rel_3(fd);
    config_virtual_driver(fd);

    return fd;
}

int close_input(Input_config * in_config)
{
    return close(in_config->file);
}

void emit(int fd, int type, int code, int val)
{
   struct input_event ie;

   ie.type = type;
   ie.code = code;
   ie.value = val;
   ie.time.tv_sec = 0;
   ie.time.tv_usec = 0;

   write(fd, &ie, sizeof(ie));
}

void config_group_key_1(int fd)
{
    ioctl(fd, UI_SET_EVBIT, EV_KEY);

    int key = INPUT1_KEY_UPPER_BOUND;

    while(key-- >= INPUT1_KEY_LOWER_BOUND)
    {
        ioctl(fd, UI_SET_KEYBIT, key);
    }
}

void config_group_btn_2(int fd)
{
    ioctl(fd, UI_SET_EVBIT, EV_KEY);

    int btn = INPUT2_KEY_UPPER_BOUND;

    while(btn-- >= INPUT2_KEY_LOWER_BOUND)
    {
        ioctl(fd, UI_SET_KEYBIT, btn);
    }
}

void config_group_rel_3(int fd)
{
    ioctl(fd, UI_SET_EVBIT, EV_REL);

    int rel = INPUT3_REL_UPPER_BOUND;

    while(rel-- >= INPUT3_REL_LOWER_BOUND)
    {
        ioctl(fd, UI_SET_RELBIT, rel);
    }
}

void config_virtual_driver(int fd)
{
    /* use UI_USER_DEV */

    struct uinput_user_dev uud;

    memset(&uud, 0, sizeof(uud));
    snprintf(uud.name, UINPUT_MAX_NAME_SIZE, "uinput old interface");
    write(fd, &uud, sizeof(uud));

    ioctl(fd, UI_DEV_CREATE);
}

int test_input()
{
    int max_retries = MAX_RETRIES;

    while(max_retries > 0)
    {
        if( access( "/var/run/sdp", F_OK ) != -1 ) 
            return 0;
    
        else 
        {
            max_retries--;
            sleep(1);
        }
    }

    return 1;
}