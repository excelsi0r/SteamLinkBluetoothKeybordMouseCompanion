#include <linux/uinput.h>
#include <linux/types.h>
#include <linux/stat.h>
#include <linux/fcntl.h>
#include <string.h>
#include <unistd.h>
#include <stdio.h>
#include <signal.h>

/**
 * For Linux Kernel version: 3.8.13-mrvl
 */
volatile sig_atomic_t stop;

/* emit function is identical to of the first example */
void emit(int fd, int type, int code, int val)
{
   struct input_event ie;

   ie.type = type;
   ie.code = code;
   ie.value = val;
   /* timestamp values below are ignored */
   ie.time.tv_sec = 0;
   ie.time.tv_usec = 0;

   write(fd, &ie, sizeof(ie));
}


void inthand(int signum) {
    stop = 1;
}

int main(void)
{
	printf("Welcome!\n");
   fflush(stdout);

   struct uinput_user_dev uud;
   int version, rc, fd;
	int i = 50;

	/*Opening uinput test*/
   fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
   printf("Uinput opening code: %i\n", fd);
   fflush(stdout);

	/*Version Testing*/
   rc = ioctl(fd, UI_GET_VERSION, &version);
   printf("Which version code: %i-%i\n", rc, version);
	fflush(stdout);

   if (rc == 0 && version >= 5) {
      /* use UI_DEV_SETUP */
      return 0;
   }

   /*
    * The ioctls below will enable the device that is about to be
    * created, to pass key events, in this case the space key.
    */
   ioctl(fd, UI_SET_EVBIT, EV_KEY);
   ioctl(fd, UI_SET_KEYBIT, KEY_SPACE);

   ioctl(fd, UI_SET_EVBIT, EV_KEY);
   ioctl(fd, UI_SET_KEYBIT, BTN_LEFT);

   ioctl(fd, UI_SET_EVBIT, EV_REL);
   ioctl(fd, UI_SET_RELBIT, REL_X);
   ioctl(fd, UI_SET_RELBIT, REL_Y);

   memset(&uud, 0, sizeof(uud));
   snprintf(uud.name, UINPUT_MAX_NAME_SIZE, "uinput old interface");
   write(fd, &uud, sizeof(uud));

   ioctl(fd, UI_DEV_CREATE);

   /*
    * On UI_DEV_CREATE the kernel will create the device node for this
    * device. We are inserting a pause here so that userspace has time
    * to detect, initialize the new device, and can start listening to
    * the event, otherwise it will not notice the event we are about
    * to send. This pause is only needed in our example code!
    */
   sleep(1);

   /* Key press, report the event, send key release, and report again */
   emit(fd, EV_KEY, KEY_SPACE, 1);
   emit(fd, EV_SYN, SYN_REPORT, 0);
   emit(fd, EV_KEY, KEY_SPACE, 0);
   emit(fd, EV_SYN, SYN_REPORT, 0);

   printf("Space emited!\n");
   fflush(stdout);

   /*
    * Give userspace some time to read the events before we destroy the
    * device with UI_DEV_DESTOY.
    */
   sleep(1);

   while (i--) {
      emit(fd, EV_REL, REL_X, 5);
      emit(fd, EV_REL, REL_Y, 5);
      emit(fd, EV_SYN, SYN_REPORT, 0);
      usleep(15000);
   }

   printf("Mouse emited!\n");
   fflush(stdout);

   /*
    * Give userspace some time to read the events before we destroy the
    * device with UI_DEV_DESTOY.
    */

   sleep(1);

   ioctl(fd, UI_DEV_DESTROY);

   close(fd);
   return 0;
}
