package org.stradom;

import java.util.*;

public class Main {

    //Через точку с запятой вводятся строки и выражения
    //Из строк можно посредством {число или выражение} ссылаться на другие выражения, но не на строки
    //Вывод - конкатенированные строки, но не выражения, выражения выводятся только если на них ссылались
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Print an expression");
        String input = scan.next();
        System.out.println(StackMachine.process(input));
    }
}
