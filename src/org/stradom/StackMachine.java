package org.stradom;

import java.util.*;

/**
 * Created by Oleg Stradomski on 09.12.2016.
 */
public class StackMachine {

    //Так как предполагается обращение по номеру, то лучше использовать массив, а не стек
    private static String[] strings;
    //Чтобы не считать лишний раз, будем отмечать посчитанные выражения
    //Ключ - это номер выражения, есть ключ - значит оно посчитано.
    private static Map<Integer, Double> exmap = new HashMap<Integer, Double>();
    private static String[] trigonometric = { "sin", "cos", "tan", "ctg" };
    private static String mathops = "+-*/";

    public static String process(String expressions) {
        strings = expressions.split(";");
        String result = "";
        for(int i = 0; i < strings.length; i++) {
            if(isMath(strings[i])) {
                exmap.put(i, calcExpression(strings[i]));
            }
            else {
                result += calcString(strings[i]);
            }
        }
        return result;
    }

    private static String calcString(String string) {
        for (int i = 0;i < string.length();i++) {
            char ch = string.charAt(i);
            if(ch == '{') {
                for (int j = i+1;j < string.length();j++) {
                    char c = string.charAt(j);
                    if(c == '{') {
                        System.out.println("Ошибка в строке, выполнение прекращено!");
                        System.exit(0);
                    }
                    if(c == '}') {
                        if(isMath(string.substring(i+1,j))) {
                            Double result = calcExpression(string.substring(i+1,j));
                            //Если ссылка правильная, т.е. в рамках массива и число там целое
                            if(result-1 >= 0 && result-1 < strings.length && (result == Math.floor(result))) {
                                //Если уже считали
                                if(exmap.containsKey(result.intValue()-1)) {
                                    string = string.replace(string.substring(i+1,j),Double.toString(exmap.get(result.intValue()-1)));
                                    i += Double.toString(exmap.get(result.intValue()-1)).length()+1;
                                }
                                //Если не считали, то посчитаем
                                else {
                                    //Если там строка, то считать нечего - ссылка не верна
                                    if(isMath(strings[result.intValue()-1])) {
                                        Double r = calcExpression(strings[result.intValue()-1]);
                                        exmap.put(result.intValue()-1,r);
                                        string = string.replace(string.substring(i+1,j),Double.toString(r));
                                        i += r.toString().length()+1;
                                    }
                                    else {
                                        string = string.replace(string.substring(i+1,j),"Bad link!");
                                        i += 10;
                                    }
                                }
                            }
                            else {
                                string = string.replace(string.substring(i+1,j),"Bad link!");
                                i += 10;
                            }
                        }
                        else {
                            //Ссылаться можно только на выражения посредством выражений
                            string = string.replace(string.substring(i+1,j),"Bad link!");
                        }
                        break;
                    }
                }
            }
            if(ch == '}') {
                System.out.println("Ошибка в строке, выполнение прекращено!");
                System.exit(0);
            }
        }
        return string;
    }

    private static double calcExpression(String expression) {
        LinkedList<Double> digits = new LinkedList<>();
        LinkedList<String> operators = new LinkedList<>();

        StringTokenizer stringTokenizer = new StringTokenizer(expression, mathops +"()", true);
        while (stringTokenizer.hasMoreTokens())
        {
            String token = stringTokenizer.nextToken();

            if(token.equals("(")) {
                operators.add("(");
            }

            //Если встретили закрывающуюся скобку, выполняем все операции до открывающейся, удаляя их при этом из стека
            else if (token.equals(")")) {

                while(!operators.getLast().equals("(")) {
                    calculate(digits, operators.removeLast());
                }
                operators.removeLast();
            }

            //Добавляем операторы и функции в стек и вычисляем предшествующий если приоритет больше или равен
            else if (isOperator(token)||isFunction(token)) {
                while(!operators.isEmpty() &&
                        priority(operators.getLast()) >= priority(token)) {
                    calculate(digits, operators.removeLast());
                }
                operators.add(token);
            }

            //добавляем числа в стек
            else {
                digits.add(Double.parseDouble(token));
            }
        }

        //Вычисляем оставшиеся операторы
        while(!operators.isEmpty()) {
            try {
                calculate(digits, operators.removeLast());
            }
            catch (Exception e) {
                System.out.println("Ошибка в выражении, выполнение прекращено!");
                System.exit(0);
            }
        }

        return digits.get(0);
    }

    private static void calculate(LinkedList<Double> st, String oper) {

        double firstDigit;
        double secondDigit;
        if (isFunction(oper))
        {
            firstDigit = st.removeLast();
            switch(oper) {
                case "cos":
                    st.add(Math.cos(firstDigit));
                    break;
                case "sin":
                    st.add(Math.sin(firstDigit));
                    break;
                case "tan":
                    st.add(Math.tan(firstDigit));
                    break;
                case "ctg":
                    st.add(1/Math.tan(firstDigit));
                    break;
            }
        }
        else
        {
            firstDigit = st.removeLast();
            secondDigit = st.removeLast();
            switch(oper) {
                case "+":
                    st.add(secondDigit + firstDigit);
                    break;
                case "-":
                    st.add(secondDigit - firstDigit);
                    break;
                case "*":
                    st.add(secondDigit * firstDigit);
                    break;
                case "/":
                    st.add(secondDigit / firstDigit);
                    break;
            }
        }

    }

    private static int priority(String oper) {
        if (oper.equals("+") || oper.equals("-")) {
            return 1;
        }
        else if (oper.equals("(")||oper.equals(")")) {
            return 0;
        }
        else return 2;

    }

    private static boolean isOperator(String tok) {
        return mathops.contains(tok);
    }

    private static boolean isMath(String string) {
        boolean flag = false;
        StringTokenizer stringTokenizer = new StringTokenizer(string, mathops + "()", false);
        while (stringTokenizer.hasMoreTokens())
        {
            String token = stringTokenizer.nextToken();
            if (isFunction(token)){
                flag=true;
            }
            else {
                try
                {
                    Double.parseDouble(token);
                    flag=true;
                }
                catch (NumberFormatException e)
                {
                    flag=false;
                    break;
                }
            }
        }
        return flag;
    }

    private static boolean isFunction(String tok) {
        for (String item : trigonometric) {
            if (item.equals(tok)) {
                return true;
            }
        }
        return false;
    }
}
