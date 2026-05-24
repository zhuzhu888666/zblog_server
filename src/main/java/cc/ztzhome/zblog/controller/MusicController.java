package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MusicController {
    //根据歌曲id获取歌曲临时url，刷新url；
    @GetMapping("/public")
    public ResponseModel<String> getMusicUrl(){
        return null;
    }
    //获取随机歌单-权限->public
    //分页查询，接收分页参数，如果无参则默认每页条数据；
    @GetMapping()
    public ResponseModel<List<String>> getRandomList(){
        return null;
    }

    //用户收藏音乐-权限->已登录；

    //用户收藏歌单-权限->已登录；

}
