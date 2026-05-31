package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PlaylistDetailVo extends PlaylistVo {
    private List<MusicVo> songs;
}
