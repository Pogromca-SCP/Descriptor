package descriptor;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.event.ActionEvent;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * A pop-up alert message
 */
public final class Alert extends JFrame
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Contains and displays alert message
	 */
	private JLabel txt;
	
	/**
	 * Alert construction
	 */
	public Alert()
	{
		super("Komunikat");
		setSize(600, 150);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		URL imgUrl = getClass().getResource("/Alert.png");

		if (imgUrl != null)
		{
			ImageIcon img = new ImageIcon(imgUrl);
			setIconImage(img.getImage());
		}

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		txt = new JLabel();
		add(txt, c);
		c.gridy = 1;
		JButton b = new JButton("OK");
		b.addActionListener((ActionEvent e) -> invokeLater(() -> setVisible(false)));
		add(b, c);
		setResizable(false);
	}

	/**
	 * Changes alert message and displays the alert
	 * 
	 * @param message - new message to display
	 */
	public void show(String message)
	{
		txt.setText(message);
		setVisible(true);
	}
}
