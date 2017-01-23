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
        Stack<Double> digits = new Stack<>();
        Stack<String> operators = new Stack<>();

        StringTokenizer stringTokenizer = new StringTokenizer(expression, mathops +"()", true);
        while (stringTokenizer.hasMoreTokens())
        {
            String token = stringTokenizer.nextToken();
            //Считаем всё, что внутри скобок
            if(token.equals("(")) {
                operators.push("(");
            }
            else if (token.equals(")")) {

                while(!operators.peek().equals("(")) {
                    calculate(digits, operators.pop());
                }
                operators.pop();
            }
            else if (isOperator(token)||isFunction(token)) {
                while(!operators.isEmpty() &&
                        priority(operators.peek()) >= priority(token)) {
                    calculate(digits, operators.pop());
                }
                operators.push(token);
            }
            else {
                digits.push(Double.parseDouble(token));
            }
        }
        while(!operators.isEmpty()) {
            try {
                calculate(digits, operators.pop());
            }
            catch (Exception e) {
                System.out.println("Ошибка в выражении, выполнение прекращено!");
                System.exit(0);
            }
        }

        return digits.pop();
    }

    private static void calculate(Stack<Double> st, String oper) {

        double d1;
        double d2;
        if (isFunction(oper))
        {
            d1 = st.pop();
            switch(oper) {
                case "cos":
                    st.push(Math.cos(d1));
                    break;
                case "sin":
                    st.push(Math.sin(d1));
                    break;
                case "tan":
                    st.push(Math.tan(d1));
                    break;
                case "ctg":
                    st.push(1/Math.tan(d1));
                    break;
            }
        }
        else
        {
            d1 = st.pop();
            d2 = st.pop();
            switch(oper) {
                case "+":
                    st.push(d2 + d1);
                    break;
                case "-":
                    st.push(d2 - d1);
                    break;
                case "*":
                    st.push(d2 * d1);
                    break;
                case "/":
                    st.push(d2 / d1);
                    break;
            }
        }

    }

    private static int priority(String operator) {
        if (operator.equals("+") || operator.equals("-")) {
            return 1;
        }
        else if (operator.equals("(")||operator.equals(")")) {
            return 0;
        }
        else return 2;

    }

    private static boolean isOperator(String string) {
        return mathops.contains(string);
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
