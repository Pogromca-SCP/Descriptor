/*
 * This file is part of the PDF Descriptor project.
 * Copyright (C) 2021 Pogromca SCP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package descriptor;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import java.awt.Dimension;
import java.net.URL;
import javax.swing.ImageIcon;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.util.stream.Stream;
import java.nio.file.Path;
import com.itextpdf.text.pdf.PdfReader;
import java.util.regex.Matcher;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import org.json.easy.serialization.JSONReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

import static java.awt.Toolkit.getDefaultToolkit;
import static javax.swing.SwingUtilities.invokeLater;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.nio.file.Files.isRegularFile;
import static com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage;
import static java.nio.file.Files.copy;
import static org.json.easy.serialization.JSONSerializer.deserializeArray;

/**
 * PDF Files Descriptor
 */
public class Descriptor extends JFrame
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Contains source directory
	 */
	private final JTextField src;

	/**
	 * Contains target directory
	 */
	private final JTextField tar;

	/**
	 * File chooser for directory selection
	 */
	private final JFileChooser fc;

	/**
	 * Work mode selection
	 */
	private final JComboBox<Mode> mode;

	/**
	 * Number of processed elements
	 */
	private int count;

	/**
	 * Alerts display window
	 */
	private final Alert arr;

	/**
	 * Descriptor construction
	 */
	public Descriptor()
	{
		super("Opisywacz");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 300);
		final Dimension dim = getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		final URL imgUrl = getClass().getResource("/Descriptor.png");

		if (imgUrl != null)
		{
			setIconImage(new ImageIcon(imgUrl).getImage());
		}

		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		c.gridx = 0;
		c.gridy = 0;
		arr = new Alert();
		mode = new JComboBox<Mode>(loadModes());
		add(mode, c);
		++c.gridy;
		add(new JLabel("Lokalizacja z plikami:"), c);
		++c.gridy;
		src = new JTextField(40);
		add(src, c);
		++c.gridx;
		final JButton s = new JButton("Przeglądaj");
		s.addActionListener(e -> invokeLater(() -> getDir(src, s)));
		add(s, c);
		--c.gridx;
		++c.gridy;
		add(new JLabel("UWAGA! Program przemieli wszystkie pliki PDF w podanym folderze!"), c);
		++c.gridy;
		add(new JLabel("Folder docelowy:"), c);
		++c.gridy;
		tar = new JTextField(40);
		add(tar, c);
		++c.gridx;
		final JButton z = new JButton("Przeglądaj");
		z.addActionListener(e -> invokeLater(() -> getDir(tar, z)));
		add(z, c);
		--c.gridx;
		++c.gridy;
		final JButton b = new JButton("Do dzieła!");

		b.addActionListener(e -> invokeLater(() -> {
			b.setEnabled(false);
			descript(src.getText(), (Mode) mode.getSelectedItem(), tar.getText());
			b.setEnabled(true);
			count = 0;
		}));

		add(b, c);
		count = 0;
		setResizable(false);
		setVisible(true);
	}

	/**
	 * Choosing a directory
	 * 
	 * @param target - text field to which the directory will be injected
	 * @param parent - a parent button for file chooser
	 */
	private void getDir(final JTextField target, final JButton parent)
	{
		final int val = fc.showOpenDialog(parent);

		if (val == JFileChooser.APPROVE_OPTION)
		{
			target.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * Files descripting
	 * 
	 * @param dir - source directory
	 * @param m - working mode
	 * @param target - target directory
	 */
	private void descript(final String dir, final Mode mode, final String target)
	{
		try (Stream<Path> paths = walk(get(dir)))
		{
			paths.forEach(filePath -> {
				if (isRegularFile(filePath) && getFileExtension(filePath.toString()).equalsIgnoreCase("pdf"))
				{
					try
					{
						final PdfReader reader = new PdfReader(filePath.toString());
						
						if (mode.name == null)
						{
							final java.io.FileWriter writer = new java.io.FileWriter(target + '/' + getCount() + ".txt");
							writer.write(getTextFromPage(reader, 1));
							writer.flush();
							writer.close();
							inc();
						}
						else
						{
							final Matcher mat = mode.pattern.matcher(getTextFromPage(reader, mode.page));

							if (mat.find())
							{
								final String gr = mat.group(mat.groupCount());
								final String rev = mode.revertText ? rev(gr) : gr;
								final String tmp = mode.addParenthesis ? ('(' + rev  + ')') : rev;
								copy(filePath, get(target + '/' + tmp + mode.postfix));
								inc();
							}
						}
					}
					catch (Exception e)
					{
						return;
					}
				}
			});
		}
		catch (UncheckedIOException a)
		{
			alert("Błąd! Program nie ma dostępu do podanej lokalizacji. Spróbuj podać inną. (np. folder na pulpicie)");
			return;
		}
		catch (Exception e)
		{
			alert("Wystąpił błąd i proces został zatrzymany. Przetworzono " + count + " elementów.");
			return;
		}
		
		alert("Proces przebiegł bez zakłóceń. Przetworzono " + count + " elementów.");
	}

	/**
	 * Checks file's extension
	 * 
	 * @param fullName - file to check
	 * @return - file extension
	 */
	private String getFileExtension(final String fullName)
	{
		final int dotIndex = fullName.lastIndexOf('.');
		return (dotIndex == -1) ? "" : fullName.substring(dotIndex + 1);
	}

	/**
	 * Increments the counter
	 */
	private void inc()
	{
		++count;
	}

	/**
	 * Returns the counter
	 * 
	 * @return - current counter value
	 */
	private int getCount()
	{
		return count;
	}

	/**
	 * Reverts the names
	 * 
	 * @param org - the name to revert
	 * @return - reverted name
	 * @throws Exception - Unsupported symbol
	 */
	private String rev(final String org)
	{
		final int spaceIndex = org.lastIndexOf(' ');

		if (spaceIndex == -1)
		{
			return org;
		}

		final StringBuilder sb = new StringBuilder();
		sb.append(org.substring(spaceIndex + 1));
		sb.append(' ');
		sb.append(org.substring(0, spaceIndex));
		return sb.toString();
	}

	/**
	 * Displays an alert window with message
	 * 
	 * @param message - message to display
	 */
	private void alert(final String message)
	{
		invokeLater(() -> arr.show(message));
	}
	
	/**
	 * Loads program configuration
	 * 
	 * @return Loaded configuration
	 */
	private Mode[] loadModes()
	{
		final LinkedList<Mode> res = new LinkedList<Mode>();
		res.add(new Mode(null));
		
		try (JSONReader reader = new JSONReader(new FileReader("wzory.json")))
		{
			deserializeArray(reader).forEach(val -> res.add(new Mode(val.asObject())));;
		}
		catch (FileNotFoundException f)
		{
			alert("Nie znaleziono pliku konfiguracyjnego!");
		}
		
		return res.toArray(new Mode[res.size()]);
	}
}
