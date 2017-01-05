package mySublime;

import java.awt.Color;
import java.awt.Frame;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


/**
 * 当文本输入区的有字符插入或者删除时, 进行高亮.
 * 
 * 要进行语法高亮, 文本输入组件的document要是styled document才行. 所以不要用JTextArea. 可以使用JTextPane.
 * 
 * @author Biao
 * 
 */

class SyntaxHighlighterListener implements DocumentListener {
	private Set<String> keywords;
	private Set<String> punctuations;
	private Set<String> arithmeticOperators;
	
	public static final int COMMENT_NORMAL = 0; 	// /**/ 普通注释风格
	public static final int COMMENT_WEB = 1;		// <!-- --> web注释风格
	
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
	public static final Color commentColor = new Color(146, 145, 145);//注释颜色
	

	private Style keywordStyle;	//关键词样式
	private Style normalStyle;	//默认样式
	private Style stringStyle;	//字符串内容样式
	private Style numberStyle;	//数字样式
	private Style functionNameStyle;//函数名称样式
	private Style punctuationStyle;//标点样式
	private Style attributeStyle;//属性名称样式
	private Style commentStyle;//注释样式
	
	private boolean stringStart = false;
	private HighlightKeywordsDemo UI;

	public SyntaxHighlighterListener(HighlightKeywordsDemo UI, JTextPane editor) {
		// 准备着色使用的样式
		keywordStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
		normalStyle = ((StyledDocument) editor.getDocument()).addStyle("Normal_Style", null);
		stringStyle = ((StyledDocument) editor.getDocument()).addStyle("String_Style", null);
		functionNameStyle = ((StyledDocument) editor.getDocument()).addStyle("FunctionName_Style", null);
		numberStyle = ((StyledDocument) editor.getDocument()).addStyle("Number_Style", null);
		punctuationStyle = ((StyledDocument) editor.getDocument()).addStyle("Punctuation_Style", null);
		stringStyle = ((StyledDocument) editor.getDocument()).addStyle("String_Style", null);
		attributeStyle = ((StyledDocument) editor.getDocument()).addStyle("attribute_Style", null);
		commentStyle = ((StyledDocument) editor.getDocument()).addStyle("comment_Style", null);
		//默认样式设置
		StyleConstants.setForeground(normalStyle, normalColor);
		//关键词样式设置 粗体
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
		//注释样式设置,斜体
		StyleConstants.setForeground(commentStyle, commentColor);
		StyleConstants.setItalic(commentStyle, true);
		
		// 准备关键字
		keywords = new HashSet<String>();
		addAll(keywords, keywordArray);
		//准备标点符号
		punctuations = new HashSet<String>();
		addAll(punctuations,punctuationArray);
		//准备运算符
		arithmeticOperators = new HashSet<String>();
		addAll(arithmeticOperators, arithmeticOperatorsArray);
		
		this.UI = UI;
		
	}
	
	private void addAll(Set<String> set, String[] array) {
		for(int i = 0; i < array.length;i++){
			set.add(array[i]);
		}
	}

