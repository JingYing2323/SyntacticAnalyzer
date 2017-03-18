import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

public class SyntacticAnalyzer {
	static char Vn[] = { 'E', 'T', 'G', 'F', 'S' };
	static char Vt[] = { '+', '-', '*', '/', '(', ')', 'i', '#', '@' };

	static boolean First[][] = new boolean[Vn.length][Vt.length + 1];
	static boolean Follow[][] = new boolean[Vn.length][Vt.length + 1];
	static String M[][] = new String[Vn.length][Vt.length - 1];
	static String Grammer[] = { "E->TG", "G->+TG|-TG", "G->@", "T->FS", "S->*FS|/FS", "S->@", "F->(E)", "F->i" };

	static int or(int i, String s) {// 返回最近的在其右边一个“|”位置
		for (i = i + 1; i < s.length(); i++) {
			if (s.charAt(i) == '|')
				return i;// 存在，就返回位置
		}
		return -1;// 返回-1表示没有“|”在其右边
	}

	static int vn(char c) {// 返回c在非终结符表中的位置
		int i;
		for (i = 0; i < Vn.length; i++) {
			if (c == Vn[i])
				return i;// 在表中，就返回位置
		}
		return -1;// 返回-1表示不在表中
	}

	static int vt(char c) {// 返回c在终结符表中的位置
		int i;
		for (i = 0; i < Vt.length; i++) {
			if (c == Vt[i])
				return i;// 在表中，就返回位置
		}
		return -1;// 返回-1表示不在表中
	}

	static void addfi(String s, int j) {// 求关于某一个产生式的first集
		int v = vn(s.charAt(0));// v为产生式左边的非终结符
		int i;
		if (vt(s.charAt(j)) != -1) {// 产生式右边第一个为终结符
			First[v][vt(s.charAt(j))] = true;// 就把s.charAt(j)加入s.charAt(0)的first集
		} else {// 产生式右边第一个为非终结符
			if (!First[vn(s.charAt(j))][Vt.length]) {// 如果s.charAt(j)的first集没有求，先求s.charAt(j)的first集
				first(s.charAt(j));
			}
			for (i = 0; i < Vt.length; i++) {// 把s.charAt(j)的first集中不为@的加入s.charAt(0)的first集
				if (First[vn(s.charAt(j))][i] && Vt[i] != '@') {
					First[v][i] = true;
				}
			}
			if (vt('@') != -1)// 终结符中有@
				if (First[vn(s.charAt(j))][vt('@')]) {// 如果@属于当前s.charAt(j)的first集
					if (j == s.length() - 1) {// j=s.length()-1就将@加入s.charAt(0)的first集
						First[v][vt('@')] = true;
						return;
					}
					if (s.charAt(j + 1) != '|') {// s.charAt(j+1)不是“|”就将s.charAt(j+1)的first集加入s.charAt(0)的first集
						j++;
						addfi(s, j);
					} else {// s.charAt(j+1)是“|”就将@加入s.charAt(0)的first集
						First[v][vt('@')] = true;
					}
				}
		}
	}

