import java.util.Scanner;
import java.util.Stack;
import javax.sound.midi.SysexMessage;

public class YLSyntacticAnalyzer {

	final static int MAX_RULE_NUM = 10;
	final static int MAX_VN_NUM = 10;
	final static int MAX_VT_NUM = 10;
	final static int MAX_STACK_DEPTH = 20;
	final static int MAX_P_LENGTH = 20;
	final static int MAX_ST_LENGTH = 50;


	static String[] Vn = new String[MAX_VN_NUM + 1];
	static String[] Vt = new String[MAX_VT_NUM + 1];
	static String[] Grammer = new String[MAX_RULE_NUM];
	static int vt_num, vn_num, p_num;
	// 缓冲区
	static String[] buffer = new String[MAX_P_LENGTH];
	static char ch = ' ';
	static char[] st = new char[MAX_ST_LENGTH];
	// First Follow集
	static boolean FirstSet[][] = new boolean[Vn.length][Vt.length + 1];
	static boolean FollowSet[][] = new boolean[Vn.length][Vt.length + 1];
	// 分析表
	static String[][] M = new String[MAX_VN_NUM + 1][MAX_VT_NUM + 2];
	// 预测分析表
	static String matched_str = "";
	static String stack_state = "";
	static String input_str = "";
	String action_str = "";
	static Stack<String> stack = new Stack<String>();
	static int ip = 0;

	static void Init() {
		vn_num = vt_num = p_num = 0;
		for (int i = 0; i <= MAX_VN_NUM; i++) {
			Vn[i] = "$";
		}
		for (int i = 0; i <= MAX_VT_NUM; i++) {
			Vt[i] = "$";
		}
		for (int i = 0; i < MAX_RULE_NUM; i++) {
			Grammer[i] = "$";
		}
		for (int i = 0; i <= MAX_VN_NUM; i++) {
			for (int j = 0; j <= MAX_VT_NUM + 1; j++) {
				M[i][j] = "$";
			}
		}
	}

	static int IndexCh(String ch) {
		int n;
		n = 0; /* is Vn? */
		while (!ch.equals(Vn[n]) && !"$".equals(Vn[n]))
			n++;
		if (!"$".equals(Vn[n]))
			return 100 + n;
		n = 0; /* is Vt? */
		while (!ch.equals(Vt[n]) && !"$".equals(Vt[n]))
			n++;
		if (!"$".equals(Vt[n]))
			return n;
		return -1;
	}

	/* 输出Vn或Vt的内容 */
	static void ShowChArray(String[] collect) {
		int k = 0;
		while ("$" != collect[k]) {
			if (k != 0)
				System.out.print(" ");
			System.out.print(collect[k++] + " ");
		}
		System.out.println();
	}

	// 输入非终结符
	static void InputVn() {

		Scanner in = new Scanner(System.in);
		boolean input_error = true;
		do {
			System.out.print("请输入所有的非终结符，注意:");
			System.out.println("请将开始符放在第一位，并以#号结束:");
			int n = 0;
			String str = "$";
			while (n < MAX_VN_NUM) {
				Vn[n++] = "$";
			}
			n = 0;
			while (n < MAX_VN_NUM) {
				str = in.next();
				if (str.equals("#"))
					break;
				if (-1 == IndexCh(str)) {
					Vn[n++] = str;
					vn_num++;
				}
			}
			Vn[n] = "$"; // 以“#”标志结束用于判断长度是否合法
			if (!str.equals("#")) {
				System.out.println("符号数目超过限制");
				continue;
			}

			ShowChArray(Vn);
			String option = " ";
			while (!option.equals("y") && !option.equals("n")) {
				System.out.print("输入正确确认?(y/n):");
				option = in.next();
			}
			if (str.equals("n")) {
				System.out.print("录入错误重新输入！\n");
				input_error = false;
			}
		} while (!input_error);
		// in.close();
	}

