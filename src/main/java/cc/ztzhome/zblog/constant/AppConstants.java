package cc.ztzhome.zblog.constant;

/**
 * 应用常量
 */
public final class AppConstants {

    private AppConstants() {
    }

    //普通临时Url过期时间->24小时
    public static final long URL_TIMEOUT= 1000L*60*60*24;

    // ==================== 用户 ====================

    /** 用户头像路径 */
    public static final String USER_AVATAR = "user/userAvatar/";

    // ==================== 音乐 ====================

    /** 音乐文件存放路径 */
    public static final String SONG_SAVE_PATH = "music/songs/";

    /** 歌曲封面存放路径 */
    public static final String SONG_COVER_PATH = "music/cover/";

    /** 歌词存放路径 */
    public static final String SONG_LYRIC_PATH = "music/lrc/";

    /** 歌单封面存放路径 */
    public static final String PLAYLIST_COVER_PATH = "music/cover/";

    // ==================== 歌手 ====================

    /** 歌手头像路径 */
    public static final String ARTIST_COVER_PATH = "artist/cover/";

    // ==================== 图片 ====================

    /** 轮播图片存放路径 */
    public static final String CAROUSEL_IMAGE = "image/carousel/";
}
