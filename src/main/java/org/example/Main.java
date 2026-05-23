package org.example;

import org.example.parser.NetallParser;

public class Main {

    public static void main(String[] args) {

        NetallParser parser =
                new NetallParser();

        parser.parseNews();
    }
}