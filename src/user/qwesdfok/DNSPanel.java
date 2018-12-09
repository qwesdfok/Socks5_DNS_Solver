package user.qwesdfok;

import Libs.UITools;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class DNSPanel
{
	private class UnitPanel
	{
		JPanel panel = new JPanel(new GridBagLayout());
		JCheckBox dnsSelection;
		String filter;

		public UnitPanel(String filter,boolean selected)
		{
			this.filter = filter;
			dnsSelection = new JCheckBox(filter,selected);
			dnsSelection.addItemListener(e->toggle());
			JButton delete = new JButton("D");
			delete.addActionListener(e -> delete());
			uiTools.resetAuto();
			panel.add(dnsSelection, uiTools.autoConfig(1.0, 0.0));
			panel.add(delete, uiTools.autoConfig(0.0, 0.0));
		}

		void delete()
		{
			dataPanel.remove(panel);
			dataInfo.remove(filter);
			dataList.remove(filter);
			dnsSolver.dns_list.remove(filter);
			showPanel.repaint();
			dnsSolver.save();
		}

		void toggle()
		{
			if (dnsSelection.isSelected())
				conf.dns.add(filter);
			else
				conf.dns.remove(filter);
		}
	}

	private UITools uiTools = new UITools();
	private JPanel dataPanel = new JPanel(new GridBagLayout()), addPanel;
	private JScrollPane showPanel = new JScrollPane(dataPanel);
	private ConcurrentHashMap<String, UnitPanel> dataInfo = new ConcurrentHashMap<>();
	private JTextField addText = new JTextField("0.0.0.0");
	private Vector<String> dataList;
	private DNSSolver dnsSolver;
	private WorkThread.ConnectionConf conf;

	public DNSPanel(Vector<String> dataList, DNSSolver dnsSolver, WorkThread.ConnectionConf conf)
	{
		this.conf = conf;
		this.dnsSolver = dnsSolver;
		this.dataList = dataList;
		addPanel = new JPanel(new GridBagLayout());
		uiTools.resetAuto();
		addPanel.add(addText, uiTools.autoConfig());
		JButton addButton = new JButton("A");
		addButton.addActionListener(e -> add(addText.getText()));
		addPanel.add(addButton, uiTools.autoConfig(0.0, 0.0));
		for (String data : dataList)
		{
			UnitPanel unitPanel = new UnitPanel(data,true);
			dataInfo.putIfAbsent(data, unitPanel);
		}
		conf.dns.addAll(dnsSolver.dns_list);
		render();
	}

	private void render()
	{
		dataPanel.removeAll();
		uiTools.resetAuto();
		dataPanel.add(addPanel, uiTools.autoConfig(1.0, 0.0));
		for (Map.Entry<String, UnitPanel> entry : dataInfo.entrySet())
		{
			dataPanel.add(entry.getValue().panel, uiTools.nextLine().autoConfig(1.0, 0.0));
		}
		dataPanel.add(new JPanel(), uiTools.nextLine().autoConfig());
		showPanel.repaint();
	}

	private void add(String filter)
	{
		UnitPanel unitPanel = new UnitPanel(filter,false);
		UnitPanel old = dataInfo.putIfAbsent(filter, unitPanel);
		if (old != null) return;
		dataList.add(filter);
		dnsSolver.save();
		render();
	}

	public JComponent getShowPanel()
	{
		return showPanel;
	}
}
