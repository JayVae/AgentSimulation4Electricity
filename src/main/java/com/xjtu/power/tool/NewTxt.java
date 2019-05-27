package com.xjtu.power.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NewTxt {

    public static void arrOut(double[][] arr) throws IOException {
        int n = arr.length;

        File file = new File("h:\\array.txt"); //存放数组数据的文件

        FileWriter out = new FileWriter(file); //文件写入流

//将数组中的数据写入到文件中。每行各数据之间TAB间隔
        for(int i=0;i<n;i++){
            for(int j=0;j<arr[i].length;j++){
                out.write(arr[i][j]+",");
            }
            out.write("\r\n");
        }
        out.close();

    }
} 