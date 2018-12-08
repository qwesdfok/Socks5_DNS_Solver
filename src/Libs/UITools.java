package Libs;

import java.awt.*;

/**
 * Created by qwesdfok on 2016/12/21.
 */
public class UITools
{
	private GridBagConstraints constraints;
	public int auto_x = 0, auto_y = 0;

	public UITools()
	{
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(4, 8, 4, 8);
	}

	public UITools(GridBagConstraints constraints)
	{
		this.constraints = constraints;
	}

	public void resetAuto()
	{
		auto_x = 0;
		auto_y = 0;
	}

	public GridBagConstraints config(int x, int y)
	{
		return config(x, y, 1, 1, 1.0, 1.0);
	}

	public GridBagConstraints config(int x, int y, int width, int height)
	{
		return config(x, y, width, height, 1.0, 1.0);
	}

	public GridBagConstraints config(int x, int y, int width, int height, double dx, double dy)
	{
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.weightx = dx;
		constraints.weighty = dy;
		return constraints;
	}

	public GridBagConstraints autoConfig(int width, int height, double dx, double dy)
	{
		config(auto_x, auto_y, width, height, dx, dy);
		auto_x += width;
		return constraints;
	}

	public GridBagConstraints autoConfig(double dx, double dy)
	{
		return autoConfig(1, 1, dx, dy);
	}

	public GridBagConstraints autoConfig()
	{
		return autoConfig(1, 1, 1.0,1.0);
	}

	public UITools nextLine()
	{
		auto_x = 0;
		auto_y += 1;
		return this;
	}

	public GridBagConstraints getConstraints()
	{
		return constraints;
	}

	public UITools setConstraints(GridBagConstraints constraints)
	{
		this.constraints = constraints;
		return this;
	}
}
