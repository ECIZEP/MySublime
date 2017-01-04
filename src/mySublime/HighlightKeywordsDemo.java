package mySublime;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class HighlightKeywordsDemo {
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(700, 700);
		frame.setLocationRelativeTo(null);
		frame.setTitle("MySublime Text");
		ImageIcon icon = new ImageIcon("icon.jpg");
		frame.setIconImage(icon.getImage());
		
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu find = new JMenu("Find");
		JMenu view = new JMenu("View");
		menuBar.add(file);
		menuBar.add(find);
		menuBar.add(view);
		
		JTextPane editor = new JTextPane();
		Color bgColor = new Color(30, 30, 30);
		Font codeFont = new Font("Monospace", Font.PLAIN, 14);
		editor.setFont(codeFont);
		editor.setBackground(bgColor);	
		editor.getDocument().addDocumentListener(new SyntaxHighlighter(editor));
		editor.setCaretColor(Color.WHITE);
		JScrollPane jScrollPane = new JScrollPane(editor);
		
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(jScrollPane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

/**
 * 当文本输入区的有字符插入或者删除时, 进行高亮.
 * 
 * 要进行语法高亮, 文本输入组件的document要是styled document才行. 所以不要用JTextArea. 可以使用JTextPane.
 * 
 * @author Biao
 * 
 */

class SyntaxHighlighter implements DocumentListener {
	private Set<String> keywords;
	private Set<String> punctuations;
	private Set<String> arithmeticOperators;
	
	public static final String[] keywordArray = {"break","case","catch","continue","default","delete","do","else",
			"finally","for","function","if","in","instanceof","new","return","switch","this","throw","try","typeof",
			"var","void","while","with"};//关键词
	public static final String[] punctuationArray = {"\\",":",",",";","(",")","[","]","{","}","$",".","%","<",">"};//标点符号
	public static final String[] arithmeticOperatorsArray = {"+","-","*","/","="};//运算符

	public static final Color normalColor = new Color(230,126,34);//默认颜色
	public static final Color keywordColor = new Color(231,76,60);//关键词颜色
	public static final Color stringColor = new Color(241,196,15);//字符串内容颜色
	public static final Color numberColor = new Color(108,113,196);//数字颜色
	public static final Color functionNameColor = new Color(46,204,113);//函数名称颜色
	public static final Color punctuationColor = Color.WHITE;//标点颜色
	public static final Color attributeColor = Color.WHITE;//属性名称颜色
	

	private Style keywordStyle;	//关键词样式
	private Style normalStyle;	//默认样式
	private Style stringStyle;	//字符串内容样式
	private Style numberStyle;	//数字样式
	private Style functionNameStyle;//函数名称样式
	private Style punctuationStyle;//标点名称样式
	private Style attributeStyle;//标点名称样式
	
	private boolean stringStart = false;

	public SyntaxHighlighter(JTextPane editor) {
		// 准备着色使用的样式
		keywordStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
		normalStyle = ((StyledDocument) editor.getDocument()).addStyle("Normal_Style", null);
		stringStyle = ((StyledDocument) editor.getDocument()).addStyle("String_Style", null);
		functionNameStyle = ((StyledDocument) editor.getDocument()).addStyle("FunctionName_Style", null);
		numberStyle = ((StyledDocument) editor.getDocument()).addStyle("Number_Style", null);
		punctuationStyle = ((StyledDocument) editor.getDocument()).addStyle("Punctuation_Style", null);
		stringStyle = ((StyledDocument) editor.getDocument()).addStyle("String_Style", null);
		attributeStyle = ((StyledDocument) editor.getDocument()).addStyle("attribute_Style", null);
		//默认样式设置
		StyleConstants.setForeground(normalStyle, normalColor);
		//关键词样式设置
		StyleConstants.setForeground(keywordStyle, keywordColor);
		StyleConstants.setBold(keywordStyle, true);
		//函数名样式设置
		StyleConstants.setForeground(functionNameStyle, functionNameColor);
		//数字样式设置
		StyleConstants.setForeground(numberStyle, numberColor);
		//标点样式设置
		StyleConstants.setForeground(punctuationStyle, punctuationColor);
		//字符串样式设置
		StyleConstants.setForeground(stringStyle, stringColor);
		//属性样式设置
		StyleConstants.setForeground(attributeStyle, attributeColor);
		
		// 准备关键字
		keywords = new HashSet<String>();
		addAll(keywords, keywordArray);
		//准备标点符号
		punctuations = new HashSet<String>();
		addAll(punctuations,punctuationArray);
		//准备运算符
		arithmeticOperators = new HashSet<String>();
		addAll(arithmeticOperators, arithmeticOperatorsArray);
		
	}
	
	private void addAll(Set<String> set, String[] array) {
		for(int i = 0; i < array.length;i++){
			set.add(array[i]);
		}
	}

	public void colouring(StyledDocument doc, int pos, int len) throws BadLocationException {
		// 取得插入或者删除后影响到的单词.
		// 例如"public"在b后插入一个空格, 就变成了:"pub lic", 这时就有两个单词要处理:"pub"和"lic"
		// 这时要取得的范围是pub中p前面的位置和lic中c后面的位置
		
		int start = indexOfWordStart(doc, pos);
		int end = indexOfWordEnd(doc, pos + len);
		
		int nextPos = indexOfStringLineEnd(doc, pos);
		System.out.println("nextPos = " + nextPos);
		if(end < nextPos){
			end = nextPos;
		}
		System.out.println("endindex :" + end);
		
		char ch;
		while (start < end) {
			ch = getCharAt(doc, start);
			System.out.println("本轮是" + ch);
			if(indexOfStringLineStart(doc, start) != -1 && ch != '"'){
				SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, stringStyle));
				++start;
			}else if (Character.isLetter(ch) || ch == '_') {
				// 如果是以字母或者下划线开头, 说明是单词
				// pos为处理后的最后一个下标
				start = colouringWord(doc, start);
			} else if(ch == '"' || ch == '\'') {
				SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, stringStyle));
				++start;
			} else if(Character.isDigit(ch)) {
				//数字
				SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, numberStyle));
				++start;
			} else if(arithmeticOperators.contains(String.valueOf(ch))){
				//运算符 使用和关键词一样的样式
				SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, keywordStyle));
				++start;
			} else if(punctuations.contains(String.valueOf(ch))) {
				//标点符号
				SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, punctuationStyle));
				++start;
			}else {
				//默认
				SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, normalStyle));
				++start;
			}
		}
	}
	
	public void colouringString(StyledDocument doc, int startPos, int endPos) {
		
	}

	/**
	 * 对单词进行着色, 并返回单词结束的下标.
	 * 
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public int colouringWord(StyledDocument doc, int pos) throws BadLocationException {
		int wordEnd = indexOfWordEnd(doc, pos);
		String word = doc.getText(pos, wordEnd - pos);

		if (keywords.contains(word)) {
			// 如果是关键字, 就进行关键字的着色
			// 这里有一点要注意, 在insertUpdate和removeUpdate的方法调用的过程中, 不能修改doc的属性.
			// 但我们又要达到能够修改doc的属性, 所以把此任务放到这个方法的外面去执行.
			// 实现这一目的, 可以使用新线程, 但放到swing的事件队列里去处理更轻便一点.
			SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, keywordStyle));
		} else if(doc.getText(wordEnd,1).charAt(0) == '('){
			//如果是函数名，使用函数名的着色
			SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, functionNameStyle));
		}else if (pos-1 >=0 && doc.getText(pos-1, 1).charAt(0) == '.') {
			//如果是属性名，使用属性名字着色
			SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, attributeStyle));
		}else {
			SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, normalStyle));
		}

		return wordEnd;
	}

	/**
	 * 取得在文档中下标在pos处的字符.
	 * 
	 * 如果pos为doc.getLength(), 返回的是一个文档的结束符, 不会抛出异常. 如果pos<0, 则会抛出异常.
	 * 所以pos的有效值是[0, doc.getLength()]
	 * 
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public char getCharAt(Document doc, int pos) throws BadLocationException {
		return doc.getText(pos, 1).charAt(0);
	}

	/**
	 * 取得下标为pos时, 它所在的单词开始的下标. Â±wor^dÂ± (^表示pos, Â±表示开始或结束的下标)
	 * 
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public int indexOfWordStart(Document doc, int pos) throws BadLocationException {
		// 从pos开始向前找到第一个非单词字符.
		for (; pos > 0 && isWordCharacter(doc, pos - 1); --pos);

		return pos;
	}

	/**
	 * 取得下标为pos时, 它所在的单词结束的下标. Â±wor^dÂ± (^表示pos, Â±表示开始或结束的下标)
	 * 
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public int indexOfWordEnd(Document doc, int pos) throws BadLocationException {
		// 从pos开始向后找到第一个非单词字符.
		for (; isWordCharacter(doc, pos); ++pos);

		return pos;
	}

	/**
	 * 如果一个字符是字母, 数字, 下划线, 则返回true.
	 * 
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public boolean isWordCharacter(Document doc, int pos) throws BadLocationException {
		char ch = getCharAt(doc, pos);
		if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') { return true; }
		return false;
	}
	/*
	 * 如果本行当前位置前面的"是奇数个，则返回最后一个"的pos
	 * 否则返回-1
	 */
	public int indexOfStringLineStart(Document doc, int endpos) throws BadLocationException {
		int startpos = endpos;
		for (; startpos > 0 && getCharAt(doc, startpos-1) != '\n' && getCharAt(doc, startpos-1) != '\r'; --startpos);
		String all = doc.getText(startpos, endpos - startpos);
		System.out.println("all:" + all);
		int num = 0;
		for(int i = 0; i < all.length();i++){
			if(all.charAt(i) == '"'){
				num++;
			}
		}
		//System.out.println("num:" + num);
		if(num % 2 == 1){
			//是奇数个，返回之前的一个"的pos
			//System.out.println("上一个\"的位置：" + all.lastIndexOf("\""));
			return all.lastIndexOf("\"");
		}else{
			return -1;
		}
	}
	/*
	 * 如果本行当前位置后面的"存在,返回其pos
	 * 否则返回换行符的位置
	 */
	public int indexOfStringLineEnd(Document doc, int startPos) throws BadLocationException {
		int i;
		for (i = startPos + 1;i < doc.getLength();i++){
			if(doc.getText(i, 1).charAt(0) == '"' || doc.getText(i, 1).charAt(0) == '\n' || doc.getText(i, 1).charAt(0) == '\r'){
				return i;
			}
		}
		return --i;
	}
	
	//public int indexOf
	
	@Override
	public void changedUpdate(DocumentEvent e) {

	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		try {
			colouring((StyledDocument) e.getDocument(), e.getOffset(), e.getLength());
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		try {
			// 因为删除后光标紧接着影响的单词两边, 所以长度就不需要了
			colouring((StyledDocument) e.getDocument(), e.getOffset(), 0);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 完成着色任务
	 * 
	 * @author Biao
	 * 
	 */
	private class ColouringTask implements Runnable {
		private StyledDocument doc;
		private Style style;
		private int pos;
		private int len;

		public ColouringTask(StyledDocument doc, int pos, int len, Style style) {
			this.doc = doc;
			this.pos = pos;
			this.len = len;
			this.style = style;
		}

		public void run() {
			try {
				// 这里就是对字符进行着色
				doc.setCharacterAttributes(pos, len, style, true);
			} catch (Exception e) {}
		}
	}
}