	static void first(char v) {// 求非终结符v关于该文法的first集
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i];
			if (s.charAt(0) == v) {// 产生式的左边是要求的非终结符
				j = 3;
				if (s.charAt(0) != s.charAt(j))// 要求的非终结符与右边第一个不同时，求first集
					addfi(s, j); // 求关于此产生式的first集
				while (or(j, s) != -1 && j < s.length())// 判断有无‘|’，有就继续求first集
				{
					j = or(j, s);// 得到‘|’位置
					if (s.charAt(0) != s.charAt(j + 1))// 要求的非终结符与右边第一个不同时，求first集
						addfi(s, j + 1);// 求关于此产生式的first集
				}
			}
		}
		First[vn(v)][Vt.length] = true;// 将fi[vn(v)][Vt.length]设为true，表示已求v的first集
	}

	static void fir() {// 求所以非终结符的first集
		int i, j;
		for (i = 0; i < Vn.length; i++) {// 非终结符的first集未求时，求该非终结符的first集
			if (!First[i][Vt.length]) {
				first(Vn[i]);
			}
		}
		System.out.println("first集");
		for (i = 0; i < Vn.length; i++) {// 输出非终结符的first集
			System.out.println(Vn[i]);
			for (j = 0; j < Vt.length; j++) {
				if (First[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	static void addfo(String s, int j) {// 求关于某一个产生式的follow集
		int i;
		// 考察字符位于产生式末尾时将产生式左边的那个字符的follow集加到考察字符的follow集中
		if (j == s.length() - 1 || (j < s.length() - 1 && s.charAt(j + 1) == '|')) {
			if (s.charAt(0) != s.charAt(j)) {// 考察字符与产生式左边的非终结符不同时
				if (!Follow[vn(s.charAt(0))][Vt.length]) {// 产生式左边的非终结符的follow集未求时，先求产生式左边的非终结符的follow集
					follow(s.charAt(0));
				}
				for (i = 0; i < Vt.length; i++) {// 产生式左边的非终结符的follow集加到考察字符的follow集中
					if (Follow[vn(s.charAt(0))][i]) {
						Follow[vn(s.charAt(j))][i] = true;
					}
				}
			}
		}
		// 将考察字符右边第一个字符的first集加到考察字符的follow集中
		if (j < s.length() - 1 && s.charAt(j + 1) != '|') {
			if (vt(s.charAt(j + 1)) != -1) {// 该字符为终结符
				if (Vt[vt(s.charAt(j + 1))] != '@')// 该字符不为@，将该字符加入考察字符的follow集中
					Follow[vn(s.charAt(j))][vt(s.charAt(j + 1))] = true;
			} else {// 该字符不为终结符
				for (i = 0; i < Vt.length; i++) {// 该字符的first集中除@的非终结符加入考察字符的follow集中
					if (First[vn(s.charAt(j + 1))][i] && Vt[i] != '@') {
						Follow[vn(s.charAt(j))][i] = true;
					}
				}
				if (vt('@') != -1) {// 非终结符中有@
					// 当考察字符右边的字符串的first集中有'@'将产生式左边的那个字符的follow集加到考察字符的follow集中
					if (s.charAt(0) == s.charAt(j))
						return;//// 考察字符与产生式左边的非终结符时同时，返回
					boolean m = true;// 当考察字符右边的字符串的first集中有'@'，m为真，没有时，m为假
					for (i = j + 1; i < s.length(); i++) {
						if (vt(s.charAt(i)) != -1) {// 当考察字符右边的字符串中有终结符，m为假
							m = false;
							break;
						}
						if (s.charAt(i) == '|') {// 遇到'|'跳出
							break;
						}
						if (!First[vn(s.charAt(i))][vt('@')]) {// 当考察字符右边的字符串中的有一非终结符的first集中不含@，m为假
							m = false;
						}
					}
					if (m) {// m为真，将产生式左边的那个字符的follow集加到考察字符的follow集中
						if (!Follow[vn(s.charAt(0))][Vt.length]) {// 产生式左边的非终结符的follow集未求时，先求产生式左边的非终结符的follow集
							follow(s.charAt(0));
						}
						for (i = 0; i < Vt.length; i++) {// 产生式左边的非终结符的follow集加到考察字符的follow集中
							if (Follow[vn(s.charAt(0))][i]) {
								Follow[vn(s.charAt(j))][i] = true;
							}
						}
					}
				}
			}
		}
	}

	static void follow(char v) {// 求非终结符v关于该文法的follow集
		
		if (v == 'E') {// v为开始符号时，将#加入v的follow集中
			Follow[vn(v)][vt('#')] = true;
		}
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i];
			for (j = 3; j < s.length(); j++) {
				if (s.charAt(j) == v) {// 产生式右边有考察字符
					addfo(s, j);// 求关于该产生式的follow集
				}
			}
		}
		Follow[vn(v)][Vt.length] = true;// 将fo[vn(v)][Vt.length]设为true，表示已求v的first集
	}

	static void fol() {// 求所以非终结符的follow集
		int i, j;
		for (i = 0; i < Vn.length; i++) {// 非终结符的follow集未求时，求该非终结符的ffollow集
			if (!Follow[i][Vt.length]) {
				follow(Vn[i]);
			}
		}
		System.out.println("follow集");
		for (i = 0; i < Vn.length; i++) {// 输出非终结符的follow集
			System.out.println(Vn[i]);
			for (j = 0; j < Vt.length; j++) {
				if (Follow[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	static void MM(int j, int i, String s, int m) {// 将某一个产生式填入预测分析表中
		char u = Grammer[i].charAt(0);
		int k;
		if (vt(Grammer[i].charAt(j)) != -1) {// Gr[i].charAt(j)为终结符
			if (Grammer[i].charAt(j) != '@')// Gr[i].charAt(j)不为@时将s加到M[vn(u)][vt(Gr[i].charAt(j))]中
				M[vn(u)][vt(Grammer[i].charAt(j))] = s;
			else {
				for (k = 0; k < Vt.length - 1; k++) {// Gr[i].charAt(j)为@，将属于s的follow集的b将s加到M[vn(u)][vt(b)]中
					if (Follow[vn(u)][k])
						M[vn(u)][k] = s;
				}
			}
		} else {// Gr[i].charAt(j)为非终结符
			for (k = 0; k < Vt.length - 1; k++) {// 对于终结符a属于Gr[i].charAt(j)的first集时，将s加到M[vn(u)][vt(a)]中
				if (First[vn(Grammer[i].charAt(j))][k])
					M[vn(u)][k] = s;
			}
			if (First[vn(Grammer[i].charAt(j))][vt('@')]) {// 当@属于Gr[i].charAt(j)的first集时，将属于s的follow集的b将s加到M[vn(u)][vt(b)]中
				if (j == m - 1) {
					for (k = 0; k < Vt.length - 1; k++) {
						if (Follow[vn(u)][k])
							M[vn(u)][k] = s;
					}
				} else {
					j = j + 1;
					MM(j, i, s, m);
				}

			}
		}
	}

	static void build_M() {// 构造预测分析表
		int i, j, m;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			j = 3;
			while (j < Grammer[i].length()) {
				m = or(j, Grammer[i]);
				if (m == -1) {
					m = Grammer[i].length();
				}
				s = Grammer[i].substring(j, m);// 将j到m-1赋值到s
				MM(j, i, s, m);
				j = m + 1;
			}

		}

	}

	static void isright(String s) {// 分析一个字符串是否符合该文法
		Stack<Character> temp = new Stack<Character>();// 分析栈
		temp.setSize(20);
		temp.push(new Character('#'));// 初始化将#入栈
		temp.push(new Character('E'));// 初始化将E入栈
		char u, v;
		int i = 0, j, k = 0;
		String m, action = "初始化", rule = "     ";
		while (i < s.length()) {
			u = s.charAt(i);
			System.out.print(k + "      ");
			for (j = 0; j < temp.size(); j++) {
				if (temp.get(j) != null)
					System.out.print(temp.get(j));// 输出分析栈内字符
			}
			System.out.print("     " + s.substring(i) + "     ");// 剩余输入串
			System.out.print("     " + rule + "     ");// 所用产生式
			System.out.println(action);// 动作
			v = temp.pop();
			action = "pop";
			if (vn(v) != -1) {// 栈顶元素为非终结符时
				if (M[vn(v)][vt(u)] != null) {// 分析表中有产生式
					m = M[vn(v)][vt(u)];
					rule = v + "->" + m;
					if (!m.equals("@")) {// 产生式不为@
						action = action + ",push(";
						for (j = m.length() - 1; j > -1; j--) {// 将产生式反序入栈
							action = action + m.charAt(j);
							temp.push(new Character(m.charAt(j)));
						}
						action = action + ")";
					}

				} else {// 分析表中没有产生式，提示错误
					rule = "     ";
					System.out.println("wrong :  " + u + "不在" + v + "对应的分析表中");
					return;
				}

			} else {// 栈顶元素为终结符时
				rule = "";
				if (v == u) {// 栈顶元素与输入符匹配
					if (v == '#') {// 栈顶元素为#时，成功
						System.out.println("accept");
						return;
					} else {
						i++;
						action = "getnext(I)";
					}
				} else {// 栈顶元素与输入符不匹配，提示错误
					System.out.println("wrong :  " + u + "与" + v + "不等");
					return;
				}

	
			
			}
			k++;
		}
	}

	public static void main(String args[]) throws IOException {

		fir();
		fol();



		String s;
		BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));
		s = sin.readLine();
		s = s.concat("#");
		System.out.println(s);
		isright(s);
		sin.close();
	}
}