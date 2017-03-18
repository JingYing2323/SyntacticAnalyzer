import java.awt.TexturePaint;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class LL {
	
	//保存vn
	static Set<String> temp_vn_set = new HashSet<String>();
	//保存vt
	static Set<String> temp_vt_set = new HashSet<String>();
	//保存文法
	static Set<String> result_grammer = new HashSet<String>();
	static int vt_num, vn_num, p_num;
	static String begin_Vn;
	// First Follow集
	static Set<String> isFirst = new HashSet<String>();
	static HashMap<String, Set<String>> FirstSet = new HashMap<>();
	static Set<String> isFollow = new HashSet<String>();
	static HashMap<String, Set<String>> FollowSet = new HashMap<>();

	// 预测分析表
	static String[] Vn;
	static String[] Vt;
	static String[][] M;
	static String matched_str = "";
	static String stack_state = "";
	static String input_str = "";
	String action_str = "";
	static Stack<String> stack = new Stack<String>();
	static int ip = 0;

	static void Init() {
		vn_num = vt_num = p_num = 0;
	}

	//返回符号的位置
	static int IndexCh(String ch) {

		for (int n = 0; n < Vn.length; n++) {
			if (ch.equals(Vn[n]))
				return 100 + n;
		}
		for (int n = 0; n < Vt.length; n++) {
			// System.out.println(ch+" "+Vt[n]);
			if (ch.equals(Vt[n]))
				return n;
		}
		return -1;
	}

	//返回最近的在其右边一个“|”位置
	static int or(int i, String s) {
		for (i = i + 1; i < s.length(); i++) {
			if (s.substring(i, i + 1).equals("|"))
				return i;// 存在，就返回位置
		}
		return -1;// 返回-1表示没有“|”在其右边
	}

	/*产生式输入 */
	static void InputP() {

		RandomAccessFile reader = null;
		try {
			reader = new RandomAccessFile("test.txt", "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String grammers = "";
		try {
			for (String line = null; (line = reader.readLine()) != null;) {
				grammers += line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String temp_grammer[] = grammers.split(",");
		Set<String> set_grammer = new HashSet<String>();

		for (String s : temp_grammer) {
			if (!set_grammer.contains(s))
				set_grammer.add(s);
		}

		grammers.replace("\r", "");
		grammers.replace("\0", "");
		grammers.replace("\n", "");

		for (String s : set_grammer) {
			int j = 3;
			boolean has_delimiter = false;
			String begin = s.substring(0, 3);
			if (or(j, s) == -1) {
				result_grammer.add(s);
			}
			while (or(j, s) != -1 && j < s.length()) {
				has_delimiter = true;
				String temp = begin + s.substring(j, or(j, s));
				result_grammer.add(temp);
				j = or(j, s);
				result_grammer.add(s.substring(0, or(3, s)));
			}
			if (has_delimiter) {
				result_grammer.add(begin + s.substring(j + 1));
			}
		}

		for (String s : result_grammer) {
			System.out.println(s);
			String begin = s.substring(0, 1);
			temp_vn_set.add(begin);
		}

		for (String s : result_grammer) {
			for (int j = 3; j < s.length(); j++) {
				String temp_str = s.substring(j, j + 1);
				if (!temp_vn_set.contains(temp_str) && !temp_str.equals("|")) {
					temp_vt_set.add(temp_str);
				}
			}
		}
	}

	/*求关于字符串s的First集*/
	static Set<String> First(String s) {
		Set<String> results = new HashSet<String>();

		if(s.equals("")){
			return results;
		}
		
		if (s.length() > 1) {// 目标串长度大于1
			boolean is_all_null = true;
			for (int i = 0; i < s.length(); i++) {
				Set<String> temp = First(s.substring(i, i + 1));//递归往下求
				FirstSet.put(s.substring(i, i + 1), temp);
				if(temp_vt_set.contains(s.substring(0, 1))){
					results.add(s.substring(0,1));
					return results;
				}
				for (String str : temp) {
					if (!str.equals("@"))
						results.add(str);
				}
				if (!temp.contains("@")) { // X->Y1Y2……Yk 如果Yi 有一个不含@ 跳出
					is_all_null = false;
					break;
				}
			}
			if (is_all_null) {
				results.add("@"); // 如果Yi都含有@ 加入
			}
		} else {// 单个字符
			if (temp_vt_set.contains(s)) {// 如果是终结符
				results.add(s);
				return results;
			} else {// 如果是非终结符
				// if(isFirst.contains(s)){
				// return FirstSet.get(s);
				// }
				for (String g : result_grammer) {
					String begin = g.substring(0, 1);
					if (begin.equals(s)) {
						Set<String> temp = First(g.substring(3));
						for (String temp_vn : temp) {
							results.add(temp_vn);
						}
					}
				}
			}
		}
		// isFirst.add(s);
		return results;
	}

	/*求所有非终结符的First集*/
	static void getFirstSet() {
		for (String begin : temp_vn_set) {
			FirstSet.put(begin, First(begin));
			isFirst.add(begin);
		}
		System.out.println("FIRST集：");
		for (String s : temp_vn_set) {
			Set<String> vn_first = FirstSet.get(s);
			System.out.print(s + ":");
			for (String vt : vn_first) {
				System.out.print("\t" + vt);
			}
			System.out.println();
		}
	}

	/*求所有非终结符的Follow集*/
	static void getFollowSet() {
		boolean is_add = true;
		while (is_add) {
			is_add = false;
			Set<String> results = new HashSet<String>();
			results.add("#");
			FollowSet.put(begin_Vn, results);
			for (String g : result_grammer) {

				String begin = g.substring(0, 1);

				for (int j = 3; j <= g.length() - 1; j++) {
					Set<String> temp_follow = new HashSet<>();
					Set<String> test_follow = new HashSet<>();
					// 对产生式A->αBβ 将First(β)中除@所有加入Follow(B)
					String cur = g.substring(j, j + 1);
					if (temp_vn_set.contains(cur)) {

						Set<String> back_first = new HashSet<>();
						if (temp_vn_set.contains(g.substring(j + 1))) {
							back_first = FirstSet.get(g.substring(j + 1));
						} else {
							back_first = First(g.substring(j + 1));
						}

						if (FollowSet.containsKey(cur)) {
							temp_follow = FollowSet.get(cur);
						}

						for (String temp_vt : back_first) {
							if (!temp_vt.equals("@")) {
								temp_follow.add(temp_vt);
							}
						}
						// 如果Fisrt(β)含有@或者B在最后， 则把Follow(A)加入Follow(B)
						if (First(g.substring(j + 1)).contains("@") || j == g.length() - 1) {

							Set<String> temp_begin_follow = new HashSet<>();
							if (FollowSet.containsKey(begin)) {
								temp_begin_follow = FollowSet.get(begin);
							}

							for (String temp_vt : temp_begin_follow) {
								temp_follow.add(temp_vt);
							}
						}
						if (FollowSet.containsKey(cur)) {
							test_follow = FollowSet.get(cur);
						}
						if (temp_follow.size() > test_follow.size())
							is_add = true;
						FollowSet.put(cur, temp_follow);
					}
				}
			}
		}
		System.out.println("FOLLOW集：");
		for (String s : temp_vn_set) {
			if (FollowSet.containsKey(s)) {
				Set<String> vn_follow = FollowSet.get(s);
				System.out.print(s + ":");
				for (String vt : vn_follow) {
					System.out.print('\t' + vt);
				}
				System.out.println();
			}
		}
	}

	/*构造预测分析表*/
	static void build_M() {
		
		int vn_num = temp_vn_set.size();
		int vt_num = temp_vt_set.size();
		Vn = new String[vn_num];
		Vt = new String[vt_num];
		M = new String[vn_num][vt_num];

		int index = 0;
		for (String v : temp_vn_set) {
			Vn[index++] = v;
		}
		index = 0;

		System.out.println("预测分析表:");
		System.out.print(" " + '\t');
		for (String v : temp_vt_set) {
			if (v.equals("@")) {
				Vt[index++] = "#";
				System.out.print(Vt[index - 1] + '\t');
			} else {
				Vt[index++] = v;
				System.out.print(v + '\t');
			}
		}
		System.out.println();
		for (int i = 0; i < vn_num; i++)
			for (int j = 0; j < vt_num; j++)
				M[i][j] = "ERROR";

		// 对每个文法A->α
		for (String g : result_grammer) {
			Set<String> temp_set = new HashSet<>();
			String back = g.substring(3);
			String begin = g.substring(0, 1);
			if (FirstSet.containsKey(back)) {
				temp_set = FirstSet.get(back);
			} else {
				temp_set = First(back);
			}
			int begin_index = IndexCh(begin) - 100;
			
			//简单来说就是对于每个文法A->α，让M【A】【Fisrt(α)】=A->α
			for (String temp_vt : temp_set) {
				if (!temp_vt.equals("@"))
					M[begin_index][IndexCh(temp_vt)] = g.substring(3);
			}
			//然后如果@在First（α） 再让M【A】【Follow(A)】=A->α
			if (temp_set.contains("@")) {
				temp_set = FollowSet.get(begin);
				for (String temp_vt : temp_set) {
					M[begin_index][IndexCh(temp_vt)] = g.substring(3);
				}
			}
		}

		for (int i = 0; i < Vn.length; i++) {
			System.out.print(Vn[i] + '\t');
			for (int j = 0; j < Vt.length; j++) {
				System.out.print(M[i][j] + '\t');
			}
			System.out.println();
		}
	}

	/*对输入串进行预测分析*/
	public static void pridictAnalyse() {
		Scanner in = new Scanner(System.in);
		System.out.println("请输入要分析的串：");
		// input_str = in.next() + "#";   //自己输
		input_str = "i+i*i)#";  //为了方便自己预设
		stack.push("$");
		stack.push("E");
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
		if (!input_str.substring(ip, ip + 1).equals("#") && stack_state.equals("#")) {
			System.out.print(matched_str + "\t" + stack_state + "\t" + input_str.substring(ip) + "\t");
			System.out.println("栈空，输入串无法匹配！错误！");
		}
	}

	public static void main(String[] args) {

		Init();

		InputP();

		begin_Vn = "E";

		getFirstSet();
		getFollowSet();

		build_M();
		pridictAnalyse();
		


		//
		// E->TG,
		// G->+TG|-TG,
		// G->@,
		// T->FS,
		// S->*FS|/FS,
		// S->@,
		// F->(E),
		// F->i

		// E->TQ,
		// Q->+TQ|@,
		// T->FP,
		// P->*FP|@,
		// F->(E)|i

		// E->Te,
		// e->+Te|@,
		// T->Ft,
		// t->*Ft|@,
		// F->(E)|i

	}

}
