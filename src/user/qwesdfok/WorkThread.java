package user.qwesdfok;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

public class WorkThread extends Thread
{
	private static final int BUFFER_SIZE = 256 * 1024;

	public static class ConnectionConf
	{
		int targetPort;
		String targetAddr;
		final DNSSolver dnsSolver;
		final Vector<WorkThread> workThreadList;
		final Vector<String> dns = new Vector<>();

		public ConnectionConf(int targetPort, String targetAddr, DNSSolver dnsSolver, Vector<WorkThread> workThreadList)
		{
			this.targetPort = targetPort;
			this.targetAddr = targetAddr;
			this.dnsSolver = dnsSolver;
			this.workThreadList = workThreadList;
		}
	}

	private Socket inSocket, targetSocket;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private PipeThread pipeThread;
	private int packet_count;
	private final ConnectionConf conf;

	private class PipeThread extends Thread
	{
		InputStream inputStream;
		OutputStream outputStream;
		byte[] buffer = new byte[BUFFER_SIZE];

		public PipeThread(InputStream inputStream, OutputStream outputStream)
		{
			this.inputStream = inputStream;
			this.outputStream = outputStream;
		}

		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					Thread.sleep(1);
					int length = inputStream.read(buffer);
					if (length == -1)
						break;
					outputStream.write(buffer, 0, length);
					outputStream.flush();
				}
			} catch (IOException | InterruptedException e)
			{
				closeAll();
			}
		}
	}

	public WorkThread(int index, Socket inSocket, ConnectionConf conf)
	{
		super("work_thread_" + index);
		this.inSocket = inSocket;
		this.conf = conf;
	}

	@Override
	public void run()
	{
		try
		{
			targetSocket = new Socket(conf.targetAddr, conf.targetPort);
			InputStream inputStream = inSocket.getInputStream();
			OutputStream outputStream = inSocket.getOutputStream();
			InputStream targetInputStream = targetSocket.getInputStream();
			OutputStream targetOutputStream = targetSocket.getOutputStream();
			pipeThread = new PipeThread(targetInputStream, outputStream);
			pipeThread.start();
			while (true)
			{
				int length = inputStream.read(buffer);
				if (length == -1)
					break;
				if (packet_count == 1 && buffer[3] == 0x03)
				{
					byte domain_length = buffer[4];
					byte[] hostPort = new byte[]{buffer[length - 2], buffer[length - 1]};
					String domain_name = new String(buffer, 5, domain_length);
					InetAddress addr = conf.dnsSolver.solve(domain_name, conf.dns);
					if (addr != null)
					{
						byte[] addr_byte = addr.getAddress();
						if (addr_byte.length == 4)
							buffer[3] = 0x01;
						else
							buffer[3] = 0x04;
						System.arraycopy(addr_byte, 0, buffer, 4, addr_byte.length);
						System.arraycopy(hostPort, 0, buffer, 4 + addr_byte.length, 2);
						length = 4 + addr_byte.length + 2;
					}
				}
				targetOutputStream.write(buffer, 0, length);
				targetOutputStream.flush();
				packet_count++;
			}

		} catch (IOException e)
		{
			closeAll();
		}
	}

	public void closeAll()
	{
		WorkThread.this.interrupt();
		pipeThread.interrupt();
		try
		{
			if (!inSocket.isClosed())
				inSocket.close();
		} catch (IOException ex)
		{
			//ignore
		}
		try
		{
			if (!targetSocket.isClosed())
				targetSocket.close();
		} catch (IOException ex)
		{
			//ignore
		}
	}
}
