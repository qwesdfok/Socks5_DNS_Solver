package user.qwesdfok;

import Libs.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

public class ServerThread extends Thread
{
	private ServerSocket serverSocket;
	private int listenPort;
	private int count = 0;
	private final WorkThread.ConnectionConf conf;
	private final Vector<WorkThread> workThreadList = new Vector<>();

//	private ArrayList<WorkThread> workThreadList = new ArrayList<>();

	public ServerThread(int listenPort, String targetAddr, int targetPort, DNSSolver dnsSolver)
	{
		super("ServerThread");
		this.listenPort = listenPort;
		conf = new WorkThread.ConnectionConf(targetPort, targetAddr, dnsSolver, workThreadList);
	}

	@Override
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(listenPort);
			serverSocket.setSoTimeout(100);
			Socket inSocket = null;
			while (true)
			{
				Thread.sleep(10);
				try
				{
					inSocket = serverSocket.accept();
				} catch (SocketTimeoutException e)
				{
					if (this.isInterrupted())
					{
						workThreadList.forEach(WorkThread::interrupt);
						break;
					}
					inSocket = null;
				}
				if (inSocket != null)
				{
					WorkThread workThread = new WorkThread(count++, inSocket, conf);
					workThread.start();
					workThreadList.add(workThread);
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			//ignore
		}
	}

	public final WorkThread.ConnectionConf getConf()
	{
		return conf;
	}
}
