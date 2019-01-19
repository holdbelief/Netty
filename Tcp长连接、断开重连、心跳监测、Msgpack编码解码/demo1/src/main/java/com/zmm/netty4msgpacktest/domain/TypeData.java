package com.zmm.netty4msgpacktest.domain;

public interface TypeData {
	//模式
    byte PING = 1;

    byte PONG = 2;

    byte CUSTOME = 3;

    //*******************************
    byte PING_SEAT = 100;

    byte PONG_SEAT = 101;

    byte SERVER_RESPONSE = 102;

    byte SERVER_RESISTANT = 103;
}
