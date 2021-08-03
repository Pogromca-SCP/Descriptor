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
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.nio.file.Path;
import com.itextpdf.text.pdf.PdfReader;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.UncheckedIOException;

import static javax.swing.SwingUtilities.invokeLater;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.nio.file.Files.isRegularFile;
import static com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage;
import static java.nio.file.Files.copy;

/**
 * PDF Files Descriptor
 */
public final class Descriptor extends JFrame
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Contains source directory
	 */
	private JTextField src;

	/**
	 * Contains target directory
	 */
	private JTextField tar;

	/**
	 * File chooser for directory selection
	 */
	private JFileChooser fc;

	/**
	 * Work mode selection
	 */
	private JComboBox<String> mode;

	/**
	 * Number of processed elements
	 */
	private int count;

	/**
	 * Are there any errors?
	 */
	private boolean isCorrect;

	/**
	 * Alerts display window
	 */
	private Alert arr;

	/**
	 * Descriptor construction
	 */
	public Descriptor()
	{
		super("Opisywacz");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 300);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		URL imgUrl = getClass().getResource("/Descriptor.png");

		if (imgUrl != null)
		{
			ImageIcon img = new ImageIcon(imgUrl);
			setIconImage(img.getImage());
		}

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		c.gridx = 0;
		c.gridy = 0;
		String[] petStrings = {"Tryb debugowania", "Certyfikaty-ogólne", "Generali NNW-szkolne", "Listy powitalne-SWRN", "Listy powitalne-CIZ"};
		mode = new JComboBox<String>(petStrings);
		add(mode, c);
		c.gridy = 1;
		add(new JLabel("Lokalizacja z plikami:"), c);
		c.gridy = 2;
		src = new JTextField(40);
		add(src, c);
		c.gridx = 1;
		JButton s = new JButton("Przeglądaj");
		s.addActionListener((ActionEvent e) -> invokeLater(() -> getDir(src, s)));
		add(s, c);
		c.gridx = 0;
		c.gridy = 3;
		add(new JLabel("UWAGA! Program przemieli wszystkie pliki PDF w podanym folderze!"), c);
		c.gridy = 4;
		add(new JLabel("Folder docelowy:"), c);
		c.gridy = 5;
		tar = new JTextField(40);
		add(tar, c);
		c.gridx = 1;
		JButton z = new JButton("Przeglądaj");
		z.addActionListener((ActionEvent e) -> invokeLater(() -> getDir(tar, z)));
		add(z, c);
		c.gridx = 0;
		c.gridy = 6;
		JButton b = new JButton("Do dzieła!");

		b.addActionListener((ActionEvent e) -> invokeLater(() -> {
			b.setEnabled(false);
			descript(src.getText(), mode.getSelectedIndex(), tar.getText());
			b.setEnabled(true);
			isCorrect = true;
			count = 0;
		}));

		add(b, c);
		count = 0;
		isCorrect = true;
		setResizable(false);
		arr = new Alert();
		setVisible(true);
	}

	/**
	 * Choosing a directory
	 * 
	 * @param target - text field to which the directory will be injected
	 * @param parent - a parent button for file chooser
	 */
	private void getDir(JTextField target, JButton parent)
	{
		int val = fc.showOpenDialog(parent);

		if (val == JFileChooser.APPROVE_OPTION)
		{
			target.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * All supported symbols
	 */
	private static String gr = "([A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ -]+[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ])";

	/**
	 * Files descripting
	 * 
	 * @param dir - source directory
	 * @param m - working mode
	 * @param target - target directory
	 */
	private void descript(final String dir, final int m, final String target)
	{
		final Pattern p;
		final String end;
		final boolean debug = (m == 0);

		switch (m - 1)
		{
			case -1:
				p = null;
				end = null;
				break;
			case 0:
				p = Pattern.compile("Ubezpieczony:? *\n?(Imię i nazwisko)? *" + gr);
				end = "_certyfikat.pdf";
				break;
			case 1:
				p = Pattern.compile("osoby: *\n?" + gr);
				end = ")_certyfikat.pdf";
				break;
			case 2:
			case 3:
				p = Pattern.compile("Sz\\.P\\. +" + gr);
				end = "_list powitalny.pdf";
				break;
			default:
				p = null;
				end = null;
				alert("Wystąpił błąd i proces nie został uruchomiony.");
				isCorrect = false;
		}
		
		if (isCorrect)
		{
			try (Stream<Path> paths = walk(get(dir)))
			{
				paths.forEach((Path filePath) -> {
					if (isRegularFile(filePath) && getFileExtension(filePath.toString()).equalsIgnoreCase("pdf"))
					{
						try
						{
							if (getIsCorrect())
							{
								final PdfReader reader = new PdfReader(filePath.toString());
								
								if (debug)
								{
									final java.io.FileWriter writer = new java.io.FileWriter(target + '\\' + getCount() + ".txt");
									writer.write(getTextFromPage(reader, 1));
									writer.flush();
									writer.close();
									inc();
								}
								else
								{
									final Matcher mat = p.matcher(getTextFromPage(reader, 1));

									if (mat.find())
									{
										copy(filePath, get(target + (m - 1 == 1 ? "/(" : '/') + rev(mat.group(mat.groupCount())) + end));
										inc();
									}
								}
							}
						}
						catch (IOException i)
						{
							alert("Wystąpił błąd i proces został zatrzymany. Przetworzono " + getCount() + " elementów.");
							negate();
						}
						catch (Exception e)
						{
							alert("Wystąpił błąd i proces został zatrzymany. Przetworzono " + getCount() + " elementów.");
							negate();
						}
					}
				});
			}
			catch (UncheckedIOException a)
			{
				alert("Błąd! Program nie ma dostępu do podanej lokalizacji. Spróbuj podać inną. (np. folder na pulpicie)");
				isCorrect = false;
			}
			catch (Exception e)
			{
				alert("Wystąpił błąd i proces został zatrzymany. Przetworzono " + count + " elementów.");
				isCorrect = false;
			}
		}

		if (isCorrect)
		{
			alert("Proces przebiegł bez zakłóceń. Przetworzono " + count + " elementów.");
		}
	}

	/**
	 * Checks file's extension
	 * 
	 * @param fullName - file to check
	 * @return - file extension
	 */
	private String getFileExtension(String fullName)
	{
		int dotIndex = fullName.lastIndexOf('.');
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
	 * Checks for errors
	 * 
	 * @return - true if no errors occured
	 */
	private boolean getIsCorrect()
	{
		return isCorrect;
	}

	/**
	 * Notifies an error occurance
	 */
	private void negate()
	{
		isCorrect = false;
	}

	/**
	 * Reverts the names
	 * 
	 * @param org - the name to revert
	 * @return - reverted name
	 * @throws Exception - Unsupported symbol
	 */
	private String rev(String org) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		int spaceIndex = org.lastIndexOf(' ');

		if (spaceIndex == -1)
		{
			throw new Exception("Unsupported symbol!");
		}

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
	private void alert(String message)
	{
		invokeLater(() -> arr.show(message));
	}
}
