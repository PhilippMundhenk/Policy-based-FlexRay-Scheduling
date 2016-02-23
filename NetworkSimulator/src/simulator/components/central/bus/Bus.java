package simulator.components.central.bus;

import java.util.Collection;

import SchedulingInterfaces.Message;

public interface Bus
{
	void tick(double tickLenMicrosec);

	void sendMsg(char[] outBuf, @SuppressWarnings("rawtypes") Collection<Message> msgs);
}
