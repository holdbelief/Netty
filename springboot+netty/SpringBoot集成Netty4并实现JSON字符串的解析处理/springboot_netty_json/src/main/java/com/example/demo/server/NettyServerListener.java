package com.example.demo.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.beans.factory.annotation.Autowired;

@WebListener
public class NettyServerListener implements ServletContextListener {

	  /**
     * 注入NettyServer
     */
    @Autowired
    private NettyServer nettyServer;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	 @Override 
	 public void contextInitialized(ServletContextEvent sce) {
		 Thread thread = new Thread(new NettyServerThread()); 
		 // 启动netty服务 
		 thread.start(); 
	 }
	 
	 /**
	     * netty服务启动线程 . <br>
	     * 
	     * @author hkb
	     */ 
	 private class NettyServerThread implements Runnable {
	
		 @Override 
		 public void run() {
			 nettyServer.run();
		 } 
	 } 
	
}
