package visualization;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class HelpPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	public HelpPanel()
	{
		this.add(new JLabel("Select multiple wrapperPDUs: \n Hold down ctrl-key for multiple single wrapperPDUs, hold shift for continuous selection."));
	}
}
