package com.xjtu.power.agent.engine;

public class Test1 {
    public static void main(String[] args){
        for (int i = 0; i < 10; i++) {
            System.out.println(0.8 +  (1.8-0.8)*i/10);
        }
        System.out.println("---------------");
        for (int i = 0; i < 10; i++) {
            System.out.println(0.6 + (1.2 - 0.6) * i / 10);
        }
        System.out.println((int)((41.25-30)/1.5));
        String str = "123";
        System.out.println(str == "123");
        System.out.println(str.equals("123"));
    }
}
