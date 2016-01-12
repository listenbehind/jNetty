package com.jnetty.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import com.jnetty.core.Config.ServiceConfig;
import com.jnetty.core.server.handler.NettyHandler;

public class SimpleNettyServer implements Server {
	
	private String ip = "127.0.0.1";
	private int port = 8080;
	private EventLoopGroup workerGroup = null;
	private EventLoopGroup bossGroup = null;
	private ServerBootstrap serverBootstrap = null;
	private NettyHandler nettyHandler = null;

	private ServiceConfig serviceConfig = null;
	
	public void initialize() {
		this.nettyHandler = new NettyHandler();
		this.nettyHandler.setServer(this);
		
		this.workerGroup = new NioEventLoopGroup();
		this.bossGroup = new NioEventLoopGroup();
		this.serverBootstrap = new ServerBootstrap();
		this.serverBootstrap.group(bossGroup, workerGroup);
		this.serverBootstrap.channel(NioServerSocketChannel.class);
		this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast("http-decoder", new HttpRequestDecoder());
				sc.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
				sc.pipeline().addLast("http-encoder", new HttpResponseEncoder());
				sc.pipeline().addLast("netty-handler", SimpleNettyServer.this.nettyHandler);
			}
		});
		this.serverBootstrap.option(ChannelOption.SO_BACKLOG, this.serviceConfig.so_back_log);
		this.serverBootstrap.option(ChannelOption.SO_KEEPALIVE, this.serviceConfig.so_keep_alive);
	}

	public void start() {
		try {
			ChannelFuture future = this.serverBootstrap.bind(this.ip, this.port).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.bossGroup.shutdownGracefully();
			this.workerGroup.shutdownGracefully();
		}
	}

	public void stop() {
		
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ServiceConfig getConfig() {
		return this.serviceConfig;
	}

	public void setConfig(ServiceConfig config) {
		this.serviceConfig = config;
	}
	
}