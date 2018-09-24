#include "protocol_headers.h"

void parse(char buf[], Event * event)
{
	event->valid = 0;
	event->mouse_ev = 0;
	event->mousewheel_ev = 0;
	event->key_ev = 0;

	char * pEnd;
	long int dev = 0;

	dev = strtol(buf, &pEnd, 10);
	
	if(dev == KEYB && pEnd != buf)
	{
		char * npEnd = pEnd;
		
		float key = strtof(pEnd, &pEnd);
		if(npEnd == pEnd) return;

		npEnd = pEnd;
		
		float key_action = strtof(pEnd, &pEnd);
		if(npEnd == pEnd) return;

		event->valid = 1;
		event->key_ev = 1;
		event->key = (int) key;
		event->key_action = (int) key_action;
	}
	else if(dev == MOUS && pEnd != buf)
	{
		char * npEnd = pEnd;		

		float mouse_x = strtof(pEnd, &pEnd);
		if(npEnd == pEnd) return;
	
		npEnd = pEnd;

		float mouse_y = strtof(pEnd, &pEnd);
		if(npEnd == pEnd) return;

		event->valid = 1;
		event->mouse_ev = 1;
		event->mouse_x = mouse_x;
		event->mouse_y = mouse_y;	
	}
	else if(dev == MHWL && pEnd != buf)
	{
		char * npEnd = pEnd;			

		float mouse_h = strtof(pEnd, &pEnd);
		if(npEnd == pEnd) return;

		npEnd = pEnd;		

		float mouse_v = strtof(pEnd, &pEnd);
		if(npEnd == pEnd) return;

		event->valid = 1;
		event->mousewheel_ev = 1;
		event->mouse_h = mouse_h;
		event->mouse_v = mouse_v;		
	}
	else return;
}

void printEvent(Event * event)
{
	printf("Event valid: %d\n", event->valid);

	if(event->mouse_ev)
	{
		printf("Mouse Event: %d\n", event->mouse_ev);
		printf("Mouse X: %f\n", event->mouse_x);
		printf("Mouse Y: %f\n", event->mouse_y);
	}
	else if(event->mousewheel_ev)
	{
		printf("MouseWheel Event: %d\n", event->mousewheel_ev);
		printf("Mouse H: %f\n", event->mouse_h);
		printf("Mouse V: %f\n", event->mouse_v);
	}
	else if(event->key_ev)
	{
		printf("Keyboard Event: %d\n", event->key_ev);
		printf("Key: %d\n", event->key);
		printf("Key Action: %d\n", event->key_action);
	}
}