	// 输入终结符
	static void InputVt() {

		Scanner in = new Scanner(System.in);
		boolean input_error = true;
		do {
			System.out.print("请输入所有的终结符，注意:");
			System.out.println("以#号结束:");
			int n = 0;
			String str = "$";
			while (n < MAX_VT_NUM) {
				Vt[n++] = "$";
			}
			n = 0;
			while (n < MAX_VT_NUM) {
				str = in.next();
				if (str.equals("#"))
					break;
				if (-1 == IndexCh(str)) {
					Vt[n++] = str;
					vt_num++;
				}
			}
			Vt[n++] = "@";
			vt_num++;

			// Vt[n] = "$"; // 以“#”标志结束用于判断长度是否合法
			if (!str.equals("#")) {
				System.out.println("符号数目超过限制");
				continue;
			}

			ShowChArray(Vt);

			Vt[n++] = "#";
			vt_num++;
			String option = " ";
			while (!option.equals("y") && !option.equals("n")) {
				System.out.print("输入正确确认?(y/n):");
				option = in.next();
			}
			if (str.equals("n")) {
				System.out.print("录入错误重新输入！\n");
				input_error = false;
			}
		} while (!input_error);

	}

	/* 产生式输入 */
	static void InputP() {
		Scanner in = new Scanner(System.in);
		int i = 0, n, num;
		System.out.print("请输入文法产生式的个数:");
		num = in.nextInt();
		System.out.println("请输入文法的" + num + "个产生式,并以回车分隔每个产生式：");
		while (i < num) {
			System.out.print("第" + (i + 1) + "个:");
			String str = in.next();

			if (CheckP(str)) {
				Grammer[i] = str;
				i++;
			} else {
				System.out.println("输入符号含非法在成分，请重新输入!");
			}
		}
		ShowChArray(Grammer);

	}

	/* 判断产生式正确性 */
	static boolean CheckP(String st) {
		int n;
		if (100 > IndexCh(st.substring(0, 1))) {
			System.out.println(st.substring(0, 1));
			return false;
		}
		if (!"-".equals(st.substring(1, 2)))
			return false;
		if (!">".equals(st.substring(2, 3)))
			return false;
		for (n = 3; n < st.length(); n++) {
			if (st.substring(n, n + 1).equals("|") || st.substring(n, n + 1).equals("@"))
				continue;
			if (-1 == IndexCh(st.substring(n, n + 1))) {
				System.out.println(st.substring(n, n + 1) + "IndexCh:" + IndexCh(st.substring(n, n + 1)));
				return false;
			}
		}
		return true;
	}

	/* 求First集 */
	static int or(int i, String s) {// 返回最近的在其右边一个“|”位置
		for (i = i + 1; i < s.length(); i++) {
			if (s.substring(i, i + 1).equals("|"))
				return i;// 存在，就返回位置
		}
		return -1;// 返回-1表示没有“|”在其右边
	}

	static void addToFisrt(String s, int j) {// 求关于某一个产生式的first集
		// System.out.println("addToFisrt():" + s + " " + j);
		int v = IndexCh(s.substring(0, 1)) - 100;// v为产生式左边的非终结符
		int i;
		if (IndexCh(s.substring(j, j + 1)) < 100 && IndexCh(s.substring(j, j + 1)) != -1) {// 如果产生式右边第一个为终结符
			// System.out.println(v + " " + IndexCh(s.substring(j, j + 1)));
			FirstSet[v][IndexCh(s.substring(j, j + 1))] = true;// 就把s.substring(j,j+1)加入s.substring(j,j+1)的first集

		} else if (IndexCh(s.substring(j, j + 1)) != -1) {// 产生式右边第一个为非终结符

			if (!FirstSet[IndexCh(s.substring(j, j + 1)) - 100][Vt.length]) {// 如果s.substring(j,j+1)的first集没有求，先求s.substring(j,j+1)的first集
				getVnFirst(s.substring(j, j + 1));
			}

			for (i = 0; i < Vt.length; i++) {// 把s.substring(j,j+1)的first集中不为@的加入s.substring(0,1)的first集
				// System.out.println(IndexCh(s.substring(j, j + 1)));
				if (FirstSet[IndexCh(s.substring(j, j + 1)) - 100][i] && !Vt[i].equals("@")) {
					FirstSet[v][i] = true;
				}
			}
		}
	}

