package user.qwesdfok;

import Libs.UITools;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class ListPanel
{
	private class UnitPanel
	{
		JPanel panel = new JPanel(new GridBagLayout());
		JTextField dataText;
		String filter;

		public UnitPanel(String filter)
		{
			this.filter = filter;
			dataText = new JTextField(filter);
			JButton delete = new JButton("D");
			delete.addActionListener(e -> delete());
			JButton modify = new JButton("M");
			modify.addActionListener(e -> modify());
			uiTools.resetAuto();
			panel.add(dataText, uiTools.autoConfig(1.0, 0.0));
			panel.add(modify, uiTools.autoConfig(0.0, 0.0));
			panel.add(delete, uiTools.autoConfig(0.0, 0.0));
		}

		void delete()
		{
			dataPanel.remove(panel);
			dataInfo.remove(filter);
			dataList.remove(filter);
			showPanel.repaint();
			dnsSolver.save();
		}

		void modify()
		{
			dataInfo.remove(filter);
			dataList.remove(filter);
			filter = dataText.getText();
			UnitPanel old = dataInfo.putIfAbsent(filter, this);
			if (old == null) dataList.add(filter);
			showPanel.repaint();
			dnsSolver.save();
		}
	}

	private UITools uiTools = new UITools();
	private JPanel dataPanel = new JPanel(new GridBagLayout()), addPanel;
	private JScrollPane showPanel = new JScrollPane(dataPanel);
	private ConcurrentHashMap<String, UnitPanel> dataInfo = new ConcurrentHashMap<>();
	private JTextField addText = new JTextField("0.0.0.0");
	private Vector<String> dataList;
	private DNSSolver dnsSolver;

	public ListPanel(Vector<String> dataList,DNSSolver dnsSolver)
	{
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
			UnitPanel unitPanel = new UnitPanel(data);
			dataInfo.putIfAbsent(data, unitPanel);
		}
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
		UnitPanel unitPanel = new UnitPanel(filter);
		UnitPanel old = dataInfo.putIfAbsent(filter, unitPanel);
		if (old != null) return;
		dataList.add(filter);
		render();
		dnsSolver.save();
	}

	public JComponent getShowPanel()
	{
		return showPanel;
	}
}
