package cc.ztzhome.zblog.utils;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class FileTypeUtil {
    //String fileName = file.getOriginalFilename(); // 原始文件名（含后缀）

    // 定义常见文件类型扩展名
    private static final List<String> MUSIC_EXT = Arrays.asList(
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma"
    );

    private static final List<String> IMAGE_EXT = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff"
    );

    private static final List<String> VIDEO_EXT = Arrays.asList(
            "mp4", "avi", "mov", "mkv", "flv", "wmv", "mpeg", "3gp"
    );

    private static final List<String> TEXT_EXT = Arrays.asList(
            "txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "md", "rtf", "csv", "html", "htm", "xml", "json", "ini", "log", "lrc"
    );

    private static final List<String> ZIP_EXT = Arrays.asList(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "jar", "war"
    );

    /**
     * 根据文件名判断文件类型
     * @param fileName 文件名（带扩展名）
     * @return 文件类型标识字符串
     */
    public static String getFileType(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "other";
        }

        // 获取文件扩展名（转换为小写）
        String extension = getFileExtension(fileName).toLowerCase();

        if (MUSIC_EXT.contains(extension)) {
            return "music";
        } else if (IMAGE_EXT.contains(extension)) {
            return "image";
        } else if (VIDEO_EXT.contains(extension)) {
            return "video";
        } else if (TEXT_EXT.contains(extension)) {
            return "text";
        } else if (ZIP_EXT.contains(extension)) {
            return "zip";
        } else {
            return "other";
        }
    }

    /**
     * 提取文件扩展名
     * @param fileName 文件名
     * @return 文件扩展名（不带点）
     */
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "无拓展名"; // 无扩展名
    }
    public static String getFileExtension2(String fileName) {
        String extWithDot = "";  // 带点的后缀
        if (fileName != null && fileName.contains(".")) {
            // 获取从最后一个点开始的后缀（包含点）
            extWithDot = fileName.substring(fileName.lastIndexOf("."));
        }
        return extWithDot;
    }
}