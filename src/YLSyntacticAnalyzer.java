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
	// ������
	static String[] buffer = new String[MAX_P_LENGTH];
	static char ch = ' ';
	static char[] st = new char[MAX_ST_LENGTH];
	// First Follow��
	static boolean FirstSet[][] = new boolean[Vn.length][Vt.length + 1];
	static boolean FollowSet[][] = new boolean[Vn.length][Vt.length + 1];
	// ������
	static String[][] M = new String[MAX_VN_NUM + 1][MAX_VT_NUM + 2];
	// Ԥ�������
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

	/* ���Vn��Vt������ */
	static void ShowChArray(String[] collect) {
		int k = 0;
		while ("$" != collect[k]) {
			if (k != 0)
				System.out.print(" ");
			System.out.print(collect[k++] + " ");
		}
		System.out.println();
	}

	// ������ս��
	static void InputVn() {

		Scanner in = new Scanner(System.in);
		boolean input_error = true;
		do {
			System.out.print("���������еķ��ս����ע��:");
			System.out.println("�뽫��ʼ�����ڵ�һλ������#�Ž���:");
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
			Vn[n] = "$"; // �ԡ�#����־���������жϳ����Ƿ�Ϸ�
			if (!str.equals("#")) {
				System.out.println("������Ŀ��������");
				continue;
			}

			ShowChArray(Vn);
			String option = " ";
			while (!option.equals("y") && !option.equals("n")) {
				System.out.print("������ȷȷ��?(y/n):");
				option = in.next();
			}
			if (str.equals("n")) {
				System.out.print("¼������������룡\n");
				input_error = false;
			}
		} while (!input_error);
		// in.close();
	}

	// �����ս��
	static void InputVt() {

		Scanner in = new Scanner(System.in);
		boolean input_error = true;
		do {
			System.out.print("���������е��ս����ע��:");
			System.out.println("��#�Ž���:");
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

			// Vt[n] = "$"; // �ԡ�#����־���������жϳ����Ƿ�Ϸ�
			if (!str.equals("#")) {
				System.out.println("������Ŀ��������");
				continue;
			}

			ShowChArray(Vt);

			Vt[n++] = "#";
			vt_num++;
			String option = " ";
			while (!option.equals("y") && !option.equals("n")) {
				System.out.print("������ȷȷ��?(y/n):");
				option = in.next();
			}
			if (str.equals("n")) {
				System.out.print("¼������������룡\n");
				input_error = false;
			}
		} while (!input_error);

	}

	/* ����ʽ���� */
	static void InputP() {
		Scanner in = new Scanner(System.in);
		int i = 0, n, num;
		System.out.print("�������ķ�����ʽ�ĸ���:");
		num = in.nextInt();
		System.out.println("�������ķ���" + num + "������ʽ,���Իس��ָ�ÿ������ʽ��");
		while (i < num) {
			System.out.print("��" + (i + 1) + "��:");
			String str = in.next();

			if (CheckP(str)) {
				Grammer[i] = str;
				i++;
			} else {
				System.out.println("������ź��Ƿ��ڳɷ֣�����������!");
			}
		}
		ShowChArray(Grammer);

	}

	/* �жϲ���ʽ��ȷ�� */
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

	/* ��First�� */
	static int or(int i, String s) {// ��������������ұ�һ����|��λ��
		for (i = i + 1; i < s.length(); i++) {
			if (s.substring(i, i + 1).equals("|"))
				return i;// ���ڣ��ͷ���λ��
		}
		return -1;// ����-1��ʾû�С�|�������ұ�
	}

	static void addToFisrt(String s, int j) {// �����ĳһ������ʽ��first��
		// System.out.println("addToFisrt():" + s + " " + j);
		int v = IndexCh(s.substring(0, 1)) - 100;// vΪ����ʽ��ߵķ��ս��
		int i;
		if (IndexCh(s.substring(j, j + 1)) < 100 && IndexCh(s.substring(j, j + 1)) != -1) {// �������ʽ�ұߵ�һ��Ϊ�ս��
			// System.out.println(v + " " + IndexCh(s.substring(j, j + 1)));
			FirstSet[v][IndexCh(s.substring(j, j + 1))] = true;// �Ͱ�s.substring(j,j+1)����s.substring(j,j+1)��first��

		} else if (IndexCh(s.substring(j, j + 1)) != -1) {// ����ʽ�ұߵ�һ��Ϊ���ս��

			if (!FirstSet[IndexCh(s.substring(j, j + 1)) - 100][Vt.length]) {// ���s.substring(j,j+1)��first��û��������s.substring(j,j+1)��first��
				getVnFirst(s.substring(j, j + 1));
			}

			for (i = 0; i < Vt.length; i++) {// ��s.substring(j,j+1)��first���в�Ϊ@�ļ���s.substring(0,1)��first��
				// System.out.println(IndexCh(s.substring(j, j + 1)));
				if (FirstSet[IndexCh(s.substring(j, j + 1)) - 100][i] && !Vt[i].equals("@")) {
					FirstSet[v][i] = true;
				}
			}
		}
	}

	static void getVnFirst(String v) {// ����ս��v���ڸ��ķ���first��
		
		
		// System.out.println("fisrt():" + v);
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i]; // �����ķ�
			if (s.equals("$"))
				return;
			if (s.substring(0, 1).equals(v)) {// ����ʽ�������Ҫ��ķ��ս��
				j = 3;
				if (!s.substring(0, 1).equals(s.substring(j, j + 1))) // Ҫ��ķ��ս�����ұߵ�һ����ͬʱ����first����������ݹ�
					addToFisrt(s, j); // ����ڴ˲���ʽ��first��

				while (or(j, s) != -1 && j < s.length())// �ж����ޡ�|�����оͼ�����first��
				{
					j = or(j, s);// �õ���|��λ��
					if (!s.substring(0,1).equals(s.substring(j,j+1))) // Ҫ��ķ��ս�����ұߵ�һ����ͬʱ����first����������ݹ�
						addToFisrt(s, j + 1);// ����ڴ˲���ʽ��first��
				}
			}
		}
		// System.out.println(IndexCh(v) + " " + Vt.length);
		FirstSet[IndexCh(v) - 100][Vt.length] = true;// ��fi[IndexCh(v)][Vt.length]��Ϊtrue����ʾ����v��first��
	}

	static void getFirstSet() {// �����з��ս����first��
		int i, j;
		for (i = 0; i < vn_num; i++) {// ���ս����first��δ��ʱ����÷��ս����first��
			if (!FirstSet[i][Vt.length]) {
				getVnFirst(Vn[i]);
			}
		}
		System.out.println("first��");
		for (i = 0; i < vn_num; i++) {// ������ս����first��
			System.out.print(Vn[i] + ":");
			for (j = 0; j < Vt.length; j++) {
				if (FirstSet[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	/* ��Follow�� */
	static void addToFollow(String s, int j) {// �����ĳһ������ʽ��follow��
		int i;
		// �����ַ�λ�ڲ���ʽĩβʱ������ʽ��ߵ��Ǹ��ַ���follow���ӵ������ַ���follow����
		// System.out.println(s.length() - 1);
		if (j == s.length() - 1 || (j < s.length() - 1 && s.substring(j + 1, j + 2).equals("|"))) {
			if (!s.substring(0, 1).equals(s.substring(j, j + 1))) {// �����ַ������ʽ��ߵķ��ս����ͬʱ
				// System.out.println(s.substring(0, 1));
				if (!FollowSet[IndexCh(s.substring(0, 1)) - 100][Vt.length]) {// ����ʽ��ߵķ��ս����follow��δ��ʱ���������ʽ��ߵķ��ս����follow��
					getVnFollow(s.substring(0, 1));
				}
				for (i = 0; i < Vt.length; i++) {// ����ʽ��ߵķ��ս����follow���ӵ������ַ���follow����
					if (FollowSet[IndexCh(s.substring(0, 1)) - 100][i]) {
						FollowSet[IndexCh(s.substring(j, j + 1)) - 100][i] = true;
					}
				}
			}
		}
		// �������ַ��ұߵ�һ���ַ���first���ӵ������ַ���follow����
		if (j < s.length() - 1 && !s.substring(j + 1, j + 2).equals("|")) {
			if (IndexCh(s.substring(j + 1, j + 2)) < 100 && IndexCh(s.substring(j + 1, j + 2)) != -1) {// ���ַ�Ϊ�ս��
				// System.out.println(IndexCh(s.substring(j + 1, j + 2)));
				if (!Vt[IndexCh(s.substring(j + 1, j + 2))].equals("@")) {// ���ַ���Ϊ@�������ַ����뿼���ַ���follow����
					// System.out.println(IndexCh(s.substring(j, j + 1)) + " " +
					// IndexCh(s.substring(j + 1, j + 2)));
					FollowSet[IndexCh(s.substring(j, j + 1)) - 100][IndexCh(s.substring(j + 1, j + 2))] = true;
				}
			} else if (IndexCh(s.substring(j + 1, j + 2)) > 100 && IndexCh(s.substring(j + 1, j + 2)) != -1) {// ���ַ���Ϊ�ս��
				for (i = 0; i < Vt.length; i++) {// ���ַ���first���г�@�ķ��ս�����뿼���ַ���follow����
					if (FirstSet[IndexCh(s.substring(j + 1, j + 2)) - 100][i] && !Vt[i].equals("@")) {
						// System.out.println(IndexCh(s.substring(j, j + 1)));
						FollowSet[IndexCh(s.substring(j, j + 1)) - 100][i] = true;
					}
				}
				if (true) {// ���ս������@
					// �������ַ��ұߵ��ַ�����first������'@'������ʽ��ߵ��Ǹ��ַ���follow���ӵ������ַ���follow����
					if (s.substring(0, 1).equals(s.substring(j, j + 1)))
						return;//// �����ַ������ʽ��ߵķ��ս��ʱͬʱ������
					boolean m = true;// �������ַ��ұߵ��ַ�����first������'@'��mΪ�棬û��ʱ��mΪ��
					for (i = j + 1; i < s.length(); i++) {
						if (s.substring(i, i + 1).equals("$"))
							continue;
						if (IndexCh(s.substring(i, i + 1)) < 100) {// �������ַ��ұߵ��ַ��������ս����mΪ��
							// System.out.println("+");
							m = false;
							break;
						}
						if (s.substring(i, i + 1).equals("|")) {// ����'|'����
							// System.out.println("++");
							break;
						}
						// System.out.println("+++" + IndexCh(s.substring(i, i +
						// 1)));
						if (!FirstSet[IndexCh(s.substring(i, i + 1)) - 100][IndexCh("@")]) {// �������ַ��ұߵ��ַ����е���һ���ս����first���в���@��mΪ��
							// System.out.println("+++" + IndexCh(s.substring(i,
							// i + 1)));
							m = false;
						}
					}
					if (m) {// mΪ�棬������ʽ��ߵ��Ǹ��ַ���follow���ӵ������ַ���follow����
						if (!FollowSet[IndexCh(s.substring(0, 1)) - 100][Vt.length]) {// ����ʽ��ߵķ��ս����follow��δ��ʱ���������ʽ��ߵķ��ս����follow��
							// System.out.println("����" + s.substring(0, 1));
							getVnFollow(s.substring(0, 1));
						}
						for (i = 0; i < Vt.length; i++) {// ����ʽ��ߵķ��ս����follow���ӵ������ַ���follow����
							if (FollowSet[IndexCh(s.substring(0, 1)) - 100][i]) {
								FollowSet[IndexCh(s.substring(j, j + 1)) - 100][i] = true;
							}
						}
					}
				}
			}
		}
	}

	static void getVnFollow(String v) {// ����ս��v���ڸ��ķ���follow��
		// System.out.println("qiu" + v);
		if (v.equals("$"))
			return;
		if (v.equals(Vn[0])) {// vΪ��ʼ����ʱ����#����v��follow����
			// System.out.println(IndexCh(v) + " " + IndexCh("#"));
			FollowSet[IndexCh(v) - 100][IndexCh("#")] = true;
		}
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i];
			for (j = 3; j < s.length(); j++) {
				if (s.substring(j, j + 1).equals(v)) {// ����ʽ�ұ��п����ַ�
					addToFollow(s, j);// ����ڸò���ʽ��follow��
				}
			}
		}
		// System.out.println(v + " " + IndexCh(v));
		FollowSet[IndexCh(v) - 100][Vt.length] = true;// ��fo[IndexCh(v)][Vt.length]��Ϊtrue����ʾ����v��first��
	}

	static void getFollowSet() {// �����Է��ս����follow��
		int i, j;
		for (i = 0; i < vn_num; i++) {// ���ս����follow��δ��ʱ����÷��ս����ffollow��
			if (!FollowSet[i][Vt.length]) {
				// System.out.println(Vn[i]);
				getVnFollow(Vn[i]);
			}
		}
		System.out.println("follow��");
		for (i = 0; i < vn_num; i++) {// ������ս����follow��
			System.out.print(Vn[i] + ":");
			for (j = 0; j < Vt.length; j++) {
				if (FollowSet[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	static void MM(int j, int i, String s, int m) {// ��ĳһ������ʽ����Ԥ���������
		String u = Grammer[i].substring(0, 1);
		int k;
		if (IndexCh(Grammer[i].substring(j, j + 1)) < 100 && IndexCh(Grammer[i].substring(j, j + 1)) != -1) {// Gr[i].substring(j,j+1)Ϊ�ս��
			if (!Grammer[i].substring(j, j + 1).equals("@")) {// Gr[i].substring(j,j+1)��Ϊ@ʱ��s�ӵ�M[IndexCh(u)][IndexCh(Gr[i].charAt(j))]��
				// System.out.println(IndexCh(u) + " " +
				// IndexCh(Grammer[i].substring(j, j + 1)));
				M[IndexCh(u) - 100][IndexCh(Grammer[i].substring(j, j + 1))] = s;
			} else {
				for (k = 0; k < Vt.length - 1; k++) {// Gr[i].substring(j,j+1)Ϊ@��������s��follow����b��s�ӵ�M[IndexCh(u)][IndexCh(b)]��
					if (FollowSet[IndexCh(u) - 100][k])
						M[IndexCh(u) - 100][k] = s;
				}
			}
		} else {// Gr[i].substring(Ϊ���ս��
			for (k = 0; k < Vt.length - 1; k++) {// �����ս��a����Gr[i].substring(j,j+1)��first��ʱ����s�ӵ�M[vn(u)][IndexCh(a)]��
				if (FirstSet[IndexCh(Grammer[i].substring(j, j + 1)) - 100][k])
					M[IndexCh(u) - 100][k] = s;
			}
			if (FirstSet[IndexCh(Grammer[i].substring(j, j + 1)) - 100][IndexCh("@")]) {// ��@����Gr[i].substring(j,j+1)��first��ʱ��������s��follow����b��s�ӵ�M[vn(u)][IndexCh(b)]��
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

	static void build_M() {// ����Ԥ�������
		String s;
		for (int i = 0; i < Grammer.length; i++) {
			int j = 3;
			while (j < Grammer[i].length()) {
				int m = or(j, Grammer[i]);
				if (m == -1) {
					m = Grammer[i].length();
				}
				s = Grammer[i].substring(j, m);// ��j��m-1��ֵ��s
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
		System.out.println("������Ҫ�����Ĵ���");
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
				System.out.println("ƥ��" + temp_input_str);
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
				String action_str = "��� " + Vn[IndexCh(stack.peek()) - 100] + "->"
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
			System.out.println("ջ�գ����봮�޷�ƥ�䣡����");
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
		System.out.println("Ԥ�������");
		build_M();
		pridictAnalyse();
	}

}
