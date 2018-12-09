package user.qwesdfok;

import Libs.Log;
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
	private JCheckBox local_dns = new JCheckBox("Local_DNS", true);
	private volatile DNSSolver dnsSolver = new DNSSolver(null);
	private ServerThread serverThread;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private WorkThread.ConnectionConf conf;
	private JTextField listen_port, target_host, target_port;

	public MainWindow()
	{
		mainWindow.setSize(700, 300);
		mainWindow.setLocationRelativeTo(null);
		local_dns.addItemListener(item -> dnsSolver.needSolve.set(local_dns.isSelected()));
		serverThread = new ServerThread(1380, "localhost", 1180, dnsSolver, this);
		conf = serverThread.getConf();
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
		DNSPanel dnsPanel = new DNSPanel(dnsSolver.dns_list, dnsSolver, conf);
		LogPanel logPanel = new LogPanel();
		listen_port = new JTextField("1380");
		target_host = new JTextField("127.0.0.1");
		target_port = new JTextField("1180");
		JButton startButton = new JButton("Start");
		startButton.addActionListener(e -> start());
		tabbedPane.addTab("IP Filter", ipListPanel.getShowPanel());
		tabbedPane.addTab("Host Filter", hostListPanel.getShowPanel());
		tabbedPane.addTab("DNS", dnsPanel.getShowPanel());
		tabbedPane.addTab("Log", logPanel.getShowPanel());
		mainWindow.add(tabbedPane, uiTools.autoConfig(5, 1, 1.0, 10.0));
		mainWindow.add(local_dns, uiTools.nextLine().autoConfig());
		mainWindow.add(new JLabel("ListenPort:"), uiTools.autoConfig());
		mainWindow.add(listen_port, uiTools.autoConfig());
		mainWindow.add(startButton, uiTools.autoConfig());
		mainWindow.add(new JLabel("TargetHost:"), uiTools.nextLine().autoConfig());
		mainWindow.add(target_host, uiTools.autoConfig());
		mainWindow.add(new JLabel("TargetPort:"), uiTools.autoConfig());
		mainWindow.add(target_port, uiTools.autoConfig());
		mainWindow.add(exitButton, uiTools.autoConfig());
	}

	public void enableVisible()
	{
		mainWindow.setVisible(true);
	}

	public void start()
	{
		if (serverThread.getStarted().get())
		{
			JOptionPane.showMessageDialog(mainWindow, "请勿重复启动，如要切换端口，请先Exit。");
			Log.default_log.info("Please Exit before modify configuration.");
			return;
		}
		conf.targetPort = Integer.parseInt(target_port.getText());
		conf.targetAddr = target_host.getText();
		serverThread.setListenPort(Integer.parseInt(listen_port.getText()));
		try
		{
			serverThread.start();
		} catch (RuntimeException e)
		{
			Log.default_log.info("Listen port is already used.");
		}
		Log.default_log.info("Started.");
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

	public JFrame getMainWindow()
	{
		return mainWindow;
	}
}
