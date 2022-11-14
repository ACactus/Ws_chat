package cn.tedu.ws_chat.controller;


import cn.tedu.ws_chat.WsChatApplication;
import cn.tedu.ws_chat.entity.ChatMessage;
import cn.tedu.ws_chat.utils.SessionUtil;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author buhao
 * @version WsServerEndpoint.java, v 0.1 2019-10-18 16:06 buhao
 */
@ServerEndpoint("/ws")
@Component
public class WsServerEndpoint {
    private static Set<WsServerEndpoint> webSockets = new HashSet<>();
    private Session session;
    private String ip;

    /**
     * 连接成功
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        webSockets.add(this);
        this.session = session;
        this.ip = SessionUtil.getRemoteAddress(session).toString();
        this.ip = this.ip.substring(1, this.ip.lastIndexOf(':'));
        String str = ip + "连接了！当前连接数：" + getClientCount();
        ChatMessage message = new ChatMessage();
        message.setSender("System");
        message.setType(ChatMessage.MessageType.JOIN);
        message.setCreateTime(new Date());
        message.setContent(str);
        message.setSender(ip);
        broadcast(message);
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        webSockets.remove(this);
        ChatMessage message = new ChatMessage();
        message.setSender("System");
        message.setContent(ip + "离开了， 当前连接数：" + getClientCount());
        message.setCreateTime(new Date());
        message.setType(ChatMessage.MessageType.LEAVE);
        broadcast(message);
    }

    /**
     * 接收到消息
     *
     * @param text
     */
    @OnMessage
    public void onMsg(String text) throws IOException {
        ChatMessage message = new ChatMessage();
        message.setContent(text);
        message.setCreateTime(new Date());
        message.setType(ChatMessage.MessageType.CHAT);
        message.setSender(this.ip);

//        return "servet 发送：" + text;
        broadcast(message);
    }

    private static int getClientCount(){
        synchronized (webSockets){
            return webSockets.size();
        }
    }

    private static void broadcast(ChatMessage message) throws IOException {
        System.out.println("广播消息：" + message);
        synchronized (webSockets){
            for (WsServerEndpoint webSocket : webSockets) {
                webSocket.session.getBasicRemote().sendText(JSON.toJSONString(message));
            }
        }
    }


}