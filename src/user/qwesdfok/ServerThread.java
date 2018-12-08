package user.qwesdfok;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerThread extends Thread
{
	private ServerSocket serverSocket;
	private int listenPort;
	private int count = 0;
	private final WorkThread.ConnectionConf conf;
	private final Vector<WorkThread> workThreadList = new Vector<>();
	private AtomicBoolean modified = new AtomicBoolean(false);
	private MainWindow mainWindow;

//	private ArrayList<WorkThread> workThreadList = new ArrayList<>();

	public ServerThread(int listenPort, String targetAddr, int targetPort, DNSSolver dnsSolver, MainWindow mainWindow)
	{
		super("ServerThread");
		this.mainWindow = mainWindow;
		this.listenPort = listenPort;
		conf = new WorkThread.ConnectionConf(targetPort, targetAddr, dnsSolver, workThreadList);
	}

	@Override
	public void run()
	{
		try
		{
			boolean retry = true;
			while (retry)
			{
				try
				{
					modified.set(false);
					serverSocket = new ServerSocket(listenPort);
					retry = false;
				} catch (IOException e)
				{
					JOptionPane.showConfirmDialog(mainWindow.getMainWindow(), "Listen port is already userd", "Error", JOptionPane.YES_NO_OPTION);
					while (!modified.get()) Thread.sleep(100);
				}
			}
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

	public void setListenPort(int listenPort)
	{
		this.listenPort = listenPort;
		modified.set(true);
	}
}
