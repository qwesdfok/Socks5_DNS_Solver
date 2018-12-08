package user.qwesdfok;

import Libs.Log;
import Libs.UITools;

import javax.swing.*;
import java.awt.*;

public class LogPanel
{
	private JTextArea logArea = new JTextArea();
	private JPanel panel = new JPanel(new GridBagLayout());
	private JScrollPane showPanel = new JScrollPane(panel);
	private JCheckBox printLog = new JCheckBox("Print Log", false);

	public LogPanel()
	{
		logArea.setEditable(false);
		Log.setDefault_log(new Log(this::printLog));
		UITools uiTools = new UITools();
		panel.add(printLog, uiTools.autoConfig(1, 1, 1.0, 0.0));
		panel.add(logArea, uiTools.nextLine().autoConfig(2, 1, 1.0, 1.0));
	}

	public void printLog(String log)
	{
		if (!printLog.isSelected())
			return;
		if (!log.endsWith("\n")) log += "\n";
		logArea.append(log);
	}

	public JComponent getShowPanel()
	{
		return showPanel;
	}
}
