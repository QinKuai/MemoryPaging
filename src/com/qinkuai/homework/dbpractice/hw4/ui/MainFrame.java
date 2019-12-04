package com.qinkuai.homework.dbpractice.hw4.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.qinkuai.homework.dbpractice.hw4.memory.MainMemoryManager;
import com.qinkuai.homework.dbpractice.hw4.memory.PageItem;

public class MainFrame {
	// 系统默认字体
	private Font sysFont = new Font("微软雅黑", Font.PLAIN, 12);
	// 当前文本文件
	private File file;

	public MainFrame() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		uiInit();
	}

	private void uiInit() {
		JFrame mainFrame = new JFrame("MEMORY PAGING SIMULATION");

		JPanel panel1 = new JPanel();

		JLabel label1 = new JLabel("Page");
		label1.setFont(sysFont);
		JComboBox<String> comboBox1 = new JComboBox<>();
		comboBox1.setFont(sysFont);
		comboBox1.addItem("Page 0");

		JButton button1 = new JButton("Read");
		JButton button2 = new JButton("Save");
		JButton button3 = new JButton("Add a page");
		JButton button4 = new JButton("Oepn file");
		button1.setFont(sysFont);
		button2.setFont(sysFont);
		button3.setFont(sysFont);
		button4.setFont(sysFont);

		panel1.add(label1);
		panel1.add(comboBox1);
		panel1.add(button1);
		panel1.add(button2);
		panel1.add(button3);
		panel1.add(button4);

		JPanel panel2 = new JPanel();

		JLabel label2 = new JLabel("Page number in memory");
		label2.setFont(sysFont);
		JTextField jtf1 = new JTextField();
		jtf1.setFont(sysFont);
		jtf1.setPreferredSize(new Dimension(350, 30));

		panel2.add(label2);
		panel2.add(jtf1);

		JPanel panel3 = new JPanel();
		JTextArea jta1 = new JTextArea();
		jta1.setFont(sysFont);
		JScrollPane scrollPane = new JScrollPane(jta1);
		scrollPane.setPreferredSize(new Dimension(500, 400));

		panel3.add(scrollPane);

		// 为一些部件添加监听器
		// 读取文件的监听器
		button4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 通过选择窗口选入对应文件
				openFile(mainFrame);
				if (file != null) {
					try {
						mainFrame.setTitle(file.getAbsolutePath());
						FileInputStream fis = new FileInputStream(file);
						byte[] buf = new byte[4 * 1024];

						int counter = fis.read(buf);
						int wholeSize = 0;
						comboBox1.removeItemAt(0);
						while (counter != -1) {
							comboBox1.addItem("Page " + wholeSize++);
							counter = fis.read(buf);
						}
						MainMemoryManager.getInstance().generatePages(wholeSize);
						fis.close();
						JOptionPane.showMessageDialog(mainFrame, "文件已生成对应的模拟页表");
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		});

		// 读取内存中数据的按钮
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (file == null) {
					JOptionPane.showMessageDialog(mainFrame, "先选择文件", "Waring", JOptionPane.WARNING_MESSAGE);
					return;
				}

				int index = comboBox1.getSelectedIndex();
				
				if (index >= 9) {
					JOptionPane.showMessageDialog(null, "You need pay for this action", null, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				
				
				byte[] bytes = MainMemoryManager.getInstance().readFromMemory(index, file);
				String content = new String(bytes);
				jta1.setText(content);

				// 刷新当前内存中的页表号
				StringBuffer strBuf = new StringBuffer();
				int counter = 0;
				for (PageItem item : MainMemoryManager.getInstance().getPageQueue()) {
					strBuf.append(item.getPageNo() + " ");
					counter++;
				}
				for (int i = 0; i < MainMemoryManager.memoryBlocksNumber - counter; i++) {
					strBuf.append(-1 + " ");
				}
				jtf1.setText(strBuf.toString());
			}
		});

		// 保存当前窗口中的文本
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (file == null) {
					JOptionPane.showMessageDialog(mainFrame, "先选择文件", "Waring", JOptionPane.WARNING_MESSAGE);
					return;
				}

				String content = jta1.getText();
				if (content.length() > 4 * 1024) {
					JOptionPane.showMessageDialog(mainFrame, "更新的内容超出了指定块大小", null, JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					MainMemoryManager.getInstance().writeToDisk(file, comboBox1.getSelectedIndex(), content.getBytes(),
							content.length(), true);
					JOptionPane.showMessageDialog(mainFrame, "保存成功", null, JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(mainFrame, "更新的内容超出了指定块大小", null, JOptionPane.ERROR_MESSAGE);
					return;
				}

			}
		});

		// 为当前文件添加一个页
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (file == null) {
					JOptionPane.showMessageDialog(mainFrame, "先选择文件", "Waring", JOptionPane.WARNING_MESSAGE);
					return;
				}

				MainMemoryManager.getInstance().addNewPageToMem(file);

				comboBox1.addItem("Page " + (MainMemoryManager.getInstance().getPages().size() - 1));
				// 刷新当前内存中的页表号
				StringBuffer strBuf = new StringBuffer();
				int counter = 0;
				for (PageItem item : MainMemoryManager.getInstance().getPageQueue()) {
					strBuf.append(item.getPageNo() + " ");
					counter++;
				}
				for (int i = 0; i < MainMemoryManager.memoryBlocksNumber - counter; i++) {
					strBuf.append(-1 + " ");
				}
				jtf1.setText(strBuf.toString());
			}
		});

		mainFrame.add(panel1, BorderLayout.NORTH);
		mainFrame.add(panel2, BorderLayout.CENTER);
		mainFrame.add(panel3, BorderLayout.SOUTH);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				button4.requestFocus();
			}
		});

		// 总窗口缩紧
		mainFrame.pack();
		// 居中
		mainFrame.setLocationRelativeTo(null);
		// 关闭时退出
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 不可缩放
		mainFrame.setResizable(false);
		// 窗口可见
		mainFrame.setVisible(true);
	}

	// 文件选择框
	private void openFile(JFrame mainFrame) {
		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(false);
		jfc.removeChoosableFileFilter(jfc.getChoosableFileFilters()[0]);

		FileNameExtensionFilter filterTxt = new FileNameExtensionFilter("txt文件(*.txt)", "txt");
		jfc.addChoosableFileFilter(filterTxt);

		jfc.setFileFilter(filterTxt);

		jfc.setCurrentDirectory(new File("res"));
		int f = jfc.showOpenDialog(mainFrame);
		if (f == JFileChooser.APPROVE_OPTION) {
			file = jfc.getSelectedFile();
			mainFrame.setTitle(file.getAbsolutePath());
		}
	}

}
