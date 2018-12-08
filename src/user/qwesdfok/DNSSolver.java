package user.qwesdfok;

import Libs.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNSSolver
{
	public final Vector<String> ip_blacklist = new Vector<>();
	public final Vector<String> host_blacklist = new Vector<>();
	public final Vector<String> dns_list = new Vector<>();
	public final AtomicBoolean needSolve = new AtomicBoolean(true);
	private final String save_path;
	private static final String IP_FILTER = "ip_filter";
	private static final String HOST_FILTER = "host_filter";
	private static final String DNS_LIST = "dns_list";
	private static final String END = "end";
	public static final String DNS_DEFAULT = "dns_default";

	public DNSSolver(String save_path)
	{
		if (save_path == null) save_path = "./config.txt";
		this.save_path = save_path;
		if (new File(save_path).exists())
		{
			read();
		} else
		{
			ip_blacklist.add("12\\d\\.\\d+\\.\\d+\\.\\d+");
			host_blacklist.add(".*google\\.com");
			dns_list.add("8.8.8.8");
			dns_list.add("8.8.4.4");
		}
	}

	public InetAddress solve(String host, Vector<String> dns)
	{
		if (!needSolve.get()) return null;
		if (dns == null || dns.size()==0)
		{
			System.clearProperty("sun.net.spi.nameservice.provider.1");
			System.clearProperty("sun.net.spi.nameservice.nameservers");
		} else
		{
			StringBuilder sb = new StringBuilder();
			for (String s:dns) sb.append(s).append(",");
			sb.deleteCharAt(sb.length() - 1);
			System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
			System.setProperty("sun.net.spi.nameservice.nameservers", sb.toString());
		}
		try
		{
			InetAddress[] addrs = InetAddress.getAllByName(host);
			for (InetAddress addr : addrs)
			{
				boolean blocked = false;
				for (String blackDomain : host_blacklist)
					if (blockTest(blackDomain, host))
						blocked = true;
				for (String blackAddr : ip_blacklist)
					if (blockTest(blackAddr, addr.getHostAddress()))
						blocked = true;
				if (!blocked)
				{
					Log.default_log.info(addr.toString());
					return addr;
				}
			}
			return null;
		} catch (UnknownHostException e)
		{
			return null;
		}
	}

	public void save()
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(save_path))))
		{
			writeInfoToFile(writer, IP_FILTER, ip_blacklist);
			writeInfoToFile(writer, HOST_FILTER, host_blacklist);
			writeInfoToFile(writer, DNS_LIST, dns_list);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void read()
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(save_path))))
		{
			readInfoFromFile(reader, IP_FILTER, ip_blacklist);
			readInfoFromFile(reader, HOST_FILTER, host_blacklist);
			readInfoFromFile(reader, DNS_LIST, dns_list);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void writeInfoToFile(BufferedWriter writer, String tag, Vector<String> buffer) throws IOException
	{
		writer.write(tag + "\n");
		for (String ip : buffer) writer.write(ip + "\n");
		writer.write(END + "\n");
	}

	private void readInfoFromFile(BufferedReader reader, String tag, Vector<String> buffer) throws IOException
	{
		buffer.clear();
		String info = reader.readLine();
		if (info.equals(tag))
		{
			while (true)
			{
				info = reader.readLine();
				if (info == null || info.equals(END)) break;
				buffer.add(info);
			}
		}
	}

	private boolean blockTest(String reg, String target)
	{
		Matcher matcher = Pattern.compile(reg).matcher(target);
		if (matcher.find())
		{
			Log.default_log.info(target + " ignored by " + reg);
			return true;
		}
		return false;
	}
}
