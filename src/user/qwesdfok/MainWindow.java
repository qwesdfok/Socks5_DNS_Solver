package user.qwesdfok;

import Libs.UITools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class MainWindow
{
	private JFrame mainWindow = new JFrame("DNS Solver");
	private SystemTray systemTray = null;
	private TrayIcon trayIcon = null;
	private JCheckBox local_dns = new JCheckBox("Local_DNS", false);
	private volatile DNSSolver dnsSolver = new DNSSolver(null);
	private ServerThread serverThread;
	private JTabbedPane tabbedPane = new JTabbedPane();

	public MainWindow()
	{
		mainWindow.setSize(400, 300);
		mainWindow.setLocationRelativeTo(null);
		local_dns.addItemListener(item -> dnsSolver.needSolve.set(local_dns.isSelected()));
		serverThread = new ServerThread(1380, "localhost", 1180, dnsSolver);
		systemTray = SystemTray.getSystemTray();
		URL jpgURL = this.getClass().getClassLoader().getResource("icon.jpg");
		if (jpgURL == null) throw new RuntimeException("JPG file is not found");
		ImageIcon imageIcon = new ImageIcon(jpgURL);
		PopupMenu popupMenu = new PopupMenu();
		MenuItem showMainWindow = new MenuItem("Show");
		MenuItem exit = new MenuItem("Exit");
		showMainWindow.addActionListener(e -> showWindow());
		exit.addActionListener(e -> close());
		popupMenu.add(showMainWindow);
		popupMenu.add(exit);
		trayIcon = new TrayIcon(imageIcon.getImage(), "DNS_Solver", popupMenu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2) showWindow();
			}
		});
		mainWindow.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				hideWindow();
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				if (serverThread.isAlive())
					serverThread.interrupt();
			}
		});
		mainWindow.setLayout(new GridBagLayout());
		UITools uiTools = new UITools();
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(e -> close());
		ListPanel ipListPanel = new ListPanel(dnsSolver.ip_blacklist, dnsSolver);
		ListPanel hostListPanel = new ListPanel(dnsSolver.host_blacklist, dnsSolver);
		DNSPanel dnsPanel = new DNSPanel(dnsSolver.dns_list, dnsSolver, serverThread.getConf());
		LogPanel logPanel = new LogPanel();
		tabbedPane.addTab("IP Filter", ipListPanel.getShowPanel());
		tabbedPane.addTab("Host Filter", hostListPanel.getShowPanel());
		tabbedPane.addTab("DNS", dnsPanel.getShowPanel());
		tabbedPane.addTab("Log", logPanel.getShowPanel());
		mainWindow.add(tabbedPane, uiTools.autoConfig(2, 1, 1.0, 10.0));
		mainWindow.add(local_dns, uiTools.nextLine().autoConfig());
		mainWindow.add(exitButton, uiTools.autoConfig());
	}

	public void start()
	{
		mainWindow.setVisible(true);
		serverThread.start();
	}

	private void hideWindow()
	{
		mainWindow.setVisible(false);
		try
		{
			systemTray.add(trayIcon);
		} catch (AWTException e)
		{
			e.printStackTrace();
			mainWindow.dispose();
		}
	}

	private void showWindow()
	{
		systemTray.remove(trayIcon);
		mainWindow.setVisible(true);
	}

	private void close()
	{
		if (serverThread.isAlive())
			serverThread.interrupt();
		systemTray.remove(trayIcon);
		mainWindow.dispose();
	}
}
