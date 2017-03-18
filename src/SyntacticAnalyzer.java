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

	static int or(int i, String s) {// ��������������ұ�һ����|��λ��
		for (i = i + 1; i < s.length(); i++) {
			if (s.charAt(i) == '|')
				return i;// ���ڣ��ͷ���λ��
		}
		return -1;// ����-1��ʾû�С�|�������ұ�
	}

	static int vn(char c) {// ����c�ڷ��ս�����е�λ��
		int i;
		for (i = 0; i < Vn.length; i++) {
			if (c == Vn[i])
				return i;// �ڱ��У��ͷ���λ��
		}
		return -1;// ����-1��ʾ���ڱ���
	}

	static int vt(char c) {// ����c���ս�����е�λ��
		int i;
		for (i = 0; i < Vt.length; i++) {
			if (c == Vt[i])
				return i;// �ڱ��У��ͷ���λ��
		}
		return -1;// ����-1��ʾ���ڱ���
	}

	static void addfi(String s, int j) {// �����ĳһ������ʽ��first��
		int v = vn(s.charAt(0));// vΪ����ʽ��ߵķ��ս��
		int i;
		if (vt(s.charAt(j)) != -1) {// ����ʽ�ұߵ�һ��Ϊ�ս��
			First[v][vt(s.charAt(j))] = true;// �Ͱ�s.charAt(j)����s.charAt(0)��first��
		} else {// ����ʽ�ұߵ�һ��Ϊ���ս��
			if (!First[vn(s.charAt(j))][Vt.length]) {// ���s.charAt(j)��first��û��������s.charAt(j)��first��
				first(s.charAt(j));
			}
			for (i = 0; i < Vt.length; i++) {// ��s.charAt(j)��first���в�Ϊ@�ļ���s.charAt(0)��first��
				if (First[vn(s.charAt(j))][i] && Vt[i] != '@') {
					First[v][i] = true;
				}
			}
			if (vt('@') != -1)// �ս������@
				if (First[vn(s.charAt(j))][vt('@')]) {// ���@���ڵ�ǰs.charAt(j)��first��
					if (j == s.length() - 1) {// j=s.length()-1�ͽ�@����s.charAt(0)��first��
						First[v][vt('@')] = true;
						return;
					}
					if (s.charAt(j + 1) != '|') {// s.charAt(j+1)���ǡ�|���ͽ�s.charAt(j+1)��first������s.charAt(0)��first��
						j++;
						addfi(s, j);
					} else {// s.charAt(j+1)�ǡ�|���ͽ�@����s.charAt(0)��first��
						First[v][vt('@')] = true;
					}
				}
		}
	}

	static void first(char v) {// ����ս��v���ڸ��ķ���first��
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i];
			if (s.charAt(0) == v) {// ����ʽ�������Ҫ��ķ��ս��
				j = 3;
				if (s.charAt(0) != s.charAt(j))// Ҫ��ķ��ս�����ұߵ�һ����ͬʱ����first��
					addfi(s, j); // ����ڴ˲���ʽ��first��
				while (or(j, s) != -1 && j < s.length())// �ж����ޡ�|�����оͼ�����first��
				{
					j = or(j, s);// �õ���|��λ��
					if (s.charAt(0) != s.charAt(j + 1))// Ҫ��ķ��ս�����ұߵ�һ����ͬʱ����first��
						addfi(s, j + 1);// ����ڴ˲���ʽ��first��
				}
			}
		}
		First[vn(v)][Vt.length] = true;// ��fi[vn(v)][Vt.length]��Ϊtrue����ʾ����v��first��
	}

	static void fir() {// �����Է��ս����first��
		int i, j;
		for (i = 0; i < Vn.length; i++) {// ���ս����first��δ��ʱ����÷��ս����first��
			if (!First[i][Vt.length]) {
				first(Vn[i]);
			}
		}
		System.out.println("first��");
		for (i = 0; i < Vn.length; i++) {// ������ս����first��
			System.out.println(Vn[i]);
			for (j = 0; j < Vt.length; j++) {
				if (First[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	static void addfo(String s, int j) {// �����ĳһ������ʽ��follow��
		int i;
		// �����ַ�λ�ڲ���ʽĩβʱ������ʽ��ߵ��Ǹ��ַ���follow���ӵ������ַ���follow����
		if (j == s.length() - 1 || (j < s.length() - 1 && s.charAt(j + 1) == '|')) {
			if (s.charAt(0) != s.charAt(j)) {// �����ַ������ʽ��ߵķ��ս����ͬʱ
				if (!Follow[vn(s.charAt(0))][Vt.length]) {// ����ʽ��ߵķ��ս����follow��δ��ʱ���������ʽ��ߵķ��ս����follow��
					follow(s.charAt(0));
				}
				for (i = 0; i < Vt.length; i++) {// ����ʽ��ߵķ��ս����follow���ӵ������ַ���follow����
					if (Follow[vn(s.charAt(0))][i]) {
						Follow[vn(s.charAt(j))][i] = true;
					}
				}
			}
		}
		// �������ַ��ұߵ�һ���ַ���first���ӵ������ַ���follow����
		if (j < s.length() - 1 && s.charAt(j + 1) != '|') {
			if (vt(s.charAt(j + 1)) != -1) {// ���ַ�Ϊ�ս��
				if (Vt[vt(s.charAt(j + 1))] != '@')// ���ַ���Ϊ@�������ַ����뿼���ַ���follow����
					Follow[vn(s.charAt(j))][vt(s.charAt(j + 1))] = true;
			} else {// ���ַ���Ϊ�ս��
				for (i = 0; i < Vt.length; i++) {// ���ַ���first���г�@�ķ��ս�����뿼���ַ���follow����
					if (First[vn(s.charAt(j + 1))][i] && Vt[i] != '@') {
						Follow[vn(s.charAt(j))][i] = true;
					}
				}
				if (vt('@') != -1) {// ���ս������@
					// �������ַ��ұߵ��ַ�����first������'@'������ʽ��ߵ��Ǹ��ַ���follow���ӵ������ַ���follow����
					if (s.charAt(0) == s.charAt(j))
						return;//// �����ַ������ʽ��ߵķ��ս��ʱͬʱ������
					boolean m = true;// �������ַ��ұߵ��ַ�����first������'@'��mΪ�棬û��ʱ��mΪ��
					for (i = j + 1; i < s.length(); i++) {
						if (vt(s.charAt(i)) != -1) {// �������ַ��ұߵ��ַ��������ս����mΪ��
							m = false;
							break;
						}
						if (s.charAt(i) == '|') {// ����'|'����
							break;
						}
						if (!First[vn(s.charAt(i))][vt('@')]) {// �������ַ��ұߵ��ַ����е���һ���ս����first���в���@��mΪ��
							m = false;
						}
					}
					if (m) {// mΪ�棬������ʽ��ߵ��Ǹ��ַ���follow���ӵ������ַ���follow����
						if (!Follow[vn(s.charAt(0))][Vt.length]) {// ����ʽ��ߵķ��ս����follow��δ��ʱ���������ʽ��ߵķ��ս����follow��
							follow(s.charAt(0));
						}
						for (i = 0; i < Vt.length; i++) {// ����ʽ��ߵķ��ս����follow���ӵ������ַ���follow����
							if (Follow[vn(s.charAt(0))][i]) {
								Follow[vn(s.charAt(j))][i] = true;
							}
						}
					}
				}
			}
		}
	}

	static void follow(char v) {// ����ս��v���ڸ��ķ���follow��
		
		if (v == 'E') {// vΪ��ʼ����ʱ����#����v��follow����
			Follow[vn(v)][vt('#')] = true;
		}
		int i, j;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			s = Grammer[i];
			for (j = 3; j < s.length(); j++) {
				if (s.charAt(j) == v) {// ����ʽ�ұ��п����ַ�
					addfo(s, j);// ����ڸò���ʽ��follow��
				}
			}
		}
		Follow[vn(v)][Vt.length] = true;// ��fo[vn(v)][Vt.length]��Ϊtrue����ʾ����v��first��
	}

	static void fol() {// �����Է��ս����follow��
		int i, j;
		for (i = 0; i < Vn.length; i++) {// ���ս����follow��δ��ʱ����÷��ս����ffollow��
			if (!Follow[i][Vt.length]) {
				follow(Vn[i]);
			}
		}
		System.out.println("follow��");
		for (i = 0; i < Vn.length; i++) {// ������ս����follow��
			System.out.println(Vn[i]);
			for (j = 0; j < Vt.length; j++) {
				if (Follow[i][j]) {
					System.out.print("   " + Vt[j]);
				}
			}
			System.out.println();
		}
	}

	static void MM(int j, int i, String s, int m) {// ��ĳһ������ʽ����Ԥ���������
		char u = Grammer[i].charAt(0);
		int k;
		if (vt(Grammer[i].charAt(j)) != -1) {// Gr[i].charAt(j)Ϊ�ս��
			if (Grammer[i].charAt(j) != '@')// Gr[i].charAt(j)��Ϊ@ʱ��s�ӵ�M[vn(u)][vt(Gr[i].charAt(j))]��
				M[vn(u)][vt(Grammer[i].charAt(j))] = s;
			else {
				for (k = 0; k < Vt.length - 1; k++) {// Gr[i].charAt(j)Ϊ@��������s��follow����b��s�ӵ�M[vn(u)][vt(b)]��
					if (Follow[vn(u)][k])
						M[vn(u)][k] = s;
				}
			}
		} else {// Gr[i].charAt(j)Ϊ���ս��
			for (k = 0; k < Vt.length - 1; k++) {// �����ս��a����Gr[i].charAt(j)��first��ʱ����s�ӵ�M[vn(u)][vt(a)]��
				if (First[vn(Grammer[i].charAt(j))][k])
					M[vn(u)][k] = s;
			}
			if (First[vn(Grammer[i].charAt(j))][vt('@')]) {// ��@����Gr[i].charAt(j)��first��ʱ��������s��follow����b��s�ӵ�M[vn(u)][vt(b)]��
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

	static void build_M() {// ����Ԥ�������
		int i, j, m;
		String s;
		for (i = 0; i < Grammer.length; i++) {
			j = 3;
			while (j < Grammer[i].length()) {
				m = or(j, Grammer[i]);
				if (m == -1) {
					m = Grammer[i].length();
				}
				s = Grammer[i].substring(j, m);// ��j��m-1��ֵ��s
				MM(j, i, s, m);
				j = m + 1;
			}

		}

	}

	static void isright(String s) {// ����һ���ַ����Ƿ���ϸ��ķ�
		Stack<Character> temp = new Stack<Character>();// ����ջ
		temp.setSize(20);
		temp.push(new Character('#'));// ��ʼ����#��ջ
		temp.push(new Character('E'));// ��ʼ����E��ջ
		char u, v;
		int i = 0, j, k = 0;
		String m, action = "��ʼ��", rule = "     ";
		while (i < s.length()) {
			u = s.charAt(i);
			System.out.print(k + "      ");
			for (j = 0; j < temp.size(); j++) {
				if (temp.get(j) != null)
					System.out.print(temp.get(j));// �������ջ���ַ�
			}
			System.out.print("     " + s.substring(i) + "     ");// ʣ�����봮
			System.out.print("     " + rule + "     ");// ���ò���ʽ
			System.out.println(action);// ����
			v = temp.pop();
			action = "pop";
			if (vn(v) != -1) {// ջ��Ԫ��Ϊ���ս��ʱ
				if (M[vn(v)][vt(u)] != null) {// ���������в���ʽ
					m = M[vn(v)][vt(u)];
					rule = v + "->" + m;
					if (!m.equals("@")) {// ����ʽ��Ϊ@
						action = action + ",push(";
						for (j = m.length() - 1; j > -1; j--) {// ������ʽ������ջ
							action = action + m.charAt(j);
							temp.push(new Character(m.charAt(j)));
						}
						action = action + ")";
					}

				} else {// ��������û�в���ʽ����ʾ����
					rule = "     ";
					System.out.println("wrong :  " + u + "����" + v + "��Ӧ�ķ�������");
					return;
				}

			} else {// ջ��Ԫ��Ϊ�ս��ʱ
				rule = "";
				if (v == u) {// ջ��Ԫ���������ƥ��
					if (v == '#') {// ջ��Ԫ��Ϊ#ʱ���ɹ�
						System.out.println("accept");
						return;
					} else {
						i++;
						action = "getnext(I)";
					}
				} else {// ջ��Ԫ�����������ƥ�䣬��ʾ����
					System.out.println("wrong :  " + u + "��" + v + "����");
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