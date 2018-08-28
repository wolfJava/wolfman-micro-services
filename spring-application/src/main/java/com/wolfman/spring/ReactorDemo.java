package com.wolfman.spring;

import java.util.stream.Stream;

public class ReactorDemo {

    public static void main(String[] args) {


        Stream
                .of(1,2,3,4,5,6) //生产
                .map(String::valueOf) //处理
                .forEach(System.out::println); //消费


    }


}
