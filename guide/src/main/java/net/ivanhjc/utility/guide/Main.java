package net.ivanhjc.utility.guide;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        System.out.println(lastStringLength());
        System.out.println(charOccurrences());
    }

    /**
     * 字符串最后一个单词的长度
     * 题目描述：计算字符串最后一个单词的长度，单词以空格隔开。
     * 输入描述: 输入一行，代表要计算的字符串，非空，长度小于5000。
     * 输出描述: 输出一个整数，表示输入字符串最后一个单词的长度。
     */
    public static int lastStringLength() {
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();
        if (str == null || (str = str.trim()).length() == 0) {
            return 0;
        }
        int lastSpcIdx = str.lastIndexOf(' ');
        return str.length() - 1 - lastSpcIdx;
    }

    /**
     * 题目描述: 写出一个程序，接受一个由字母、数字和空格组成的字符串，和一个字母，然后输出输入字符串中该字母的出现次数。不区分大小写。
     * 输入描述: 第一行输入一个由字母和数字以及空格组成的字符串，第二行输入一个字母。
     * 输出描述: 输出输入字符串中含有该字符的个数。
     */
    public static int charOccurrences() {
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();
        String str2 = sc.nextLine();
        if (str == null || (str = str.trim()).length() == 0 || str2 == null || (str2 = str2.trim()).length() == 0) {
            return 0;
        }
        int n = 0;
        char[] chars = str.toCharArray();
        char ch = str2.toCharArray()[0];
        for (int i = 0; i < chars.length; i++) {
            if (Character.toUpperCase(chars[i]) == Character.toUpperCase(ch)) {
                n++;
            }
        }
        return n;
    }
}