	/**
	 * 对字符优先级进行判断，跳转到相应的函数高亮
	 * @param doc
	 * @param pos
	 * @param len
	 * @throws BadLocationException
	 */
	public void colouring(StyledDocument doc, int pos, int len) throws BadLocationException {
		// 取得插入或者删除后影响到的单词.
		// 例如"public"在b后插入一个空格, 就变成了:"pub lic", 这时就有两个单词要处理:"pub"和"lic"
		// 这时要取得的范围是pub中p前面的位置和lic中c后面的位置
		
		int start = indexOfWordStart(doc, pos);
		int end = indexOfWordEnd(doc, pos + len);
		
		int nextDoubleStringPos = indexOfStringLineNext(doc, pos, '"');
		int nextSingleStringPos = indexOfStringLineNext(doc, pos, '\'');
		
		//System.out.println("worldEnd:" + end);
		if(end < nextDoubleStringPos){
			end = nextDoubleStringPos;
		}
		if(end < nextSingleStringPos){
			end = nextSingleStringPos;
		}
		System.out.println("startindex:" + start);
		System.out.println("endindex :" + end);
		
		char ch;
		while (start < end) {
			ch = getCharAt(doc, start);
			System.out.println(ch);
			int commentPrevWeb = indexOfCommentPrev(doc, start, COMMENT_WEB);
			int commentPrevNormal = indexOfCommentPrev(doc, start, COMMENT_NORMAL);
			if(commentPrevWeb != -1){
				//前面有未闭合的<!-- 或者当闭合时，将闭合的最后一个字符>着色
				//<!--sdfgdsdfg<!--gdgdfgdfgsdg-->-->  bug？不算是
				start = colouringComment(doc, start, commentPrevWeb, true);
			}else if (commentPrevNormal != -1) {
				start = colouringComment(doc, start, commentPrevNormal, true);
			}else if (isExistLineCommentPrevious(doc, start)) {
				//行注释着色处理
				start = colouringLineComment(doc, start, ch);
			}else if(indexOfStringLinePrev(doc, start,'"') != -1 || ch == '"'){
				//本行前面有奇数个" 或者当前符号是" 则为字符串色
				start = colouringString(doc, start, true, ch);				
			}else if (indexOfStringLinePrev(doc, start,'\'') != -1 || ch == '\'') {
				//本行前面有奇数个' 或者当前符号是' 则为字符串色
				start = colouringString(doc, start, false, ch);	
			}else if (Character.isLetter(ch) || ch == '_') {
				// 如果是以字母或者下划线开头, 说明是单词
				// pos为处理后的最后一个下标
				start = colouringWord(doc, start);
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
	
	//public getPriority()
	
	/**
	 * 如果前面有未闭合的注释符号<!-- /*，则注释着色至闭合处
	 * 若无闭合，则注释到文档末尾
	 * @param doc
	 * @param start
	 * @param commentPrev
	 * @param isWebComment
	 * @return
	 * @throws BadLocationException
	 */
	public int colouringComment(StyledDocument doc, int start, int commentPrev, boolean isWebComment) throws BadLocationException{
		int endPos;
		if(isWebComment){
			endPos = indexOfCommentNext(doc, start, COMMENT_WEB);
		}else{
			endPos = indexOfCommentNext(doc, start, COMMENT_NORMAL);
		}
		SwingUtilities.invokeLater(new ColouringTask(doc, commentPrev, endPos - commentPrev, commentStyle));
		return endPos;
	} 
	
	/**
	 * 行注释着色
	 * @param doc
	 * @param start
	 * @param ch
	 * @return
	 * @throws BadLocationException
	 */
	public int colouringLineComment(StyledDocument doc, int start, char ch) throws BadLocationException {
		if(ch == '/'){
			SwingUtilities.invokeLater(new ColouringTask(doc, start - 1, 2, commentStyle));
			return ++start;
		}else{
			//整行直接注释，性能会好点吗？值得思考 
			int lineEndPos = getLineEndPos(doc,start);
			SwingUtilities.invokeLater(new ColouringTask(doc, start, lineEndPos - start, commentStyle));
			return lineEndPos;
		}
	}
	
	/**
	 * 本行前面有奇数个"，则为字符串色
	 * 本行前面有偶数个"，但是当前符号是"，则字符串色
	 * 当" '嵌套的时候会出现bug,修复比较麻烦暂时不做处理
	 * @param doc
	 * @param start
	 * @param isDouble 是否是双引号
	 * @param ch
	 * @return 下一个待处理字符的pos
	 * @throws BadLocationException
	 */
	public int colouringString(StyledDocument doc, int start, boolean isDouble, char ch) throws BadLocationException {
		char type;
		if(isDouble){
			type = '"';
		}else{
			type = '\'';
		}
		if(ch != type){
			int endPos = indexOfStringLineNext(doc, start, type);
			SwingUtilities.invokeLater(new ColouringTask(doc, start, endPos - start + 1, stringStyle));
			return endPos + 1;
		}else{
			SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, stringStyle));
			return ++start;
		}
	}
	

	/**
	 * 对单词进行判断,根据不同类型着色，并返回单词结束的下标.
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
	
	
	/**
	 * @param doc
	 * @param endpos
	 * @param ch
	 * @return 如果本行当前位置前面的"是奇数个，则返回最后一个"的pos  否则返回-1
	 * @throws BadLocationException
	 */
	public int indexOfStringLinePrev(Document doc, int endpos,char ch) throws BadLocationException {
		int startpos = endpos;
		for (; startpos > 0 && getCharAt(doc, startpos-1) != '\n' && getCharAt(doc, startpos-1) != '\r'; --startpos);
		String all = doc.getText(startpos, endpos - startpos);
		System.out.println("行首至POS的字符串:" + all);
		int num = 0;
		for(int i = 0; i < all.length();i++){
			if(all.charAt(i) == ch){
				//如果引号'"在注释区域，则跳过该引号，不做计算
				if(indexOfCommentPrev(doc, startpos+i, COMMENT_WEB) == -1 && indexOfCommentPrev(doc, startpos+i, COMMENT_NORMAL) == -1)
				{
					num++;
				}
			}
		}
		if(num % 2 == 1){
			//是奇数个，返回之前的一个"的pos
			return all.lastIndexOf(ch);
		}else{
			return -1;
		}
	}
	
	
	/**
	 * @param doc
	 * @param startPos
	 * @param ch
	 * @return 如果本行当前位置后面的"存在,返回其pos，否则返回本行最后一个字符的位置
	 * @throws BadLocationException
	 */
	public int indexOfStringLineNext(Document doc, int startPos, char ch) throws BadLocationException {
		int i;
		//删除时a时：//'a  传过来的pos是删除的符号的offset 3,而此时3的位置是换行符,所以需要对当前是否换行做一个判断  
		if(getCharAt(doc, startPos) == '\n' || getCharAt(doc, startPos) == '\r'){
			return startPos;
		}
		for (i = startPos + 1;i < doc.getLength();i++){
			if(getCharAt(doc, i) == ch || getCharAt(doc, i) == '\'' || getCharAt(doc, i) == '\n' || getCharAt(doc, i) == '\r'){
				return i;
			}
		}
		return i;
	}
	

	/**
	 * 
	 * 判断当前行是否存在行注释符号
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public boolean isExistLineCommentPrevious(Document doc, int pos) throws BadLocationException{
		for (; pos > 0; pos--) {
			if(getCharAt(doc, pos) == '/' && getCharAt(doc, pos - 1) == '/'){
				return true;
			}
			if(getCharAt(doc, pos) == '\r' || getCharAt(doc, pos) == '\n'){
				return false;
			}
		}
		return false;
	}
	
	/**
	 * 返回当前行的行尾pos
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public int getLineEndPos(Document doc, int pos) throws BadLocationException {
		for(;pos < doc.getLength() && getCharAt(doc, pos) != '\r' && getCharAt(doc, pos) != '\n'; pos++);
		return pos;
	}
	
	
	/**
	 * 判断前面是否有未闭合的<!-- 或者/* 有则返回第一个未闭合的
	 * @param doc
	 * @param pos
	 * @param type
	 * @return
	 * @throws BadLocationException
	 */
	public int indexOfCommentPrev(Document doc, int pos, int type) throws BadLocationException {
		//<!-- 当输入最后一个-时，变色，所以pos+1
		String all = doc.getText(0, pos);
		System.out.println("all:" + all);
		boolean commentYes = false;
		int returnValue = -1;
		if(type == COMMENT_NORMAL){
			for(int i = 0; i < all.length();i++){
				if(i+1 < all.length() && all.charAt(i) == '/' && all.charAt(i+1) == '*'){
					if(!commentYes){
						//记住第一个为闭合的/*
						returnValue = i;
					}
					commentYes = true;
				}
				if(i+1 < all.length() && all.charAt(i) == '*' && all.charAt(i+1) == '/'){
					commentYes = false;
					System.out.println(commentYes);
				}
			}
			if(commentYes){
				return returnValue;
			}else{
				return -1;
			}
		}else if(type == COMMENT_WEB){
			for(int i = 0; i < all.length();i++){
				if(i+3 < all.length() && all.charAt(i) == '<' && all.charAt(i+1) == '!' && all.charAt(i+2) == '-' && all.charAt(i+3) == '-'){
					if(!commentYes){
						//记住第一个为闭合的<!--
						returnValue = i;
					}
					commentYes = true;
				}
				if(i+2 < all.length() && all.charAt(i) == '-' && all.charAt(i+1) == '-' && all.charAt(i+2) == '>'){
					commentYes = false;
				}
			}
			if(commentYes){
				return returnValue;
			}else{
				return -1;
			}
		}
		return -1;
	}
	
	
	/**
	 * 从自身开始，得到下一个注释闭合符号的pos
	 * @param doc
	 * @param pos
	 * @param type
	 * @return
	 * @throws BadLocationException
	 */
	public int indexOfCommentNext(Document doc, int pos, int type) throws BadLocationException {
		int nextPos;
		if(type == COMMENT_NORMAL){
			for (nextPos = pos - 1;nextPos < doc.getLength();nextPos++){
				if(getCharAt(doc, nextPos) == '*' && getCharAt(doc, nextPos+1) == '/'){
					return nextPos + 2;
				}
			}
			return nextPos;
		}else if(type == COMMENT_WEB){
			for (nextPos = pos - 1;nextPos < doc.getLength();nextPos++){
				if(getCharAt(doc, nextPos) == '-' && getCharAt(doc, nextPos+1) == '-' && getCharAt(doc, nextPos+2) == '>'){
					return nextPos + 3;
				}
			}
			return nextPos;
		}
		return -1;
	}
	
	
	/**
	 * 当前行是否存在行注释//
	 * @param doc
	 * @param pos
	 * @return
	 * @throws BadLocationException
	 */
	public int isExistCommentLinePrevious(Document doc, int pos) throws BadLocationException{
		boolean lineFlag = true;
		int returnValue = 0;
		for (int i = pos; i >= 0; i++) {
			if(lineFlag && getCharAt(doc, i) == '/' && getCharAt(doc, i - 1) == '/'){
				returnValue++;
				lineFlag = false;
			}
			if(getCharAt(doc, i) == '-' && getCharAt(doc, i-1) == '/'){
				return 1;
			}
		}
		return 0;
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		if(UI != null && !UI.getIsModefied()){
			UI.setIsModefied(true);
		}
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
			System.out.println("删除pos" + e.getOffset());
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