	static void getVnFirst(String v) {// 求非终结符v关于该文法的first集
		
		
		// System.out.println("fisrt():" + v);
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i]; // 遍历文法
			if (s.equals("$"))
				return;
			if (s.substring(0, 1).equals(v)) {// 产生式的左边是要求的非终结符
				j = 3;
				if (!s.substring(0, 1).equals(s.substring(j, j + 1))) // 要求的非终结符与右边第一个不同时，求first集，避免左递归
					addToFisrt(s, j); // 求关于此产生式的first集

				while (or(j, s) != -1 && j < s.length())// 判断有无‘|’，有就继续求first集
				{
					j = or(j, s);// 得到‘|’位置
					if (!s.substring(0,1).equals(s.substring(j,j+1))) // 要求的非终结符与右边第一个不同时，求first集，避免左递归
						addToFisrt(s, j + 1);// 求关于此产生式的first集
				}
			}
		}
		// System.out.println(IndexCh(v) + " " + Vt.length);
		FirstSet[IndexCh(v) - 100][Vt.length] = true;// 将fi[IndexCh(v)][Vt.length]设为true，表示已求v的first集
	}

	static void getFirstSet() {// 求所有非终结符的first集
		int i, j;
		for (i = 0; i < vn_num; i++) {// 非终结符的first集未求时，求该非终结符的first集
			if (!FirstSet[i][Vt.length]) {
				getVnFirst(Vn[i]);
			}
		}
		System.out.println("first集");
		for (i = 0; i < vn_num; i++) {// 输出非终结符的first集
			System.out.print(Vn[i] + ":");
			for (j = 0; j < Vt.length; j++) {
				if (FirstSet[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	/* 求Follow集 */
	static void addToFollow(String s, int j) {// 求关于某一个产生式的follow集
		int i;
		// 考察字符位于产生式末尾时将产生式左边的那个字符的follow集加到考察字符的follow集中
		// System.out.println(s.length() - 1);
		if (j == s.length() - 1 || (j < s.length() - 1 && s.substring(j + 1, j + 2).equals("|"))) {
			if (!s.substring(0, 1).equals(s.substring(j, j + 1))) {// 考察字符与产生式左边的非终结符不同时
				// System.out.println(s.substring(0, 1));
				if (!FollowSet[IndexCh(s.substring(0, 1)) - 100][Vt.length]) {// 产生式左边的非终结符的follow集未求时，先求产生式左边的非终结符的follow集
					getVnFollow(s.substring(0, 1));
				}
				for (i = 0; i < Vt.length; i++) {// 产生式左边的非终结符的follow集加到考察字符的follow集中
					if (FollowSet[IndexCh(s.substring(0, 1)) - 100][i]) {
						FollowSet[IndexCh(s.substring(j, j + 1)) - 100][i] = true;
					}
				}
			}
		}
		// 将考察字符右边第一个字符的first集加到考察字符的follow集中
		if (j < s.length() - 1 && !s.substring(j + 1, j + 2).equals("|")) {
			if (IndexCh(s.substring(j + 1, j + 2)) < 100 && IndexCh(s.substring(j + 1, j + 2)) != -1) {// 该字符为终结符
				// System.out.println(IndexCh(s.substring(j + 1, j + 2)));
				if (!Vt[IndexCh(s.substring(j + 1, j + 2))].equals("@")) {// 该字符不为@，将该字符加入考察字符的follow集中
					// System.out.println(IndexCh(s.substring(j, j + 1)) + " " +
					// IndexCh(s.substring(j + 1, j + 2)));
					FollowSet[IndexCh(s.substring(j, j + 1)) - 100][IndexCh(s.substring(j + 1, j + 2))] = true;
				}
			} else if (IndexCh(s.substring(j + 1, j + 2)) > 100 && IndexCh(s.substring(j + 1, j + 2)) != -1) {// 该字符不为终结符
				for (i = 0; i < Vt.length; i++) {// 该字符的first集中除@的非终结符加入考察字符的follow集中
					if (FirstSet[IndexCh(s.substring(j + 1, j + 2)) - 100][i] && !Vt[i].equals("@")) {
						// System.out.println(IndexCh(s.substring(j, j + 1)));
						FollowSet[IndexCh(s.substring(j, j + 1)) - 100][i] = true;
					}
				}
				if (true) {// 非终结符中有@
					// 当考察字符右边的字符串的first集中有'@'将产生式左边的那个字符的follow集加到考察字符的follow集中
					if (s.substring(0, 1).equals(s.substring(j, j + 1)))
						return;//// 考察字符与产生式左边的非终结符时同时，返回
					boolean m = true;// 当考察字符右边的字符串的first集中有'@'，m为真，没有时，m为假
					for (i = j + 1; i < s.length(); i++) {
						if (s.substring(i, i + 1).equals("$"))
							continue;
						if (IndexCh(s.substring(i, i + 1)) < 100) {// 当考察字符右边的字符串中有终结符，m为假
							// System.out.println("+");
							m = false;
							break;
						}
						if (s.substring(i, i + 1).equals("|")) {// 遇到'|'跳出
							// System.out.println("++");
							break;
						}
						// System.out.println("+++" + IndexCh(s.substring(i, i +
						// 1)));
						if (!FirstSet[IndexCh(s.substring(i, i + 1)) - 100][IndexCh("@")]) {// 当考察字符右边的字符串中的有一非终结符的first集中不含@，m为假
							// System.out.println("+++" + IndexCh(s.substring(i,
							// i + 1)));
							m = false;
						}
					}
					if (m) {// m为真，将产生式左边的那个字符的follow集加到考察字符的follow集中
						if (!FollowSet[IndexCh(s.substring(0, 1)) - 100][Vt.length]) {// 产生式左边的非终结符的follow集未求时，先求产生式左边的非终结符的follow集
							// System.out.println("先求" + s.substring(0, 1));
							getVnFollow(s.substring(0, 1));
						}
						for (i = 0; i < Vt.length; i++) {// 产生式左边的非终结符的follow集加到考察字符的follow集中
							if (FollowSet[IndexCh(s.substring(0, 1)) - 100][i]) {
								FollowSet[IndexCh(s.substring(j, j + 1)) - 100][i] = true;
							}
						}
					}
				}
			}
		}
	}

	static void getVnFollow(String v) {// 求非终结符v关于该文法的follow集
		// System.out.println("qiu" + v);
		if (v.equals("$"))
			return;
		if (v.equals(Vn[0])) {// v为开始符号时，将#加入v的follow集中
			// System.out.println(IndexCh(v) + " " + IndexCh("#"));
			FollowSet[IndexCh(v) - 100][IndexCh("#")] = true;
		}
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i];
			for (j = 3; j < s.length(); j++) {
				if (s.substring(j, j + 1).equals(v)) {// 产生式右边有考察字符
					addToFollow(s, j);// 求关于该产生式的follow集
				}
			}
		}
		// System.out.println(v + " " + IndexCh(v));
		FollowSet[IndexCh(v) - 100][Vt.length] = true;// 将fo[IndexCh(v)][Vt.length]设为true，表示已求v的first集
	}

	static void getFollowSet() {// 求所以非终结符的follow集
		int i, j;
		for (i = 0; i < vn_num; i++) {// 非终结符的follow集未求时，求该非终结符的ffollow集
			if (!FollowSet[i][Vt.length]) {
				// System.out.println(Vn[i]);
				getVnFollow(Vn[i]);
			}
		}
		System.out.println("follow集");
		for (i = 0; i < vn_num; i++) {// 输出非终结符的follow集
			System.out.print(Vn[i] + ":");
			for (j = 0; j < Vt.length; j++) {
				if (FollowSet[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	static void MM(int j, int i, String s, int m) {// 将某一个产生式填入预测分析表中
		String u = Grammer[i].substring(0, 1);
		int k;
		if (IndexCh(Grammer[i].substring(j, j + 1)) < 100 && IndexCh(Grammer[i].substring(j, j + 1)) != -1) {// Gr[i].substring(j,j+1)为终结符
			if (!Grammer[i].substring(j, j + 1).equals("@")) {// Gr[i].substring(j,j+1)不为@时将s加到M[IndexCh(u)][IndexCh(Gr[i].charAt(j))]中
				// System.out.println(IndexCh(u) + " " +
				// IndexCh(Grammer[i].substring(j, j + 1)));
				M[IndexCh(u) - 100][IndexCh(Grammer[i].substring(j, j + 1))] = s;
			} else {
				for (k = 0; k < Vt.length - 1; k++) {// Gr[i].substring(j,j+1)为@，将属于s的follow集的b将s加到M[IndexCh(u)][IndexCh(b)]中
					if (FollowSet[IndexCh(u) - 100][k])
						M[IndexCh(u) - 100][k] = s;
				}
			}
		} else {// Gr[i].substring(为非终结符
			for (k = 0; k < Vt.length - 1; k++) {// 对于终结符a属于Gr[i].substring(j,j+1)的first集时，将s加到M[vn(u)][IndexCh(a)]中
				if (FirstSet[IndexCh(Grammer[i].substring(j, j + 1)) - 100][k])
					M[IndexCh(u) - 100][k] = s;
			}
			if (FirstSet[IndexCh(Grammer[i].substring(j, j + 1)) - 100][IndexCh("@")]) {// 当@属于Gr[i].substring(j,j+1)的first集时，将属于s的follow集的b将s加到M[vn(u)][IndexCh(b)]中
				if (j == m - 1) {
					for (k = 0; k < Vt.length - 1; k++) {
						if (FollowSet[IndexCh(u) - 100][k])
							M[IndexCh(u)][k] = s;
					}
				} else {
					j = j + 1;
					MM(j, i, s, m);
				}
			}
		}
	}

	static void build_M() {// 构造预测分析表
		String s;
		for (int i = 0; i < Grammer.length; i++) {
			int j = 3;
			while (j < Grammer[i].length()) {
				int m = or(j, Grammer[i]);
				if (m == -1) {
					m = Grammer[i].length();
				}
				s = Grammer[i].substring(j, m);// 将j到m-1赋值到s
				MM(j, i, s, m);
				j = m + 1;
			}

		}
		for (int i = 0; i < vn_num; i++) {
			for (int j = 0; j < vt_num - 1; j++) {
				System.out.print(M[i][j] + "\t");
			}
			System.out.println();
		}
	}

	public static void pridictAnalyse() {
		Scanner in = new Scanner(System.in);
		System.out.println("请输入要分析的串：");
		//input_str = in.next() + "#";
		 input_str = "i+i*i)#";
		stack.push("$");
		stack.push(Vn[0]);
		stack_state = Vn[0] + "#";
		System.out.println(matched_str + "\t" + stack_state + "\t" + input_str + "\t");
		while (!stack.peek().equals("$")) {
			if (stack.peek().equals(input_str.substring(ip, ip + 1))) {
				stack.pop();
				String temp_input_str = input_str.substring(ip, ip + 1);
				matched_str += temp_input_str;
				ip++;
				stack_state = stack_state.substring(1);
				System.out.print(matched_str + "\t" + stack_state + "\t" + input_str.substring(ip) + "\t");
				System.out.println("匹配" + temp_input_str);
				continue;
			} else if (IndexCh(stack.peek()) < 100) {
				System.out.println("error1");
				break;
			} else if (M[IndexCh(stack.peek()) - 100][IndexCh(input_str.substring(ip, ip + 1))].equals("$")) {
				System.out.println(IndexCh(stack.peek()) - 100 + " " + input_str.substring(ip, ip + 1) + " error2");
				break;
			} else if (!M[IndexCh(stack.peek()) - 100][IndexCh(input_str.substring(ip, ip + 1))].equals("$")) {
				String back_str = M[IndexCh(stack.peek()) - 100][IndexCh(input_str.substring(ip, ip + 1))];
				int grammer_length = M[IndexCh(stack.peek()) - 100][IndexCh(input_str.substring(ip, ip + 1))].length();
				// System.out.println(M[IndexCh(stack.peek()) -
				// 100][IndexCh(input_str.substring(ip, ip + 1))]+"
				// length:"+M[IndexCh(stack.peek()) -
				// 100][IndexCh(input_str.substring(ip, ip + 1))].length());
				String temp_str = M[IndexCh(stack.peek()) - 100][IndexCh(input_str.substring(ip, ip + 1))];
				String action_str = "输出 " + Vn[IndexCh(stack.peek()) - 100] + "->"
						+ M[IndexCh(stack.peek()) - 100][IndexCh(input_str.substring(ip, ip + 1))];
				stack.pop();
				stack_state = stack_state.substring(1);
				if (!back_str.equals("@")) {
					for (int i = grammer_length - 1; i >= 0; i--) {
						// iSystem.out.println(temp_str);
						String temp = temp_str.substring(i, i + 1);
						stack.push(temp);
						stack_state = temp + stack_state;
					}
				}
				System.out.print(matched_str + "\t" + stack_state + "\t" + input_str.substring(ip) + "\t");
				// System.out.println(IndexCh(stack.peek())+"
				// "+IndexCh(input_str.substring(ip, ip + 1)));
				System.out.println(action_str);
			}
		}
		System.out.println(input_str.substring(ip,ip+1)+' '+stack_state);
		if(!input_str.substring(ip,ip+1).equals("#")&&stack_state.equals("#")){
//			System.out.println(input_str.substring(ip,ip+1)+' '+stack_state);
//			System.out.print(matched_str + "\t" + stack_state + "\t" + input_str.substring(ip) + "\t");
			System.out.println("栈空，输入串无法匹配！错误！");
		}

	}

	public static void main(String[] args) {

		Init();
		// System.out.println("what");
		// InputVn();
		// InputVt();
		// InputP();

		 Vn[0] = "E";
		 Vn[1] = "T";
		 Vn[2] = "G";
		 Vn[3] = "S";
		 Vn[4] = "F";
		 vn_num = 5;
		
		 Vt[0] = "i";
		 Vt[1] = "+";
		 Vt[2] = "-";
		 Vt[3] = "*";
		 Vt[4] = "/";
		 Vt[5] = "(";
		 Vt[6] = ")";
		 Vt[7] = "#";
		 Vt[8] = "@";
		 vt_num = 9;
		
		 Grammer[0] = "E->TG";
		 Grammer[1] = "G->+TG|-TG";
		 Grammer[2] = "G->@";
		 Grammer[3] = "T->FS";
		 Grammer[4] = "S->*FS|/FS";
		 Grammer[5] = "S->@";
		 Grammer[6] = "F->(E)";
		 Grammer[7] = "F->i";

//		Vn[0] = "E";
//		Vn[1] = "T";
//		Vn[2] = "Q";
//		Vn[3] = "F";
//		Vn[4] = "P";
//		vn_num = 5;
//
//		Vt[0] = "i";
//		Vt[1] = "+";
//		Vt[2] = "*";
//		Vt[3] = "(";
//		Vt[4] = ")";
//		Vt[5] = "@";
//		Vt[6] = "#";
//		vt_num = 6;
//
//		Grammer[0] = "E->TQ";
//		Grammer[1] = "Q->+TQ|@";
//		Grammer[2] = "T->FP";
//		Grammer[3] = "P->*FP|@";
//		Grammer[4] = "F->(E)|i";

		// Vn[0] = "E";
		// Vn[1] = "e";
		// Vn[2] = "T";
		// Vn[3] = "t";
		// Vn[4] = "F";
		// vn_num = 5;
		//
		// Vt[0] = "i";
		// Vt[1] = "+";
		// Vt[2] = "*";
		// Vt[3] = "(";
		// Vt[4] = ")";
		// Vt[5] = "#";
		// Vt[6] = "@";
		// vt_num = 7;
		//
		// Grammer[0] = "E->Te";
		// Grammer[1] = "e->+Te|@";
		// Grammer[2] = "T->Ft";
		// Grammer[3] = "t->*Ft|@";
		// Grammer[4] = "F->(E)|i";

		getFirstSet();
		getFollowSet();
		System.out.println("预测分析表");
		build_M();
		pridictAnalyse();
	}

}
