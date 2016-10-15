package uk.ac.soton.ldanalytics.iotwo;

import java.io.IOException;

import org.zeromq.ZMQ.Socket;

public class ResultsReceiver implements Runnable  {
	private Socket receiver;
	
	public ResultsReceiver(Socket receiver) {
		this.receiver = receiver;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while (!Thread.currentThread ().isInterrupted ()) {
            String msg = receiver.recvStr();
            System.out.println(msg);
            try {
            	EventsWebSocket.sendMessage(msg);
			} catch (IOException e) {
				System.out.println("msg:"+msg);
				e.printStackTrace();
			}
        }
        receiver.close();
	}

}

