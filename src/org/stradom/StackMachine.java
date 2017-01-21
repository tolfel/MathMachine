package org.stradom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Oleg Stradomski on 09.12.2016.
 */
public class StackMachine {

    //Так как предполагается обращение по номеру, то лучше использовать массив, а не стек
    private static String[] strings;
    //Чтобы не считать лишний раз, будем отмечать посчитанные выражения
    //Ключ - это номер выражения, есть ключ - значит оно посчитано.
    private static Map<Integer, Double> exmap = new HashMap<Integer, Double>();

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
                for (int j = i;j < string.length();j++) {
                    char c = string.charAt(j);
                    if(c == '}') {
                        if(isMath(string.substring(i+1,j))) {
                            Double result = calcExpression(string.substring(i+1,j));
                            //Если ссылка правильная, т.е. в рамках массива и число там целое
                            if(result-1 >= 0 && result-1 < strings.length && (result == Math.floor(result))) {
                                //Если уже считали
                                if(exmap.containsKey(result.intValue()-1)) {
                                    string = string.replace(string.substring(i+1,j),Double.toString(exmap.get(result.intValue()-1)));
                                }
                                //Если не считали, то посчитаем
                                else {
                                    //Если там строка, то считать нечего - ссылка не верна
                                    if(isMath(strings[result.intValue()-1])) {
                                        Double r = calcExpression(strings[result.intValue()-1]);
                                        exmap.put(result.intValue()-1,r);
                                        string = string.replace(string.substring(i+1,j),Double.toString(r));
                                    }
                                    else {
                                        string = string.replace(string.substring(i+1,j),"Bad link!");
                                    }
                                }
                            }
                            else {
                                string = string.replace(string.substring(i+1,j),"Bad link!");
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
        }
        return string;
    }

    private static double calcExpression(String expression) {
        Stack<Integer> operators  = new Stack<Integer>();
        Stack<Double> values = new Stack<Double>();
        Stack<Integer> operatorstmp  = new Stack<Integer>();
        Stack<Double> valuestmp = new Stack<Double>();

        String value = "";
        boolean f = false;
        for (int i = 0;i < expression.length();i++)
        {
            char ch = expression.charAt(i);
            /* Если встретили открывающуюся скобку, то найдём закрывающую, вытащим выражение внутри
            и рекурсивно применим этот же метод*/
            if(ch == '(') {
                String ex = expression.substring(i+1,expression.length());
                //Считаем количество открывающих скобок, чтобы остановиться не на первой закрывающей, а на последней.
                int flag = 0;
                int j = 0;
                char c = ' ';
                while(flag != 0 && c != ')' || j < ex.length() ) {
                    c = ex.charAt(j);
                    if(c == '(') {
                        flag++;
                    }
                    if(c == ')') {
                        if(flag == 0) {
                            break;
                        }
                        else {
                            flag--;
                        }
                    }
                    j++;
                }
                //Заменяем выражение в скобках его значением
                expression = expression.replace(expression.substring(i,i+j+2),Double.toString(calcExpression(expression.substring(i+1,i+j+1))));
                i--;
                f = false;
            }
            else if (ch != '+' &&  ch != '*' && ch != '/' && ch != '-') {
                //Сюда записываем все цифры, идущие вместе, чтобы в итоге получить число
                value = value + ch;
                f = false;
            }
            else {
                if(f) {
                    System.out.println("Ошибка в выражении, выполнение прекращено!");
                    System.exit(0);
                }
                f = true;
                //Если в велью лежит шестнадцатиричное число, то упадёт NumberFormatException, тогда парсим по основанию 16
                try {
                    values.push(Double.parseDouble(value));
                }
                catch(NumberFormatException e) {
                    value = value.replace("0x", "");
                    values.push(new Double(Integer.parseInt(value, 16)));
                }
                //После окончания числа останется оператор, его пихаем в операторы
                operators.push((int) ch);
                value = "";
            }
        }
        try {
            values.push(Double.parseDouble(value));
        }
        catch(NumberFormatException e) {
            value = value.replace("0x", "");
            values.push(new Double(Integer.parseInt(value, 16)));
        }

        //Будет по очереди считать каждый оператор в правильном порядке
        //Достаём числа рядом с оператором, а не какие-нибудь первые попавшиеся
        char mathOps[] = {'/','*','+','-'};
        for (int i = 0; i < 4; i++)
        {
            boolean it = false;
            while (!operators.isEmpty())
            {
                int optr = operators.pop();
                double v1 = values.pop();
                double v2 = values.pop();
                if (optr == mathOps[i])
                {
                    if (i == 0)
                    {
                        valuestmp.push(v2 / v1);
                        it = true;
                        break;
                    }
                    else if (i == 1)
                    {
                        valuestmp.push(v2 * v1);
                        it = true;
                        break;
                    }
                    else if (i == 2)
                    {
                        valuestmp.push(v2 + v1);
                        it = true;
                        break;
                    }
                    else if (i == 3)
                    {
                        valuestmp.push(v2 - v1);
                        it = true;
                        break;
                    }
                }
                else
                {
                    //Если оператор был "не тот"(Например плюс, а мы только деление рассматриваем, то пока откладываем его
                    valuestmp.push(v1);
                    values.push(v2);
                    operatorstmp.push(optr);
                }
            }
            while (!valuestmp.isEmpty())
                values.push(valuestmp.pop());
            while (!operatorstmp.isEmpty())
                operators.push(operatorstmp.pop());
            if (it)
                i--;
        }
        return values.pop();
    }

    private static boolean isMath(String string) {
        if(string.matches("^[-*+/0123456789()]+$")) {
            return true;
        }
        else {
            return false;
        }
    }
}
