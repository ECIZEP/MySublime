package mySublime;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;



public class HighlightKeywordsDemo implements ActionListener {
	// setup icons - File Menu
	private final ImageIcon newIcon = new ImageIcon("icons/new.png");
	private final ImageIcon openIcon = new ImageIcon("icons/open.png");
	private final ImageIcon saveIcon = new ImageIcon("icons/save.png");
	private final ImageIcon closeIcon = new ImageIcon("icons/close.png");
	
	// setup icons - Edit Menu
	private final ImageIcon cutIcon = new ImageIcon("icons/cut.png");
	private final ImageIcon copyIcon = new ImageIcon("icons/copy.png");
	private final ImageIcon pasteIcon = new ImageIcon("icons/paste.png");
	private final ImageIcon selectAllIcon = new ImageIcon("icons/selectall.png");
	
	public static final String EDITOR_NAME = "MySublime Text";

	private JMenuItem openFile,newFile,saveFile,exit,copy,cut,paste,selectAll;
	private JFrame frame;
	private JTextPane editor;
	private File currentFile = null;
	private boolean isModefied = false;
	
	public HighlightKeywordsDemo() {
		frame = new JFrame();
		frame.setSize(700, 700);
		frame.setLocationRelativeTo(null);
		frame.setTitle("Untitled - " + EDITOR_NAME);
		ImageIcon icon = new ImageIcon("icon.jpg");
		frame.setIconImage(icon.getImage());
		
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		newFile = new JMenuItem("New File", newIcon);
		newFile.addActionListener(this);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		openFile = new JMenuItem("Open File", openIcon);
		openFile.addActionListener(this);
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		//JMenuItem openFolder = new JMenuItem("Open Folder");
		saveFile = new JMenuItem("Save", saveIcon);
		saveFile.addActionListener(this);
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		//JMenuItem saveAs = new JMenuItem("Save As");
		exit = new JMenuItem("exit", closeIcon);
		exit.addActionListener(this);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		file.add(newFile);
		file.add(openFile);
		//file.add(openFolder);
		//file.add(new JSeparator());
		file.add(saveFile);
		//file.add(saveAs);
		//file.add(new JSeparator());
		file.add(exit);
		
		JMenu edit = new JMenu("Edit");
		copy =  new JMenuItem("Copy", copyIcon);
		copy.addActionListener(this);
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		cut = new JMenuItem("Cut", cutIcon);
		cut.addActionListener(this);
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		paste = new JMenuItem("Paste", pasteIcon);
		paste.addActionListener(this);
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		selectAll = new JMenuItem("Slect All", selectAllIcon);
		selectAll.addActionListener(this);
		selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		edit.add(copy);
		edit.add(cut);
		edit.add(paste);
		edit.add(selectAll);
		
		JMenu view = new JMenu("View");
		menuBar.add(file);
		menuBar.add(edit);
		
		editor = new JTextPane();
		Color bgColor = new Color(30, 30, 30);
		Font codeFont = new Font("Monospace", Font.PLAIN, 14);
		editor.setFont(codeFont);
		editor.setBackground(bgColor);	
		
		editor.getDocument().addDocumentListener(new SyntaxHighlighterListener(this, editor));
		editor.setCaretColor(Color.WHITE);
		JScrollPane jScrollPane = new JScrollPane(editor);
		
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(jScrollPane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public boolean getIsModefied() {
		return isModefied;
	}
	
	public void setIsModefied(boolean isModefied) {
		if(isModefied){
			if(currentFile != null){
				frame.setTitle(currentFile.getPath() + " ● - " + EDITOR_NAME);
			}else{
				frame.setTitle("Untitled ● - " + EDITOR_NAME);
			}
			
		}else {
			if(currentFile != null){
				frame.setTitle(currentFile.getPath() + " - " + EDITOR_NAME);
			}else{
				frame.setTitle("Untitled - " + EDITOR_NAME);
			}
		}
		this.isModefied = isModefied;
	}
	
	public static void main(String[] args) {
		new HighlightKeywordsDemo();
	}
	
	private boolean openFile() {
		JFileChooser fileChooser = new JFileChooser();
		int option = fileChooser.showOpenDialog(frame);
		if(option == JFileChooser.APPROVE_OPTION){
			currentFile = fileChooser.getSelectedFile();
			frame.setTitle(currentFile.getPath() + " - " + EDITOR_NAME);
			try {
				BufferedReader in = new BufferedReader(new FileReader(currentFile));
				String all = "",row;
				while((row = in.readLine()) != null){
					all = all + row + "\r\n";
				}
				editor.setText(all);
				in.close();
				//打开文件后 文件处于未修改状态
				setIsModefied(false);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
		}
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == exit){
			if (isModefied) {
				if(JOptionPane.showConfirmDialog(frame, "文件没有保存,确定退出程序吗？") == JOptionPane.YES_OPTION){
					frame.dispose();
				}
			}
		}else if(e.getSource() == saveFile){
			if(currentFile == null){
				JFileChooser fileChoose = new JFileChooser();
				int option = fileChoose.showSaveDialog(frame);
				if (option == JFileChooser.APPROVE_OPTION) {
					currentFile = fileChoose.getSelectedFile();
				}
			}
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(currentFile.getPath()));
				// Write the contents of the TextArea to the file
				out.write(editor.getText());
				// Close the file stream
				out.close();
				//保存后处于未修改状态
				setIsModefied(false);
			} catch (Exception e2) {
				// TODO: handle exception
				System.out.println(e2.getMessage());
			}
			
	
		}else if(e.getSource() == openFile){
			if(!isModefied){
				openFile();
			}else{
				if(JOptionPane.showConfirmDialog(frame, "文件没有保存,确定打开新的文件吗？") == JOptionPane.YES_OPTION){
					openFile();
				}
			}
		}else if(e.getSource() == newFile) {
			if(!isModefied){
				currentFile = null;
				setIsModefied(false);
			}else{
				if(JOptionPane.showConfirmDialog(frame, "文件没有保存,确定新建文件吗？") == JOptionPane.YES_OPTION){
					currentFile = null;
					setIsModefied(false);
				}
			}
		}else if (e.getSource() == selectAll) {
			editor.selectAll();
		} else if (e.getSource() == copy) {
			editor.copy();
		} else if (e.getSource() == cut) {
			editor.cut();
		} else if (e.getSource() == paste) {
			editor.paste();
		}
	}
}
