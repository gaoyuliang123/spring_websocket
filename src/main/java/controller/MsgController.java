package controller;

import com.google.gson.GsonBuilder;
import entity.Message;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.TextMessage;
import websocket.MyWebSocketHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/msg")
public class MsgController {
    @Autowired
    MyWebSocketHandler myWebSocketHandler;

    Map<Long, User> users = new HashMap<Long, User>();
    //数据模拟
    @ModelAttribute
    public void setReqAndRes() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("张三");
        users.put(user1.getId(), user1);
        User user2 = new User();
        user2.setId(2L);
        user2.setName("李四");
        users.put(user2.getId(), user2);
    }

    //用户登录
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView doLogin(User user, HttpServletRequest request) {
        request.getSession().setAttribute("uid", user.getId());
        request.getSession().setAttribute("name", users.get(user.getId()).getName());
        return new ModelAndView("redirect:talk");
    }

    //跳转到交谈聊天页面
    @RequestMapping(value = "talk", method = RequestMethod.GET)
    public ModelAndView talk() {
        return new ModelAndView("talk");
    }

    //跳转到广播页面
    @RequestMapping(value = "broadcast", method = RequestMethod.GET)
    public ModelAndView broadcast() {
        return new ModelAndView("broadcast");
    }

    //发布系统广播（群发）
    @ResponseBody
    @RequestMapping(value = "broadcast", method = RequestMethod.POST)
    public void broadcast(String text) throws Exception {
        Message msg = new Message();
        msg.setFrom(-1L);
        msg.setFromName("系统广播");
        msg.setTo(0L);
        msg.setDate(new Date());
        msg.setText(text);
        myWebSocketHandler.broadcast(new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
    }

}
