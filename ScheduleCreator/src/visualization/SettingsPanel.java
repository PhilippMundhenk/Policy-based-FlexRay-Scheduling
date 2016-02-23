package visualization;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class SettingsPanel extends JPanel implements ItemListener
{
	public static final int NEW_COLOR_BTN = 1;
	
	private static final long serialVersionUID = 1L;
	protected JCheckBox ecuModeBox;
	protected JButton newColorsBtn;
	protected Boolean ecuMode = true;
	protected JCheckBox autoSwitchPaneBox;
	protected Boolean autoSwitchPane = true;
	protected Visualizer mainFrame;

	public SettingsPanel(Visualizer caller)
	{
		this.setLayout(new BorderLayout());
		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		
		this.mainFrame = caller;
				
		ecuModeBox = new JCheckBox("ECU Mode");
		ecuModeBox.setMnemonic(KeyEvent.VK_C);
		if(ecuMode)
		{
			ecuModeBox.setSelected(true);
		}
		else
		{
			ecuModeBox.setSelected(false);
		}
		ecuModeBox.addItemListener(this);
		settingsPanel.add(ecuModeBox);
		
		autoSwitchPaneBox = new JCheckBox("automatically switch info tabs");
		autoSwitchPaneBox.setMnemonic(KeyEvent.VK_C);
		if(autoSwitchPane)
		{
			autoSwitchPaneBox.setSelected(true);
		}
		else
		{
			autoSwitchPaneBox.setSelected(false);
		}
		autoSwitchPaneBox.addItemListener(this);
		settingsPanel.add(autoSwitchPaneBox);
		
		newColorsBtn = new JButton("new colors");
		newColorsBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				EcuColors.newColors();
				mainFrame.setSelectedTab(0);
				mainFrame.colorsChanged();
			}
		});
		settingsPanel.add(newColorsBtn);
		this.add(settingsPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		
		if (source == ecuModeBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				ecuMode = false;
	        }
			else if (e.getStateChange() == ItemEvent.SELECTED)
			{
				ecuMode = true;
	        }
		}
		else if (source == autoSwitchPaneBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				autoSwitchPane = false;
	        }
			else if (e.getStateChange() == ItemEvent.SELECTED)
			{
				autoSwitchPane = true;
	        }
		}
	}
	
	public Boolean getEcuMode()
	{
		return ecuMode;
	}
	
	public Boolean getAutoSwitchPane()
	{
		return autoSwitchPane;
	}
	
	public void addActionListener(int buttonID, ActionListener actionListener)
	{
		if(buttonID == NEW_COLOR_BTN)
		{
			newColorsBtn.addActionListener(actionListener);
		}
	}
}
